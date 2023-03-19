// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.exploit.ABlink
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.*

@ModuleInfo(name = "Timer", category = ModuleCategory.WORLD, autoDisable = EnumAutoDisableType.RESPAWN)
class Timer : Module() {

    private val speedValue = FloatValue("Speed", 2F, 0.1F, 24F)
    private val verusValue = BoolValue("Verus", false);
    private val onMoveValue = BoolValue("OnMove", true)

    override fun onEnable() {
        if(verusValue.get()) LiquidBounce.moduleManager[ABlink::class.java]!!.state = true
    }

    override fun onDisable() {
        if (mc.thePlayer == null) {
            return
        }

        mc.timer.timerSpeed = 1F
        if(verusValue.get())  LiquidBounce.moduleManager[ABlink::class.java]!!.state = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (MovementUtils.isMoving() || !onMoveValue.get()) {
            mc.timer.timerSpeed = speedValue.get()
            return
        }

        mc.timer.timerSpeed = 1F
    }

    override val tag: String?
        get() = "${speedValue.get().toString()}"
}
