package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity

class UniversoReverseVelocity : VelocityMode("UniversoReverse") {
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            packet.motionX = (packet.getMotionX() * -0.3).toInt()
            packet.motionZ = (packet.getMotionZ() * -0.3).toInt()
        }
    }
}