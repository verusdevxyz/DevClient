package net.ccbluex.liquidbounce.features.module.modules.movement.speed.modes.grim

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.events.MovementInputEvent
import net.ccbluex.liquidbounce.event.events.PlayerTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.movement.speed.ModuleSpeed
import net.ccbluex.liquidbounce.utils.client.handlePacket
import net.ccbluex.liquidbounce.utils.client.inGame
import net.ccbluex.liquidbounce.utils.entity.directionYaw
import net.ccbluex.liquidbounce.utils.entity.moving
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import kotlin.math.cos
import kotlin.math.sin

class SpeedGrimTest(override val parent: ChoiceConfigurable<*>) : Choice("GrimTest") {

    private val speed by float("BoostSpeed", 0.08F, 0.01F..0.08F, "b/t")

    /**
     * Handles player movement to make the jump input appear legit and sprint in all directions.
     */
    val handleMovementInput = handler<MovementInputEvent> {
        if (!player.isOnGround || !player.moving) {
            return@handler
        }

        if (!mc.options.jumpKey.isPressed && ModuleSpeed.shouldDelayJump()) {
            return@handler
        }

        player.networkHandler.sendPacket(ClientCommandC2SPacket(player, Mode.START_SPRINTING))
        //it.jumping = true
    }

    /**
     * Grim Collide mode for the Speed module.
     * Increases speed when colliding with entities while in the air, only when moving backward.
     * Also includes techniques to manipulate packet events to stay undetectable.
     */
    val tickHandler = handler<PlayerTickEvent> {
        if (player.input.movementForward >= 0.0f && player.input.movementSideways == 0.0f) { return@handler }

        if (!player.isOnGround) {
            var collisions = 0
            val box = player.boundingBox.expand(1.0)
            for (entity in world.entities) {
                val entityBox = entity.boundingBox
                if (canCauseSpeed(entity) && box.intersects(entityBox)) {
                    collisions++
                }
            }

            val yaw = Math.toRadians(player.directionYaw.toDouble())
            val boost = this.speed * collisions
            player.addVelocity(-sin(yaw) * boost, 0.0, cos(yaw) * boost)
        }
    }

    /**
     * Handles packet manipulation to introduce lag or crash packets to avoid detection.
     */
    val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet

        if (inGame && player.moving && packet is PlayerMoveC2SPacket) {
            handlePacket(PlayerMoveC2SPacket.Full(player.x, player.y + 0.1, player.z, player.yaw, player.pitch, false))
            //event.cancelEvent()
        }
    }

    private fun canCauseSpeed(entity: Entity) =
        entity != player && entity is LivingEntity && entity !is ArmorStandEntity
}
