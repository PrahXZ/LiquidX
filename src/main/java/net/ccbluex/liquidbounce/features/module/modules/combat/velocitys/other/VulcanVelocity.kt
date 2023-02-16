package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.network.play.client.C0FPacketConfirmTransaction

class VulcanVelocity : VelocityMode("Vulcan") {

    override fun onEnable() {
        ClientUtils.displayChatMessage("§6§lWARNING: §fOnly works on some vulcan configurations")
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0FPacketConfirmTransaction) {
            val transUID = (packet.uid).toInt()
            if (transUID >= -31767 && transUID <= -30769) {
                event.cancelEvent()
            }
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        event.cancelEvent()
    }
}