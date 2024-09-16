package net.ccbluex.liquidbounce.features.module.modules.movement.speed.modes.grim

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.events.MovementInputEvent
import net.ccbluex.liquidbounce.event.events.PlayerTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.movement.fly.ModuleFly
import net.ccbluex.liquidbounce.features.module.modules.movement.speed.ModuleSpeed
import net.ccbluex.liquidbounce.script.bindings.api.JsClient
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.handlePacket
import net.ccbluex.liquidbounce.utils.client.inGame
import net.ccbluex.liquidbounce.utils.client.regular
import net.ccbluex.liquidbounce.utils.entity.directionYaw
import net.ccbluex.liquidbounce.utils.entity.moving
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.raphimc.minecraftauth.MinecraftAuth
import kotlin.math.cos
import kotlin.math.sin

class SpeedGrimBoost(override val parent: ChoiceConfigurable<*>) : Choice("GrimBoost") {
    private val speed by float("BoostSpeed", 0F, 0F..20F, "b/t")
    private val lowStrafeFactor by float("LowStrafeFactor", 0.85F, 0.1F..1.0F, "b/t")
    private var boosting = false
    private var lastSprinting = false
    //private var lastSprinting = this.player?.isSprinting ?: false

    private fun getLastSprinting(): Boolean {
        return player.isSprinting
    }
    /**
     * Handles player movement and applies low strafe and boosting logic.
     */
    val handleMovementInput = handler<MovementInputEvent> {
        if (!player.isOnGround || !player.moving) {
            return@handler
        }

        if (!mc.options.jumpKey.isPressed && ModuleSpeed.shouldDelayJump()) {
            return@handler
        }

        it.jumping = true

        if (boosting) {
            //player.networkHandler.sendPacket(ClientCommandC2SPacket(player, Mode.START_SPRINTING))
            applyLowStrafe()
        } else {
            player.input.movementForward = 1.0f
            player.input.movementSideways = 0.0f
        }
    }

    /**
     * Grim Collide mode for the Speed module.
     * Increases speed when colliding with entities while in the air and activates boost.
     */
    val tickHandler = handler<PlayerTickEvent> {
        boosting = false

        if (player.input.movementForward <= 0.0f) {
            return@handler
        }

        if (!player.isOnGround) {
            var collisions = 0
            val box = player.boundingBox.expand(1.0)
            for (entity in world.entities) {
                val entityBox = entity.boundingBox
                if (canCauseSpeed(entity) && box.intersects(entityBox)) {
                    collisions++
                }
            }

            if (collisions > 0) {
                val yaw = Math.toRadians(player.directionYaw.toDouble())
                val boost = this.speed * collisions
                player.addVelocity(-sin(yaw) * boost, 0.0, cos(yaw) * boost)
                boosting = true
            }
        }
    }

    /**
     * Handles packet manipulation to introduce lag or crash packets to avoid detection.
     */
    val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet

        if (inGame && player.moving && packet is PlayerMoveC2SPacket) {
            handlePacket(PlayerMoveC2SPacket.Full(player.x, player.y + 0.1, player.z, player.yaw, player.pitch, false))
            // event.cancelEvent()
        }

        if (boosting) {
            if (packet is ClientCommandC2SPacket) {
                when (packet.mode) {
                    Mode.START_SPRINTING -> {
                        if (getLastSprinting()) {
                            //event.cancelEvent()
                        }
                        lastSprinting = true
                    }

                    Mode.STOP_SPRINTING -> {
                        if (!getLastSprinting()) {
                            //event.cancelEvent()
                        }
                        lastSprinting = false
                    }

                    Mode.PRESS_SHIFT_KEY,
                    Mode.RELEASE_SHIFT_KEY,
                    Mode.STOP_SLEEPING,
                    Mode.START_RIDING_JUMP,
                    Mode.STOP_RIDING_JUMP,
                    Mode.OPEN_INVENTORY,
                    Mode.START_FALL_FLYING -> TODO()

                    null -> {
                        chat(regular(ModuleFly.message("lol")))
                    }
                }
            }
        }
    }

    /**
     * Applies low strafe when boosting to avoid detection.
     */
    private fun applyLowStrafe() {
        player.input.movementSideways *= lowStrafeFactor
    }

    private fun canCauseSpeed(entity: Entity) =
        entity != player && entity is LivingEntity && entity !is ArmorStandEntity
}
