package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.ncp

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.*

class NCPLongjump : LongJumpMode("NCP") {
    private val ncpBoostValue = FloatValue("${valuePrefix}Boost", 4.25f, 1f, 10f)
    private var canBoost = false
    override fun onEnable() {
        canBoost = true
    }
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        MovementUtils.strafe(MovementUtils.getSpeed() * if (canBoost) ncpBoostValue.get() else 1f)
        if(canBoost) canBoost = false
    }


    override fun onJump(event: JumpEvent) {
        canBoost = true
    }
}