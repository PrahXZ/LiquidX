package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class UniSpeed2 : SpeedMode("OldUniversoDEN") {

    private var wasTimer = false
    private var ticks = 0

    override fun onEnable() {
        ClientUtils.displayChatMessage("§8[§6§lUniverso§b§lDEN§8] §fEs putamente infinito usalo cuanto quieras.")
    }
    override fun onUpdate() {
        ticks++
        if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
            wasTimer = false
        }
        mc.thePlayer.jumpMovementFactor = 0.0255f
        if (!mc.thePlayer.onGround && ticks > 4 && mc.thePlayer.motionY > 0) {
            mc.thePlayer.motionY = -0.40
        }

        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (MovementUtils.getSpeed() < 0.225f && !mc.thePlayer.onGround) {
            MovementUtils.strafe(0.225f)
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            ticks = 0
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            if (!mc.thePlayer.isAirBorne) {
                return //Prevent flag with Fly
            }
            mc.timer.timerSpeed = 1.00f
            wasTimer = true
            if(MovementUtils.getSpeed() < 0.48f) {
                MovementUtils.strafe(0.48f)
            }else{
                MovementUtils.strafe((MovementUtils.getSpeed()*0.995).toFloat())
            }
        }else if (!MovementUtils.isMoving()) {
            mc.timer.timerSpeed = 1.00f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}