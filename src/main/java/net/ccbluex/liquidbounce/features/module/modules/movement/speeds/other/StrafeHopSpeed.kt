// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class StrafeHopSpeed : SpeedMode("StrafeHop") {
    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            MovementUtils.strafe()
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
