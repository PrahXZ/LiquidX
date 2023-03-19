package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer

class PacketNofall : NoFallMode("Packet") {

    private val packetmode = ListValue("Packet-Mode", arrayOf("Packet1", "Packet2", "Packet3"), "Packet1")

    private var packet1Count = 0
    private var packetModify = false

    override fun onEnable() {
        packet1Count = 0
        packetModify = false
    }

    override fun onNoFall(event: UpdateEvent) {
        when (packetmode.get()) {
            "Packet1" -> {
                if (mc.thePlayer.fallDistance.toInt() / 3 > packet1Count) {
                    packet1Count = mc.thePlayer.fallDistance.toInt() / 3
                    packetModify = true
                }
                if (mc.thePlayer.onGround) {
                    packet1Count = 0
                }
            }
            "Packet2" -> {
                if (mc.thePlayer.fallDistance.toInt() / 2 > packet1Count) {
                    packet1Count = mc.thePlayer.fallDistance.toInt() / 2
                    packetModify = true
                }
                if (mc.thePlayer.onGround) {
                    packet1Count = 0
                }
            }
            "Packet3" -> {
                if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3f){
                    mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                    mc.thePlayer.fallDistance = 0f
                }
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        when (packetmode.get()) {
            "Packet1" -> {
                if(event.packet is C03PacketPlayer) {
                    if(packetModify) {
                        event.packet.onGround = true
                        packetModify = false
                    }
                }
            }
            "Packet2" -> {
                if(event.packet is C03PacketPlayer) {
                    if(packetModify) {
                        event.packet.onGround = true
                        packetModify = false
                    }
                }
            }
        }
    }
}