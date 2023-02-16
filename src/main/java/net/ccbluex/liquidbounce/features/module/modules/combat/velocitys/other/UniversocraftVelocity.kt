package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity

class UniversocraftVelocity : VelocityMode("Universocraft") {


    private val modevalue = ListValue("Universocraft-mode", arrayOf("Normal", "Hard"), "Normal")


    override fun onVelocity(event: UpdateEvent) {
        if (modevalue.equals("Normal") &&  mc.thePlayer.hurtTime > 0) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.RIDING_JUMP))
        }
        if (modevalue.equals("Hard") &&  mc.thePlayer.hurtTime > 0) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.RIDING_JUMP))
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet

        if (modevalue.equals("Normal")) {
            if (packet is S12PacketEntityVelocity) {
                packet.motionX = (packet.getMotionX() * -0.5).toInt()
                packet.motionZ = (packet.getMotionZ() * -0.1).toInt()
            }
        }
        if (modevalue.equals("Hard")) {
            if (packet is S12PacketEntityVelocity) {
                packet.motionX = (packet.getMotionX() * -0.1).toInt()
                packet.motionZ = (packet.getMotionZ() * -0.3).toInt()
            }
        }
    }

    override fun onEnable() {
        if (modevalue.equals("Hard")) {
            LiquidBounce.hud.addNotification(Notification("Velocity", "This mode can get you banned after 30 minutes.", NotifyType.WARNING, 100, 5000))
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook && mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }
    }

}
