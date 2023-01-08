package net.ccbluex.liquidbounce.features.module.modules.movement.flys.zonecraft

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer

class ZoneCraftVanilla: FlyMode("ZoneCraftVanilla") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 10f)

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.capabilities.isFlying = false
        MovementUtils.resetMotion(true)
        MovementUtils.strafe(speedValue.get())
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet !is C00PacketKeepAlive) {
            if (event.type === PacketEvent.Type.SEND) {
                if (mc.thePlayer.ticksExisted % 3 == 0 && packet is C03PacketPlayer) { // Previene el check de desync de morgan :P
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer(true))
                }
                event.cancelEvent()
            }
        }
    }

}