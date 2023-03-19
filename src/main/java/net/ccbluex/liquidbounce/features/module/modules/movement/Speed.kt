// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.movement

import com.sun.org.apache.xpath.internal.operations.Bool
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.*
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "Speed", category = ModuleCategory.MOVEMENT, autoDisable = EnumAutoDisableType.FLAG, keyBind = Keyboard.KEY_V)
class Speed : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.speeds", SpeedMode::class.java)
            .map { it.newInstance() as SpeedMode }
            .sortedBy { it.modeName }

    private val mode: SpeedMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "NCPBhop") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val noWater = BoolValue("NoWater", true)
    private val debug = BoolValue("Debug", false)
    private val hidejumps = BoolValue("Hide Jumps", false)
    val legacyWarningValue = BoolValue("LegacyWarn", true)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isSneaking || (mc.thePlayer.isInWater && noWater.get())) return
        if (MovementUtils.isMoving()) mc.thePlayer.isSprinting = true
        mode.onUpdate()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (debug.get()) {
            alert("Speed: " + MovementUtils.getSpeed().toString())
            alert("YMotion: " + mc.thePlayer.motionY.toString())
        }
        
        if (MovementUtils.isMoving()) {
            mc.thePlayer.isSprinting = true
        }

        mode.onMotion(event)

        if (mc.thePlayer.isSneaking || event.eventState !== EventState.PRE || (mc.thePlayer.isInWater && noWater.get())) {
            return
        }


        if (hidejumps.get()) {
            mc.thePlayer.cameraYaw = 0.0F
        }

        mode.onPreMotion()
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (mc.thePlayer.isSneaking || (mc.thePlayer.isInWater && noWater.get())) {
            return
        }

        mode.onMove(event)
//        LiquidBounce.moduleManager[TargetStrafe::class.java]!!.doMove(event)
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if (mc.thePlayer.isSneaking || (mc.thePlayer.isInWater && noWater.get())) {
            return
        }

        mode.onTick()
    }
    
    @EventTarget
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mode.noJump) {
            event.cancelEvent()
        }
    }

    override fun onEnable() {
        if (mc.thePlayer == null) return
        mc.timer.timerSpeed = 1f
        mode.onEnable()
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return
        mc.timer.timerSpeed = 1f
        mode.onDisable()
    }

    override val tag: String
        get() = modeValue.get()

    /**
     * 读取mode中的value并和本体中的value合并
     * 所有的value必须在这个之前初始化
      */
    override val values = super.values.toMutableList().also {
        modes.map { mode ->
            mode.values.forEach { value ->
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.equals(mode.modeName) })
            }
        }
    }
}
