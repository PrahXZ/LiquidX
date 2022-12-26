package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings


class UniversoIntave : SpeedMode("LegitUniverso") {

    private var wasTimer = false

    override fun onUpdate() {
        if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
        }
        if (Math.abs(mc.thePlayer.movementInput.moveStrafe) < 0.1f) {
            mc.thePlayer.jumpMovementFactor = 0.028499f
        }else {
            mc.thePlayer.jumpMovementFactor = 0.0284f
        }
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)

        if (MovementUtils.getSpeed() < 0.200f && !mc.thePlayer.onGround) {
            MovementUtils.strafe(0.000f)
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            if (!mc.thePlayer.isAirBorne) {
                mc.thePlayer.motionY = -0.1
                return //Prevent flag with Fly
            }
            mc.timer.timerSpeed = 1.00f
            wasTimer = true
            MovementUtils.strafe()
            if(MovementUtils.getSpeed() < 0.5f) {
                MovementUtils.strafe(0.4142f)
            }
        }else if (!MovementUtils.isMoving()) {
            mc.timer.timerSpeed = 1.00f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}