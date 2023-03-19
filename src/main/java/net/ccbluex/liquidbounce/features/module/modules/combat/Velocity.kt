// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@ModuleInfo(name = "Velocity", category = ModuleCategory.COMBAT)
class Velocity : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.velocitys", VelocityMode::class.java)
        .map { it.newInstance() as VelocityMode }
        .sortedBy { it.modeName }

    private val mode: VelocityMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Cancel") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    val horizontalValue = FloatValue("Horizontal", 0F, -2F, 2F)
    val verticalValue = FloatValue("Vertical", 0F, -2F, 2F)
    val velocityTickValue = IntegerValue("VelocityTick", 1, 0, 10).displayable { modeValue.equals("Tick") || modeValue.equals("OldSpartan")}
    val onlyGroundValue = BoolValue("OnlyGround", false)
    val onlyCombatValue = BoolValue("OnlyCombat", false)
    // private val onlyHitVelocityValue = BoolValue("OnlyHitVelocity",false)
    private val noFireValue = BoolValue("noFire", false)

    private val overrideDirectionValue = ListValue("OverrideDirection", arrayOf("None", "Hard", "Offset"), "None")
    private val overrideDirectionYawValue = FloatValue("OverrideDirectionYaw", 0F, -180F, 180F)
        .displayable { !overrideDirectionValue.equals("None") }

    val velocityTimer = MSTimer()
    var wasTimer = false
    var velocityInput = false
    var velocityTick = 0

    var antiDesync = false

    var needReset = true

    override fun onEnable() {
        antiDesync = false
        needReset = true
        mode.onEnable()
    }

    override fun onDisable() {
        antiDesync = false
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.capabilities.flySpeed = 0.05f
        mc.thePlayer.noClip = false

        mc.timer.timerSpeed = 1F
        mc.thePlayer.speedInAir = 0.02F

        mode.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mode.onUpdate(event)
        if (wasTimer) {
            mc.timer.timerSpeed = 1f
            wasTimer = false
        }
        if(velocityInput) {
            velocityTick++
        }else velocityTick = 0

        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb) {
            return
        }

        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }
        if (noFireValue.get() && mc.thePlayer.isBurning) return
        mode.onVelocity(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        mode.onMotion(event)
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent){
        mode.onStrafe(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }

        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            // if(onlyHitVelocityValue.get() && packet.getMotionY()<400.0) return
            if (noFireValue.get() && mc.thePlayer.isBurning) return
            velocityTimer.reset()
            velocityTick = 0

            if (!overrideDirectionValue.equals("None")) {
                val yaw = Math.toRadians(
                    if (overrideDirectionValue.get() == "Hard") {
                        overrideDirectionYawValue.get()
                    } else {
                        mc.thePlayer.rotationYaw + overrideDirectionYawValue.get() + 90
                    }.toDouble()
                )
                val dist = sqrt((packet.motionX * packet.motionX + packet.motionZ * packet.motionZ).toDouble())
                val x = cos(yaw) * dist
                val z = sin(yaw) * dist
                packet.motionX = x.toInt()
                packet.motionZ = z.toInt()
            }

            mode.onVelocityPacket(event)
        }

    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        mode.onWorld(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        mode.onMove(event)
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        mode.onBlockBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        mode.onJump(event)
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        mode.onStep(event)
    }
    override val tag: String
        get() = if (modeValue.get() == "Simple")
            "${horizontalValue.get()}% ${verticalValue.get()}%"
        else
            modeValue.get()

    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
            val displayableFunction = value.displayableFunction
            it.add(value.displayable { displayableFunction.invoke() && modeValue.equals(mode.modeName) })
            }
        }
    }
}
