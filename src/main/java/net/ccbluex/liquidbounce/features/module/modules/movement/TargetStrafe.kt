// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.ColorManager
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "TargetStrafe", category = ModuleCategory.PLAYER)
class TargetStrafe : Module() {
    val range = FloatValue("Range", 2.0f, 0.1f, 4.0f).displayable { !behind.get() }
    private val modeValue = ListValue("KeyMode", arrayOf("Jump", "None"), "Jump")
    private val safewalk = BoolValue("SafeWalk", true)
    val behind = BoolValue("Behind", false)
    val thirdPerson = BoolValue("ThirdPerson", false)
    val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)
    val speed = LiquidBounce.moduleManager.getModule(Speed::class.java)
    val longJump = LiquidBounce.moduleManager.getModule(LongJump::class.java)
    val flight = LiquidBounce.moduleManager.getModule(Fly::class.java)

    var direction = 1
    var lastView = 0
    var hasChangedThirdPerson = true

    override fun onEnable() {
        hasChangedThirdPerson = true
        lastView = mc.gameSettings.thirdPersonView
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {

        if (thirdPerson.get()) { // smart change back lol
            if (canStrafe) {
                if (hasChangedThirdPerson) lastView = mc.gameSettings.thirdPersonView
                mc.gameSettings.thirdPersonView = 1
                hasChangedThirdPerson = false
            } else if (!hasChangedThirdPerson) {
                mc.gameSettings.thirdPersonView = lastView
                hasChangedThirdPerson = true
            }
        }

        if (event.eventState == EventState.PRE) {
            if (mc.thePlayer.isCollidedHorizontally || safewalk.get() && checkVoid() && !flight!!.state)
                this.direction = -this.direction
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (canStrafe) {
            strafe(event, MovementUtils.getSpeed().toDouble())

            if (safewalk.get() && checkVoid() && !flight!!.state)
                event.isSafeWalk = true
        }
    }

    fun strafe(event: MoveEvent, moveSpeed: Double) {
        if (killAura?.target == null) return
        val target = killAura.target

        val rotYaw = RotationUtils.getRotationsEntity(killAura.target).yaw

        if (mc.thePlayer.getDistanceToEntity(target) <= 1.5)
            MovementUtils.setSpeed(event, moveSpeed.toInt(), rotYaw, direction.toDouble(), 0.0)
        else
            MovementUtils.setSpeed(event, moveSpeed.toInt(), rotYaw, direction.toDouble(), 1.0)

        if (behind.get()) {
            val xPos: Double = target!!.posX + -Math.sin(Math.toRadians(target.rotationYaw.toDouble())) * -2
            val zPos: Double = target.posZ + Math.cos(Math.toRadians(target.rotationYaw.toDouble())) * -2
            event.x = (moveSpeed * -MathHelper.sin(
                    Math.toRadians(RotationUtils.getRotations1(xPos, target.posY, zPos)[0].toDouble())
                            .toFloat()
            ))
            event.z = (moveSpeed * MathHelper.cos(
                    Math.toRadians(RotationUtils.getRotations1(xPos, target.posY, zPos)[0].toDouble())
                            .toFloat()
            ))
        } else {
            if (mc.thePlayer.getDistanceToEntity(target) <= range.get())
                MovementUtils.setSpeed(event, moveSpeed.toInt(), rotYaw, direction.toDouble(), 0.0)
            else
                MovementUtils.setSpeed(event, moveSpeed.toInt(), rotYaw, direction.toDouble(), 1.0)
        }
    }


    val keyMode: Boolean
        get() = when (modeValue.get()) {
            "Jump" -> Keyboard.isKeyDown(Keyboard.KEY_SPACE)
            "None" -> mc.thePlayer.movementInput.moveStrafe != 0f || mc.thePlayer.movementInput.moveForward != 0f
            else -> false
        }

    val canStrafe: Boolean
        get() = (state && (speed!!.state || flight!!.state || longJump!!.state) && killAura!!.state && killAura.target != null && !mc.thePlayer.isSneaking && keyMode && mc.gameSettings.keyBindForward.isKeyDown && !mc.gameSettings.keyBindRight.isKeyDown && !mc.gameSettings.keyBindLeft.isKeyDown && !mc.gameSettings.keyBindBack.isKeyDown)

    private fun checkVoid(): Boolean {
        for (x in -1..0) {
            for (z in -1..0) {
                if (isVoid(x, z)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isVoid(X: Int, Z: Int): Boolean {
        if (mc.thePlayer.posY < 0.0) {
            return true
        }
        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb: AxisAlignedBB = mc.thePlayer.entityBoundingBox.offset(X.toDouble(), (-off).toDouble(), Z.toDouble())
            if (mc.theWorld!!.getCollidingBoundingBoxes(mc.thePlayer as Entity, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
            off += 2
        }
        return true
    }
}
