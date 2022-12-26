package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class UniversoCraftFly: FlyMode("OldUniversoFly") {
    override fun onMove(event: MoveEvent) {
        mc.timer.timerSpeed = 1f
        if(mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            event.y = 0.0
        }else {
            mc.thePlayer.motionY = 0.0;
            event.y = 0.0
            if(mc.thePlayer.ticksExisted % 12 < 10) {
                mc.timer.timerSpeed = 4.0f
            } else {
                mc.timer.timerSpeed = 4.0f
            }
            if(mc.thePlayer.ticksExisted % 18 == 9) {
                MovementUtils.strafe(MovementUtils.getSpeed())
            }
        }
    }
}