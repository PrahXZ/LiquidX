package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.TRUtils
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook

@ModuleInfo(name = "TimerRange", category = ModuleCategory.COMBAT)
class TimerRange : Module() {
    private val timerBalanceValue = FloatValue("TimerBalance", 0f, 0f, 50f)
    private val distanceToSpeedUp = FloatValue("DistanceToSpeedUp", 0f, 0f, 10f)
    private val speedValue = FloatValue("NormalSpeed", 2F, 0.1F, 10F)
    private val boostSpeedValue = FloatValue("BoostTimer", 2F, 0.1F, 10F)


    // Var
    private var balanceTimer = 0f
    private var reachedTheLimit = false


    override fun onEnable() {
        balanceTimer = timerBalanceValue.get()
        super.onEnable()
    }

    val timerRangeUtils = TRUtils()
    @EventTarget
    fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent) {
        if (balanceTimer > 0 || mc.timer.timerSpeed - 1 > 0) {
            balanceTimer += mc.timer.timerSpeed - 1
        }
        if (balanceTimer <= 0) {
            reachedTheLimit = false
        }
        if (mc.thePlayer != null) {
            if (timerRangeUtils.closestPersonsDistance() < distanceToSpeedUp.get()) {
                if (!reachedTheLimit) {
                    if (balanceTimer < timerBalanceValue.get() * 2) {
                        mc.timer.timerSpeed = boostSpeedValue.get()
                    } else {
                        reachedTheLimit = true
                        mc.timer.timerSpeed = speedValue.get()
                    }
                } else {
                    mc.timer.timerSpeed = 1f
                }
            } else {
                mc.timer.timerSpeed = speedValue.get()
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S08PacketPlayerPosLook) {
            balanceTimer = timerBalanceValue.get() * 2
            // Stops speeding up when you got flagged
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

    override val tag: String
        get() = balanceTimer.toString()
}