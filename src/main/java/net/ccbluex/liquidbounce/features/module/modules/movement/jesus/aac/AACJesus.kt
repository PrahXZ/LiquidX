package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.aac

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

class AACJesus : JesusMode("AAC") {

    private var jesusmode = ListValue("Jesus-Mode", arrayOf("AAC", "AAC4.2.1", "AAC3.3.11", "AACFly"), "AAC")

    private val aacMotionValue = FloatValue("AACFly-Motion", 0.5f, 0.1f, 1f).displayable { jesusmode.equals("AACFly") }


    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        when (jesusmode.get()) {
            "AAC" -> {
                if (!mc.thePlayer.onGround && BlockUtils.getBlock(blockPos) === Blocks.water || mc.thePlayer.isInWater) {
                    if (!mc.thePlayer.isSprinting) {
                        mc.thePlayer.motionX *= 0.99999
                        mc.thePlayer.motionY *= 0.0
                        mc.thePlayer.motionZ *= 0.99999
                        if (mc.thePlayer.isCollidedHorizontally) {
                            mc.thePlayer.motionY = ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                        }
                    } else {
                        mc.thePlayer.motionX *= 0.99999
                        mc.thePlayer.motionY *= 0.0
                        mc.thePlayer.motionZ *= 0.99999
                        if (mc.thePlayer.isCollidedHorizontally) {
                            mc.thePlayer.motionY = ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                        }
                    }
                    if (mc.thePlayer.fallDistance >= 4) {
                        mc.thePlayer.motionY = -0.004
                    } else if (mc.thePlayer.isInWater) mc.thePlayer.motionY = 0.09
                }
                if (mc.thePlayer.hurtTime != 0) {
                    mc.thePlayer.onGround = false
                }
            }
            "AAC4.2.1" -> {
                if (!mc.thePlayer.onGround && BlockUtils.getBlock(blockPos) === Blocks.water || mc.thePlayer.isInWater) {
                    mc.thePlayer.motionY *= 0.0
                    mc.thePlayer.jumpMovementFactor = 0.08f
                    if (mc.thePlayer.fallDistance > 0) {
                        return
                    } else if (mc.thePlayer.isInWater) {
                        mc.gameSettings.keyBindJump.pressed = true
                    }
                }
            }
            "AAC3.3.11" -> {
                if (mc.thePlayer.isInWater) {
                    mc.thePlayer.motionX *= 1.17
                    mc.thePlayer.motionZ *= 1.17
                    if (mc.thePlayer.isCollidedHorizontally) {
                        mc.thePlayer.motionY = 0.24
                    } else if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ)).block !== Blocks.air) {
                        mc.thePlayer.motionY += 0.04
                    }
                }
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        when (jesusmode.get()) {
            "AACFly" -> {
                if (!mc.thePlayer.isInWater) {
                    return
                }

                event.y = aacMotionValue.get().toDouble()
                mc.thePlayer.motionY = aacMotionValue.get().toDouble()
            }
        }
    }
}