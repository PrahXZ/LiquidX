package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.universo

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class UniversocraftSpeed : SpeedMode("UniversoCraft") {
	
    private var wasTimer = false
    private var ticks = 0


    override fun onEnable() {
        mc.thePlayer.ticksExisted = 0
        sendLegacy()
    }
    override fun onUpdate() {
         ticks++
         if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
            wasTimer = false
        }
        mc.thePlayer.jumpMovementFactor = 0.0265f

        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (MovementUtils.getSpeed() < 0.265f && !mc.thePlayer.onGround) {
            MovementUtils.strafe(0.225f)
        }
        if (mc.thePlayer.onGround ) {
            ticks = 0
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
	    if (!mc.thePlayer.isAirBorne) {
                return //Prevent flag with Fly
            }
            mc.timer.timerSpeed = 1.00f
            wasTimer = true
            if(MovementUtils.getSpeed() < 0.47f) {
                MovementUtils.strafe(0.47f)
            }else{
                MovementUtils.strafe((MovementUtils.getSpeed()*0.985).toFloat())
            }
        }else if (!MovementUtils.isMoving()) {
            mc.timer.timerSpeed = 1.00f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onMove(event: MoveEvent) {
    }


    override fun onDisable() {
    }
}
