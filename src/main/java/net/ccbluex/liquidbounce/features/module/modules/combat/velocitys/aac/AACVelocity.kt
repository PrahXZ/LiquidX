package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

class AACVelocity : VelocityMode("AAC") {

    private val velocitys = ListValue("AAC-Mode", arrayOf("AACZero", "AACPush", "AAC4Reduce", "AAC5Reduce", "AAC5.2.0-Combat", "AAC5.2.0"), "AACZero")


    // AACPush
    private val aacPushXZReducerValue = FloatValue("AACPushXZReducer", 2F, 1F, 3F).displayable { velocitys.equals("AACPush") }
    private val aacPushYReducerValue = BoolValue("AACPushYReducer", true).displayable { velocitys.equals("AACPush") }
    private var jump = false

    // AAC5Combat
    private var templateX = 0
    private var templateY = 0
    private var templateZ = 0


    override fun onEnable() {
        jump = false
        templateX = 0
        templateY = 0
        templateZ = 0

    }

    override fun onVelocity(event: UpdateEvent) {
        when (velocitys.get()) {
            "AACZero" -> {
                if (mc.thePlayer.hurtTime > 0) {
                    if (!velocity.velocityInput || mc.thePlayer.onGround || mc.thePlayer.fallDistance > 2F) {
                        return
                    }
                    mc.thePlayer.addVelocity(0.0, -1.0, 0.0)
                    mc.thePlayer.onGround = true
                } else {
                    velocity.velocityInput = false
                }
            }
            "AACPush" -> {
                if (jump) {
                    if (mc.thePlayer.onGround) {
                        jump = false
                    }
                } else {
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0) {
                        mc.thePlayer.onGround = true
                    }
                    if (mc.thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get() &&
                            !LiquidBounce.moduleManager[Speed::class.java]!!.state) {
                        mc.thePlayer.motionY -= 0.014999993
                    }
                }
                if (mc.thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducerValue.get()

                    mc.thePlayer.motionX /= reduce
                    mc.thePlayer.motionZ /= reduce
                }
            }
            "AAC4Reduce" -> {
                if (mc.thePlayer.hurtTime> 0 && !mc.thePlayer.onGround && velocity.velocityInput && velocity.velocityTimer.hasTimePassed(80L)) {
                    mc.thePlayer.motionX *= 0.62
                    mc.thePlayer.motionZ *= 0.62
                }
                if (velocity.velocityInput && (mc.thePlayer.hurtTime <4 || mc.thePlayer.onGround) && velocity.velocityTimer.hasTimePassed(120L)) {
                    velocity.velocityInput = false
                }
            }
            "AAC5Reduce" -> {
                if (mc.thePlayer.hurtTime> 1 && velocity.velocityInput) {
                    mc.thePlayer.motionX *= 0.81
                    mc.thePlayer.motionZ *= 0.81
                }
                if (velocity.velocityInput && (mc.thePlayer.hurtTime <5 || mc.thePlayer.onGround) && velocity.velocityTimer.hasTimePassed(120L)) {
                    velocity.velocityInput = false
                }
            }
            "AAC5.2.0-Combat" -> {
                if (mc.thePlayer.hurtTime> 0 && velocity.velocityInput) {
                    velocity.velocityInput = false
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.jumpMovementFactor = -0.002f
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
                }
                if (velocity.velocityTimer.hasTimePassed(80L) && velocity.velocityInput) {
                    velocity.velocityInput = false
                    mc.thePlayer.motionX = templateX / 8000.0
                    mc.thePlayer.motionZ = templateZ / 8000.0
                    mc.thePlayer.motionY = templateY / 8000.0
                    mc.thePlayer.jumpMovementFactor = -0.002f
                }
            }
        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet


        when (velocitys.get()) {
            "AACZero" -> {
                velocity.velocityInput = true
            }
            "AAC4Reduce" -> {
                if(packet is S12PacketEntityVelocity) {
                    velocity.velocityInput = true
                    packet.motionX = (packet.getMotionX() * 0.6).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                }
            }
            "AAC5Reduce" -> {
                velocity.velocityInput = true
            }
            "AAC5.2.0-Combat" -> {
                if(packet is S12PacketEntityVelocity) {
                    event.cancelEvent()
                    velocity.velocityInput = true
                    templateX = packet.motionX
                    templateZ = packet.motionZ
                    templateY = packet.motionY
                }
            }
            "AAC5.2.0" -> {
                if (packet is S12PacketEntityVelocity) {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
                }
            }
        }

    }

    override fun onJump(event: JumpEvent) {
        when (velocitys.get()) {
            "AACZero" -> {
                if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || (velocity.onlyGroundValue.get() && !mc.thePlayer.onGround)) {
                    return
                }

                if ((velocity.onlyGroundValue.get() && !mc.thePlayer.onGround) || (velocity.onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
                    return
                }

                if (mc.thePlayer.hurtTime > 0) {
                    event.cancelEvent()
                }
            }
            "AACPush" -> {
                if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || (velocity.onlyGroundValue.get() && !mc.thePlayer.onGround)) {
                    return
                }

                if ((velocity.onlyGroundValue.get() && !mc.thePlayer.onGround) || (velocity.onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
                    return
                }

                jump = true

                if (!mc.thePlayer.isCollidedVertically) {
                    event.cancelEvent()
                }
            }
        }
    }
}