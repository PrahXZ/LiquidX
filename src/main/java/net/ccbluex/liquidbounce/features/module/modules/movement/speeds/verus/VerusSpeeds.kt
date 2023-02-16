package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer

class VerusSpeeds: SpeedMode("Verus") {

    private val modeValue = ListValue("Verus-Mode", arrayOf("Hop", "Ground", "YPort"), "Hop")
    private val YPortspeedValue = FloatValue("YPortSpeed", 0.61f, 0.1f, 1f).displayable { modeValue.get().equals("YPort") }

    // Variables
    private var firstHop = false

    override fun onUpdate() {
        when (modeValue.get()) {
            "Hop" -> {
                if (MovementUtils.isMoving()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        MovementUtils.strafe(0.48f)
                    }
                    MovementUtils.strafe()
                }
            }
        }
    }

    override fun onPreMotion() {
        when (modeValue.get()) {
            "Ground" -> {
                if (mc.thePlayer.onGround)
                    if (modeValue.equals("Ground")) {
                        if (mc.thePlayer.ticksExisted % 12 == 0) {
                        firstHop = false
                        MovementUtils.strafe(0.69f)
                        mc.thePlayer.jump()
                        mc.thePlayer.motionY = 0.0
                        MovementUtils.strafe(0.69f)
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, false))
                        MovementUtils.strafe(0.41f)
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                    } else if (!firstHop) {
                        MovementUtils.strafe(1.01f)
                    }
                }
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        when (modeValue.get()) {
            "YPort" -> {
                if (MovementUtils.isMoving()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        mc.thePlayer.motionY = 0.0
                        MovementUtils.strafe(YPortspeedValue.get())
                        event.y = 0.41999998688698
                    } else {
                        MovementUtils.strafe()
                    }
                }
            }
        }
    }
}