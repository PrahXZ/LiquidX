// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.features.value.Value
import net.ccbluex.liquidbounce.utils.ClientUtils

abstract class SpeedMode(val modeName: String) : MinecraftInstance() {
    protected val valuePrefix = "$modeName-"

    protected val speed: Speed
        get() = LiquidBounce.moduleManager[Speed::class.java]!!

    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    fun sendLegacy() {
        if(!speed.legacyWarningValue.get()) return

        ClientUtils.displayChatMessage("§6§lWARNING: §fThis bypass is patched or not stable for the latest anticheat!")
    }

    open fun onEnable() {}
    open fun onDisable() {}

    open fun onPreMotion() {}
    open fun onMotion(event: MotionEvent) {}
    open fun onUpdate() {}
    open fun onMove(event: MoveEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onTick() {}

    open val noJump = false
}
