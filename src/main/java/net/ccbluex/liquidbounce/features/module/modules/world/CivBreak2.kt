package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import kotlin.random.Random

object CivBreak2 : Module("CivBreak2", Category.WORLD) {

    private val reach = IntegerValue("BreakReach", 4, 0..10)
    private var delay = IntegerValue("BreakDelay", 2, 2..7)

    private var stopPacket: C07PacketPlayerDigging? = null
    private var breakPos: BlockPos? = null
    private var tick = 0
    private var blocksBroken = 0
    private var slowBreaking = false

    override fun onDisable() {
        breakPos = null
        tick = 0
        stopPacket = null
        blocksBroken = 0
        slowBreaking = false
    }

    @EventTarget
    fun onBlockClick(event: ClickBlockEvent) {
        breakPos = event.clickedBlock
    }

    @EventTarget
    fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
        if (blocksBroken >= 6) {
            slowBreaking = true
            blocksBroken = 0
        }

        if (slowBreaking) {
            if (tick % (delay.value * 5) == 0) {
                sendHitPacket()
                slowBreaking = false
            }
            tick++
            return
        }

        breakPos = searchNearNexus()
        if (breakPos == null || mc.thePlayer.positionVector.distanceTo(Vec3(breakPos!!.x.toDouble(), breakPos!!.y.toDouble(), breakPos!!.z.toDouble())) > reach.value || mc.theWorld.getBlockState(
                breakPos
            ).block == Blocks.bedrock) {
            stopPacket = null
            breakPos = null
            tick = 0
            return
        }

        if (stopPacket == null) {
            if (tick == 0 && mc.playerController.clickBlock(breakPos, EnumFacing.DOWN)) {
                mc.thePlayer.swingItem()
                tick++
            }

            if (mc.playerController.onPlayerDamageBlock(breakPos, EnumFacing.DOWN)) {
                mc.thePlayer.swingItem()
            }
        } else if (tick-- <= 0) {
            mc.thePlayer.swingItem()
            PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, EnumFacing.DOWN))
            PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, breakPos, EnumFacing.DOWN))

            mc.theWorld.markBlockRangeForRenderUpdate(breakPos, breakPos)

            tick = getRandomDelay()
            blocksBroken++
        }
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C07PacketPlayerDigging) {
            val diggingPacket = event.packet
            if (diggingPacket.status != C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                return
            }

            stopPacket = diggingPacket
            tick = delay.value
        }
    }

    private fun searchNearNexus(): BlockPos? {
        if (breakPos != null) {
            return breakPos
        }

        val reach = reach.value
        for (x in -reach until reach) {
            for (y in -reach until reach) {
                for (z in -reach until reach) {
                    val blockPos = BlockPos(
                        mc.thePlayer.posX + x,
                        mc.thePlayer.posY + y,
                        mc.thePlayer.posZ + z
                    )
                    if (mc.theWorld.getBlockState(blockPos).block == Blocks.end_stone) {
                        return blockPos
                    }
                }
            }
        }
        return null
    }

    private fun sendHitPacket() {
        repeat(10) {
            val swingPacket = C0APacketAnimation()
            PacketUtils.sendPacketNoEvent(swingPacket)
        }

        val attackPacket = C02PacketUseEntity(mc.thePlayer, C02PacketUseEntity.Action.ATTACK)
        PacketUtils.sendPacketNoEvent(attackPacket)
    }

    private fun getRandomDelay(): Int {
        return Random.nextInt(2, 8)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        RenderUtils.drawBlockBox(CivBreak.blockPos ?: return, Color.RED, true)
    }
}
