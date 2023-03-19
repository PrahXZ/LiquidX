package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.abs
import kotlin.math.sqrt

class MatrixSpeeds : SpeedMode("Matrix") {

    private val speeds = ListValue("Matrix-Mode", arrayOf("MatrixHop", "MatrixHop2", "Matrix6.6.1", "Matrix6.7.0", "Matrix6.9.2"), "MatrixHop")


    private val speedMultiValue = FloatValue("Speed", 1f, 0.7f, 1.2f).displayable { speeds.equals("MatrixHop") }
    private val noTimerValue = BoolValue("NoTimer", false).displayable { speeds.equals("MatrixHop") }
    private val groundStrafe = BoolValue("GroundStrafe", false).displayable{speeds.equals("MatrixHop2")}

    private val veloBoostValue = BoolValue("VelocBoost", true).displayable { speeds.equals("Matrix6.6.1") }
    private val timerBoostValue = BoolValue("TimerBoost", false).displayable { speeds.equals("Matrix6.6.1") }
    private val usePreMotion = BoolValue("UsePreMotion", false).displayable { speeds.equals("Matrix6.6.1") }

    // Variables
    private var recX = 0.0
    private var recY = 0.0
    private var recZ = 0.0
    private var jumped = false
    private var dist = 0.0
    private var noVelocityY = 0
    private var wasTimer = false


    override fun onEnable() {
        mc.timer.timerSpeed = 1f
    }



    override fun onUpdate() {
        when (speeds.get()) {
            "MatrixHop" -> {
                fun setTimer(value: Float) {
                    if (!noTimerValue.get()) {
                        mc.timer.timerSpeed = value
                    }
                }
                if (!mc.thePlayer.onGround) {
                    setTimer(1.0f)
                }
                if (jumped) {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    jumped = false
                    setTimer(0.9f)
                    dist = sqrt((recX - mc.thePlayer.posX) * (recX - mc.thePlayer.posX) + (recZ - mc.thePlayer.posZ) * (recZ - mc.thePlayer.posZ))
                    if (MovementUtils.getSpeed() > 0) {
                        val recSpeed = MovementUtils.getSpeed()
                        mc.thePlayer.motionX *= (0.912 * dist * speedMultiValue.get()) / recSpeed
                        mc.thePlayer.motionZ *= (0.912 * dist * speedMultiValue.get()) / recSpeed
                    }
                } else if (!mc.thePlayer.onGround) {
                    mc.thePlayer.jumpMovementFactor = 0.0265f
                }
                if (mc.thePlayer.onGround && MovementUtils.isMoving() && !mc.thePlayer.isInWater) {
                    recX = mc.thePlayer.posX
                    recY = mc.thePlayer.posY
                    recZ = mc.thePlayer.posZ
                    mc.gameSettings.keyBindJump.pressed = false
                    mc.thePlayer.jump()
                    MovementUtils.strafe()
                    jumped = true
                    setTimer(2.0f)
                }
            }
            "MatrixHop2" -> {
                mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        mc.gameSettings.keyBindJump.pressed = false
                        mc.timer.timerSpeed = 1.0f
                        if (groundStrafe.get()) MovementUtils.strafe()
                        mc.thePlayer.jump()
                    }

                    if (mc.thePlayer.motionY > 0.003) {
                        mc.thePlayer.motionX *= 1.0012
                        mc.thePlayer.motionZ *= 1.0012
                        mc.timer.timerSpeed = 1.05f
                    }
                }
            }
            "Matrix6.6.1" -> {
                if (usePreMotion.get()) return
                mc.thePlayer.jumpMovementFactor = 0.0266f
                if (!mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    if (MovementUtils.getSpeed() < 0.217) {
                        MovementUtils.strafe(0.217f)
                        mc.thePlayer.jumpMovementFactor = 0.0269f
                    }
                }
                if (mc.thePlayer.motionY < 0) {
                    timer(1.09f)
                    if (mc.thePlayer.fallDistance > 1.4)
                        timer(1.0f)
                } else {
                    timer(0.95f)
                }
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    timer(1.03f)
                    mc.thePlayer.jump()
                    if (mc.thePlayer.movementInput.moveStrafe <= 0.01 && mc.thePlayer.movementInput.moveStrafe >= -0.01) {
                        MovementUtils.strafe((MovementUtils.getSpeed() * 1.0071).toFloat())
                    }
                } else if (!MovementUtils.isMoving()) {
                    timer(1.0f)
                }
                if (MovementUtils.getSpeed() < 0.22)
                    MovementUtils.strafe()
            }
            "Matrix6.7.0" -> {
                if (noVelocityY >= 0) {
                    noVelocityY -= 1
                }
                if (!mc.thePlayer.onGround && noVelocityY <= 0) {
                    if (mc.thePlayer.motionY > 0) {
                        mc.thePlayer.motionY -= 0.0005
                    }
                    mc.thePlayer.motionY -= 0.009400114514191982
                }
                if (!mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    if (MovementUtils.getSpeed() < 0.2177 && noVelocityY < 8) {
                        MovementUtils.strafe(0.2177f)
                    }
                }
                if (abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
                    mc.thePlayer.jumpMovementFactor = 0.026f
                }else{
                    mc.thePlayer.jumpMovementFactor = 0.0247f
                }
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = 0.4105000114514192
                    if (abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
                        MovementUtils.strafe()
                    }
                }
                if (!MovementUtils.isMoving()) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "Matrix6.9.2" -> {
                if (wasTimer) {
                    wasTimer = false
                    mc.timer.timerSpeed = 1.0f
                }
                mc.thePlayer.motionY -= 0.00348
                mc.thePlayer.jumpMovementFactor = 0.026f
                mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = false
                    mc.timer.timerSpeed = 1.35f
                    wasTimer = true
                    mc.thePlayer.jump()
                    MovementUtils.strafe()
                }else if (MovementUtils.getSpeed() < 0.215) {
                    MovementUtils.strafe(0.215f)
                }
            }
        }
    }

    override fun onPreMotion() {
        when (speeds.get()) {
            "Matrix6.6.1" -> {
                if (!usePreMotion.get()) return
                mc.thePlayer.jumpMovementFactor = 0.0266f
                if (!mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    if (MovementUtils.getSpeed() < 0.217) {
                        MovementUtils.strafe(0.217f)
                        mc.thePlayer.jumpMovementFactor = 0.0269f
                    }
                }
                if (mc.thePlayer.motionY < 0) {
                    timer(1.09f)
                    if (mc.thePlayer.fallDistance > 1.4)
                        timer(1.0f)
                } else {
                    timer(0.95f)
                }
                if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    timer(1.03f)
                    mc.thePlayer.jump()
                    if (mc.thePlayer.movementInput.moveStrafe <= 0.01 && mc.thePlayer.movementInput.moveStrafe >= -0.01) {
                        MovementUtils.strafe((MovementUtils.getSpeed() * 1.0071).toFloat())
                    }
                } else if (!MovementUtils.isMoving()) {
                    timer(1.0f)
                }
                if (MovementUtils.getSpeed() < 0.22)
                    MovementUtils.strafe()
            }
        }
    }

    override fun onPacket(event: PacketEvent) {

        val packet = event.packet

        when (speeds.get()) {
            "Matrix6.6.1" -> {
                if (packet is S12PacketEntityVelocity && veloBoostValue.get()) {
                    if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                        return
                    }
                    event.cancelEvent()

                    recX = packet.motionX / 8000.0
                    recZ = packet.motionZ / 8000.0
                    if (sqrt(recX * recX + recZ * recZ) > MovementUtils.getSpeed()) {
                        MovementUtils.strafe(sqrt(recX * recX + recZ * recZ).toFloat())
                        mc.thePlayer.motionY = packet.motionY / 8000.0
                    }

                    MovementUtils.strafe((MovementUtils.getSpeed() * 1.1).toFloat())
                }
            }
            "Matrix 6.7.0" -> {
                if (packet is S12PacketEntityVelocity) {
                    if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                        return
                    }
                    noVelocityY = 10
                }
            }
        }
    }

    override fun onDisable() {
        when (speeds.get()) {
            "MatrixHop" -> {
                jumped = false
                mc.timer.timerSpeed = 1f
                mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
                mc.thePlayer.jumpMovementFactor = 0.02f
            }
            "Matrix6.6.1" -> {
                mc.timer.timerSpeed = 1f
            }
            "Matrix6.7.0" -> {
                mc.timer.timerSpeed = 1f
                noVelocityY = 0
                mc.thePlayer.jumpMovementFactor = 0.02f
            }
            "Matrix6.9.2" -> {
                wasTimer = false
                mc.timer.timerSpeed = 1.0f
            }
        }
    }

    private fun timer(value: Float) {
        if(timerBoostValue.get()) {
            mc.timer.timerSpeed = value
        }
    }

}