package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.*

class ZoneSpeed : SpeedMode("ZoneCraftFast") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.9f, 0.1f, 2f)

    override fun onMove(event: MoveEvent) {
        if (MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump() // sproof jump
                mc.thePlayer.motionY = 0.0
                MovementUtils.strafe(speedValue.get())
                event.y = 0.12999998688698
            } else {
                MovementUtils.strafe()
            }
        }
    }
}