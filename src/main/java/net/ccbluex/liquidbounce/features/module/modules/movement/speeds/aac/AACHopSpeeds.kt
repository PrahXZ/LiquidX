package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.util.MathHelper
import kotlin.math.cos
import kotlin.math.sin
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.minecraft.block.BlockCarpet

class AACHopSpeeds : SpeedMode("AACHop") {

    private var speeds = ListValue("AACHop-Mode", arrayOf("AAC4-Bhop", "AAC4Inf", "AAC5Bhop", "AAC5Fast", "AAC6Bhop", "AAC7Bhop", "AACBhop", "AACHop3.5.0", "AACHop3.3.13"), "AAC4-Bhop")


    // Values
    private var legitHop = false
    private var legitJump = false

    override fun onEnable() {
        legitHop = true

        when (speeds.get()) {
            "AAC6Bhop" -> {
                legitJump = true
            }
            "AACHop3.5.0" -> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionX = mc.thePlayer.motionZ
                }
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f
    }


    override fun onMotion(event: MotionEvent) {
        when (speeds.get()) {
            "AACHop3.5.0" -> {
                if (event.eventState === EventState.POST && MovementUtils.isMoving() && !mc.thePlayer.isInWater && !mc.thePlayer.isInLava) {
                    mc.thePlayer.jumpMovementFactor += 0.00208f
                    if (mc.thePlayer.fallDistance <= 1f) {
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump()
                            mc.thePlayer.motionX *= 1.0118
                            mc.thePlayer.motionZ *= 1.0118
                        } else {
                            mc.thePlayer.motionY -= 0.0147
                            mc.thePlayer.motionX *= 1.00138
                            mc.thePlayer.motionZ *= 1.00138
                        }
                    }
                }
            }
        }
    }

    override fun onPreMotion() {
        when (speeds.get()) {
            "AACBhop" -> {
                if (mc.thePlayer.isInWater) return

                if (MovementUtils.isMoving()) {
                    mc.timer.timerSpeed = 1.08f
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.399
                        val f = mc.thePlayer.rotationYaw * 0.017453292f
                        mc.thePlayer.motionX -= (MathHelper.sin(f) * 0.2f).toDouble()
                        mc.thePlayer.motionZ += (MathHelper.cos(f) * 0.2f).toDouble()
                        mc.timer.timerSpeed = 2f
                    } else {
                        mc.thePlayer.motionY *= 0.97
                        mc.thePlayer.motionX *= 1.008
                        mc.thePlayer.motionZ *= 1.008
                    }
                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.timer.timerSpeed = 1f
                }
            }
        }
    }

    override fun onUpdate() {
        when (speeds.get()) {
            "AAC4Inf" -> {
                mc.timer.timerSpeed = 1.00f
                if (!MovementUtils.isMoving()) {
                    return
                }
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.timer.timerSpeed = 1.00f
                }
                if (mc.thePlayer.fallDistance > 0.7 && mc.thePlayer.fallDistance < 1.3) {
                    mc.timer.timerSpeed = 1.08f
                }
            }
            "AAC5Fast" -> {
                if (!MovementUtils.isMoving()) {
                    return
                }
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.speedInAir = 0.0201F
                    mc.timer.timerSpeed = 0.94F
                }
                if (mc.thePlayer.fallDistance > 0.7 && mc.thePlayer.fallDistance < 1.3) {
                    mc.thePlayer.speedInAir = 0.02F
                    mc.timer.timerSpeed = 1.8F
                }
            }
            "AAC6Bhop" -> {
                mc.timer.timerSpeed = 1f

                if (mc.thePlayer.isInWater) return

                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        if (legitJump) {
                            mc.thePlayer.motionY = 0.4
                            MovementUtils.strafe(0.15f)
                            mc.thePlayer.onGround = false
                            legitJump = false
                            return
                        }
                        mc.thePlayer.motionY = 0.41
                        MovementUtils.strafe(0.47458485f)
                    }

                    if (mc.thePlayer.motionY < 0 && mc.thePlayer.motionY > -0.2) mc.timer.timerSpeed =
                            (1.2 + mc.thePlayer.motionY).toFloat()

                    mc.thePlayer.speedInAir = 0.022151f
                } else {
                    legitJump = true
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "AAC7Bhop" -> {
                if (!MovementUtils.isMoving() || mc.thePlayer.ridingEntity != null || mc.thePlayer.hurtTime > 0) return

                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = 0.405
                    mc.thePlayer.motionX *= 1.004
                    mc.thePlayer.motionZ *= 1.004
                    return
                }

                val speed = MovementUtils.getSpeed() * 1.0072
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                mc.thePlayer.motionX = -sin(yaw) * speed
                mc.thePlayer.motionZ = cos(yaw) * speed
            }
            "AACHop3.3.13" -> {
                if (!MovementUtils.isMoving() || mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isOnLadder || mc.thePlayer.isRiding || mc.thePlayer.hurtTime > 0) return

                when {
                    (mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) -> {
                        val yawRad = mc.thePlayer.rotationYaw * 0.017453292f
                        mc.thePlayer.motionX -= (MathHelper.sin(yawRad) * 0.202f).toDouble()
                        mc.thePlayer.motionZ += (MathHelper.cos(yawRad) * 0.202f).toDouble()
                        mc.thePlayer.motionY = 0.405
                        LiquidBounce.eventManager.callEvent(JumpEvent(0.405f))
                        MovementUtils.strafe()
                    }

                    mc.thePlayer.fallDistance < 0.31f -> {
                        if (getBlock(mc.thePlayer.position) is BlockCarpet) { // why?
                            return
                        }

                        mc.thePlayer.jumpMovementFactor = if (mc.thePlayer.moveStrafing == 0f) 0.027f else 0.021f
                        mc.thePlayer.motionX *= 1.001
                        mc.thePlayer.motionZ *= 1.001

                        if (!mc.thePlayer.isCollidedHorizontally) mc.thePlayer.motionY -= 0.014999993
                    }

                    else -> mc.thePlayer.jumpMovementFactor = 0.02f
                }
            }
        }
    }

    override fun onTick() {
        when (speeds.get()) {
            "AAC4-Bhop" -> {
                if (MovementUtils.isMoving()) {
                    if (legitHop) {
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump()
                            mc.thePlayer.onGround = false
                            legitHop = false
                        }
                        return
                    }
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.onGround = false
                        MovementUtils.strafe(0.375f)
                        mc.thePlayer.jump()
                        mc.thePlayer.motionY = 0.41
                    } else mc.thePlayer.speedInAir = 0.0211f
                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    legitHop = true
                }
            }
            "AAC5Bhop" -> {
                mc.timer.timerSpeed = 1f

                if (mc.thePlayer.isInWater) return

                if (MovementUtils.isMoving()) {
                    when {
                        mc.thePlayer.onGround -> {
                            if (legitJump) {
                                mc.thePlayer.jump()
                                legitJump = false
                                return
                            }
                            mc.thePlayer.motionY = 0.41
                            mc.thePlayer.onGround = false
                            MovementUtils.strafe(0.374f)
                        }

                        mc.thePlayer.motionY < 0.0 -> {
                            mc.thePlayer.speedInAir = 0.0201f
                            mc.timer.timerSpeed = 1.02f
                        }

                        else -> mc.timer.timerSpeed = 1.01f
                    }
                } else {
                    legitJump = true
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }


        }
    }

}