package net.ccbluex.liquidbounce.features.module.modules.movement.fly.modes.test

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.player.nofall.ModuleNoFall
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import kotlin.math.cos
import kotlin.math.sin

internal object FlySparkyA : Choice("Sparky A") {

    override val parent: ChoiceConfigurable<*>
        get() = ModuleNoFall.modes

    private val flySpeed by double("FlySpeed", 0.1, 0.01..2.0)

    val packetHandler = handler<PacketEvent> {
        val packet = it.packet
        val player = mc.player
        if (player != null && packet is PlayerMoveC2SPacket) {
            player.setVelocity(0.0, 0.0, 0.0)

            if (mc.options.forwardKey.isPressed) {
                player.velocity = player.velocity.add(
                    -sin(Math.toRadians(player.yaw.toDouble())) * flySpeed,
                    0.0,
                    cos(Math.toRadians(player.yaw.toDouble())) * flySpeed
                )
            }

            if (mc.options.jumpKey.isPressed) {
                player.setVelocity(0.0, flySpeed, 0.0)
            } else if (mc.options.sneakKey.isPressed) {
                player.setVelocity(0.0, -flySpeed, 0.0)
            }

            val posX = player.x
            val posY = player.y
            val posZ = player.z
            player.networkHandler?.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(posX, posY + 0.01, posZ, false))
        }
    }
}
