/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.fly.modes.test

import net.ccbluex.liquidbounce.config.Choice
import net.ccbluex.liquidbounce.config.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.player.nofall.ModuleNoFall
import net.ccbluex.liquidbounce.utils.entity.isFallingToVoid
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket

internal object FlySparkyA : Choice("Sparky A") {

    override val parent: ChoiceConfigurable<*>
        get() = ModuleNoFall.modes

    private val voidThreshold by int("VoidLevel", 0, -256..0)

    val packetHandler = handler<PacketEvent> {
        val packet = it.packet

        if (packet is PlayerMoveC2SPacket && player.fallDistance in 2.5..50.0
            && !player.isFallingToVoid(voidLevel = voidThreshold.toDouble(), safetyExpand = 0.0)) {

            // Apply very slow falling speed (hover in the air)
            player.setVelocity(0.0, -0.05, 0.0)  // Adjust this value for a more noticeable slow fall

            // Optional teleportation to current position to avoid triggering ground detection
            val posX = player.x
            val posY = player.y
            val posZ = player.z

            // Send a teleport packet to reset position slightly upward to simulate fly-like behavior
            player.networkHandler?.sendPacket(PlayerMoveC2SPacket.PositionAndOnGround(posX, posY + 0.01, posZ, false))

            // Prevent player from being set on the ground
            player.input.sneaking = true
        }
    }
}
