/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.exploit.ABlink
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class UniTimerSpeed : SpeedMode("UniversoTimer") {
    private var lastABlinkPulse = "";

    override fun onEnable() {
        lastABlinkPulse = LiquidBounce.moduleManager[ABlink::class.java]!!.pulseListValue.get()
        LiquidBounce.moduleManager[ABlink::class.java]!!.pulseListValue.set("Universo")
        LiquidBounce.moduleManager[ABlink::class.java]!!.state = true
    }

    override fun onDisable() {
        LiquidBounce.moduleManager[ABlink::class.java]!!.state = false
        LiquidBounce.moduleManager[ABlink::class.java]!!.pulseListValue.set(lastABlinkPulse)
    }

    override fun onUpdate() {
        val thePlayer = mc.thePlayer ?: return

        mc.timer.timerSpeed = 1.0f

        if (!MovementUtils.isMoving() || thePlayer.isInWater || thePlayer.isInLava ||
            thePlayer.isOnLadder || thePlayer.isRiding) return

        if (thePlayer.onGround)
            thePlayer.jump()
        else {
            if (thePlayer.fallDistance <= 0.1)
                mc.timer.timerSpeed = 0.7f
            else if (thePlayer.fallDistance < 1)
                mc.timer.timerSpeed = 6f
            else
                mc.timer.timerSpeed = 1f
        }
    }

    override fun onMove(event: MoveEvent) {}
}
