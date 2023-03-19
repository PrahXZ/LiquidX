package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer

@ModuleInfo(name = "TickTimer", category = ModuleCategory.WORLD)
class TimerTick : Module() {

    val customtick = BoolValue("Custom", false)
    val ticksvalue1 = IntegerValue("Ticks-1", 10, 0, 1000).displayable { customtick.get() }
    val timer1 = FloatValue ("Timer-1", 1f, 0.1f, 10f).displayable { customtick.get() }
    val ticksvalue2 = IntegerValue("Ticks-2", 20, 0, 1000).displayable { customtick.get() }
    val timer2 = FloatValue ("Timer-2", 2f, 0.1f, 10f).displayable { customtick.get() }
    val timervalue = FloatValue("Timer-Speed", 2f, 0.1f, 10f).displayable { !customtick.get() }
    var MSTimer = MSTimer()

    override fun onEnable() {
        MSTimer.reset()
    }

    override fun onDisable() {
        MSTimer.reset()
        mc.timer.timerSpeed = 1f
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {

        if (!customtick.get()) {
            if (MSTimer.hasTimePassed(10)) {
                mc.timer.timerSpeed = 1.0f
            }
            if (MSTimer.hasTimePassed(20)) {
                mc.timer.timerSpeed = timervalue.get()
                MSTimer.reset()
            }
        }
        if (customtick.get()) {
            if (MSTimer.hasTimePassed(ticksvalue1.get().toLong())) {
                mc.timer.timerSpeed = timer1.get()
            }
            if (MSTimer.hasTimePassed(ticksvalue2.get().toLong())) {
                mc.timer.timerSpeed = timer2.get()
                MSTimer.reset()
            }
        }
    }
}