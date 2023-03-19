package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.util.EnumFacing

class AACLongjump : LongJumpMode("AAC") {

    private var aacmode = ListValue("AAC-Mode", arrayOf("AACv1", "AACv2", "AACv3"), "AACv1")


    private val tpdistance = FloatValue("AACv3-TpDistance", 3f, 1f, 6f).displayable { aacmode.equals("AACv3") }
    private var teleported = false

    override fun onEnable() {
        teleported = false
    }

    override fun onUpdate(event: UpdateEvent) {
        when (aacmode.get()) {
            "AACv1" -> {
                mc.thePlayer.motionY += 0.05999
                MovementUtils.strafe(MovementUtils.getSpeed() * 1.08f)
            }
            "AACv2" -> {
                mc.thePlayer.jumpMovementFactor = 0.09f
                mc.thePlayer.motionY += 0.0132099999999999999999999999999
                mc.thePlayer.jumpMovementFactor = 0.08f
                MovementUtils.strafe()
            }
            "AACv3" -> {
                if (mc.thePlayer.fallDistance > 0.5f && !teleported) {
                    val value = tpdistance.get().toDouble()
                    var x = 0.0
                    var z = 0.0

                    when(mc.thePlayer.horizontalFacing) {
                        EnumFacing.NORTH -> z = -value
                        EnumFacing.EAST -> x = +value
                        EnumFacing.SOUTH -> z = +value
                        EnumFacing.WEST -> x = -value
                    }

                    mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z)
                    teleported = true
                }
            }
        }
    }

}