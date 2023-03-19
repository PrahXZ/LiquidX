package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import kotlin.math.cos
import kotlin.math.sin

class `AACPort-Ground-Low` : SpeedMode("AACPort-Ground-Low") {

    private var speeds = ListValue("AAC-Mode", arrayOf("AACGround", "AACGround2", "AACLowhop", "AACLowhop2", "AACPort"), "AACGround")

    private val timerValue = FloatValue("AACGround-Timer", 3f, 1.1f, 10f).displayable{speeds.equals("AACGround")}
    private val timer2Value = FloatValue("AACGround2-Timer", 3f, 1.1f, 10f).displayable{speeds.equals("AACGround2")}
    private val length = FloatValue("AACPort-Length", 1F, 1F, 20F).displayable { speeds.equals("AACPort") }

    // Values
    var legitJump = false
    var firstJump = false
    var waitForGround = false

    override fun onEnable() {
        legitJump = true
        mc.timer.timerSpeed = 1f
        firstJump = true
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onPreMotion() {
        when (speeds.get()) {
            "AACLowhop" -> {
                mc.timer.timerSpeed = 1f

                if (mc.thePlayer.isInWater) return

                if (MovementUtils.isMoving()) {
                    mc.timer.timerSpeed = 1.09f
                    if (mc.thePlayer.onGround) {
                        if (legitJump) {
                            mc.thePlayer.jump()
                            legitJump = false
                            return
                        }
                        mc.thePlayer.motionY = 0.343
                        MovementUtils.strafe(0.534f)
                    }
                } else {
                    legitJump = true
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "AACLowhop2" -> {
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.hurtTime <= 0) {
                        if (mc.thePlayer.onGround) {
                            waitForGround = false
                            if (!firstJump) firstJump = true
                            mc.thePlayer.jump()
                            mc.thePlayer.motionY = 0.41
                        } else {
                            if (waitForGround) return
                            if (mc.thePlayer.isCollidedHorizontally) return
                            firstJump = false
                            mc.thePlayer.motionY -= 0.0149
                        }
                        if (!mc.thePlayer.isCollidedHorizontally) MovementUtils.forward(if (firstJump) 0.0016 else 0.001799)
                    } else {
                        firstJump = true
                        waitForGround = true
                    }
                } else {
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionX = 0.0
                }

                val speed = MovementUtils.getSpeed().toDouble()
                mc.thePlayer.motionX = -(sin(MovementUtils.direction) * speed)
                mc.thePlayer.motionZ = cos(MovementUtils.direction) * speed
            }
        }
    }

    override fun onUpdate() {
        when (speeds.get()) {
            "AACGround" -> {
                if (!MovementUtils.isMoving()) return

                mc.timer.timerSpeed = timerValue.get()

                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            }
            "AACGround2" -> {
                if (!MovementUtils.isMoving()) return

                mc.timer.timerSpeed = timer2Value.get()

                MovementUtils.strafe(0.02f)
            }
            "AACPort" -> {
                if (!MovementUtils.isMoving()) return

                val f = mc.thePlayer.rotationYaw * 0.017453292f
                var d = 0.2
                while (d <= length.get()) {
                    val x = mc.thePlayer.posX - MathHelper.sin(f) * d
                    val z = mc.thePlayer.posZ + MathHelper.cos(f) * d

                    if (mc.thePlayer.posY < mc.thePlayer.posY.toInt() + 0.5 && BlockUtils.getBlock(BlockPos(x, mc.thePlayer.posY, z)) !is BlockAir) {
                        break
                    }

                    mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(x, mc.thePlayer.posY, z, true))
                    d += 0.2
                }
            }
        }
    }

}