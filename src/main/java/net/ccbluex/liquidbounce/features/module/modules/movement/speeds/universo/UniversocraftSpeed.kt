package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.universo

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.potion.Potion

class UniversocraftSpeed : SpeedMode("UniversoCraft") {


    var movespeed = 0.0


    override fun onEnable() {
        if (MovementUtils.isMoving() && mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            movespeed = 0.65
        } else if (MovementUtils.isMoving()) {
            movespeed = 0.52
        }
    }
    override fun onUpdate() {
        if (MovementUtils.isMoving() && mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            mc.gameSettings.keyBindJump.pressed = false
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mc.thePlayer.motionY = 0.42.toFloat().toDouble()
                movespeed -= movespeed / 156.0
                MovementUtils.strafe(movespeed.toFloat())
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
            } else {
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
            }
            MovementUtils.strafe()
        } else if (MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mc.thePlayer.motionY = 0.42.toFloat().toDouble()
                movespeed -= movespeed / 156.0
                MovementUtils.strafe(0.48f)
            } else {
            }
            MovementUtils.strafe()
        }
    }

    override fun onMove(event: MoveEvent) {
    }


    override fun onDisable() {
        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is S08PacketPlayerPosLook) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }
    }
}

