// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.*
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "BoatFly", category = ModuleCategory.MOVEMENT)
class BoatFly : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Motion", "Clip", "Velocity"), "Motion")
    private val speedValue = FloatValue("Speed", 0.3f, 0.0f, 1.0f)

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(!mc.thePlayer.isRiding) return

        val vehicle = mc.thePlayer.ridingEntity
        val x = -sin(MovementUtils.direction) * speedValue.get()
        val z = cos(MovementUtils.direction) * speedValue.get()

        when (modeValue.get().lowercase()) {
            "motion" -> {
                vehicle.motionX = x
                vehicle.motionY = (if(mc.gameSettings.keyBindJump.pressed) speedValue.get() else 0).toDouble()
                vehicle.motionZ = z
            }

            "clip" -> {
                vehicle.setPosition(vehicle.posX + x , vehicle.posY + (if (mc.gameSettings.keyBindJump.pressed) speedValue.get() else 0).toDouble() , vehicle.posZ + z)
            }

            "velocity" -> {
                vehicle.addVelocity(x, if(mc.gameSettings.keyBindJump.pressed) speedValue.get().toDouble() else vehicle.motionY, z)
            }
        }
    }
}
