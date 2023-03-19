package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.TransferUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.sqrt

class VulcanDamage : FlyMode("VulcanDamage") {
    private val onlyDamageValue = BoolValue("${valuePrefix}-OnlyDamage", true)
    private val selfDamageValue = BoolValue("${valuePrefix}-SelfDamage", true)
    private val instantDmgValue = BoolValue("${valuePrefix}-InstantDamage", true)
    private val vanillaValue = BoolValue("${valuePrefix}-Vanilla", false)
    private val flyTimerValue = FloatValue("${valuePrefix}-Timer", 0.05f, 0.02f, 0.15f).displayable{ !vanillaValue.get() }
    private var waitFlag = false
    private var isStarted = false
    var isDamaged = false
    var dmgJumpCount = 0
    var flyTicks = 0

    private var lastSentX = 0.0
    private var lastSentY = 0.0
    private var lastSentZ = 0.0

    private var lastTickX = 0.0
    private var lastTickY = 0.0
    private var lastTickZ = 0.0


    /*
    // TODO: This fly was made by contionability, all credits to him.
    */


    fun runSelfDamageCore(): Boolean {
        mc.timer.timerSpeed = 1.0f
        if (!onlyDamageValue.get() || !selfDamageValue.get()) {
            if (onlyDamageValue.get()) {
                if (mc.thePlayer.hurtTime > 0 || isDamaged) {
                    isDamaged = true
                    dmgJumpCount = 999
                    return false
                }else {
                    return true
                }
            }
            isDamaged = true
            dmgJumpCount = 999
            return false
        }
        if (isDamaged) {
            dmgJumpCount = 999
            return false
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
        if (mc.thePlayer.onGround) {
            if (dmgJumpCount >= 4) {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                isDamaged = true
                dmgJumpCount = 999
                return false
            }
            dmgJumpCount++
            MovementUtils.resetMotion(true)
            mc.thePlayer.jump()
        }
        MovementUtils.resetMotion(false)
        return true
    }

    override fun onEnable() {
        flyTicks = 0
        waitFlag = false
        isStarted = false
        isDamaged = false
        dmgJumpCount = 0
        mc.timer.timerSpeed = 1.0f
        if (instantDmgValue.get()) {
            dmgJumpCount = 11451
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688697815, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.7531999805212, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.4199999868869781, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.7531999805212, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.0, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.419999986886978, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.7531999805212, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.00133597911214, mc.thePlayer.posZ, false))
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.00133597911214, mc.thePlayer.posZ)
            waitFlag = true
        } else {
            runSelfDamageCore()
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if (!instantDmgValue.get() && runSelfDamageCore()) {
            return
        }
        if (instantDmgValue.get() && dmgJumpCount == 11451) {
            if (!isStarted) {
                return
            } else {
                isStarted = false
                waitFlag = false
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                dmgJumpCount = 999
            }
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
        if (!isStarted && !waitFlag) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, false))
            waitFlag = true
        }
        if (isStarted) {
            if (vanillaValue.get()) {
                mc.timer.timerSpeed = 1.0f
                if (!mc.gameSettings.keyBindSneak.isKeyDown) {
                    MovementUtils.resetMotion(true)
                    if (mc.gameSettings.keyBindJump.isKeyDown) {
                        mc.thePlayer.motionY = 0.42
                    }
                }
            } else {
                mc.timer.timerSpeed = flyTimerValue.get()
                MovementUtils.resetMotion(true)
            }
            flyTicks++
            if (flyTicks > 4) {
                flyTicks = 4
            }
            MovementUtils.strafe(if (vanillaValue.get()) { 0.99f } else { 9.795f + flyTicks.toFloat() * 0.05f })
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        MovementUtils.resetMotion(true)
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer && waitFlag) {
            event.cancelEvent()
        }
        if (packet is C03PacketPlayer && (dmgJumpCount < 4 && selfDamageValue.get())) {
            packet.onGround = false
        }
        if (isStarted && vanillaValue.get()) {
            if(packet is C03PacketPlayer && (packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook)) {
                val deltaX = packet.x - lastSentX
                val deltaY = packet.y - lastSentY
                val deltaZ = packet.z - lastSentZ

                if (sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) > 10.0) {
                    PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(lastTickX, lastTickY, lastTickZ, false))
                    lastSentX = lastTickX
                    lastSentY = lastTickY
                    lastSentZ = lastTickZ
                }
                lastTickX = packet.x
                lastTickY = packet.y
                lastTickZ = packet.z
                event.cancelEvent()
            }else if(packet is C03PacketPlayer) {
                event.cancelEvent()
            }
        }
        if (packet is S08PacketPlayerPosLook && waitFlag && !vanillaValue.get()) {
            isStarted = true
            waitFlag = false
            if (instantDmgValue.get()) {
                PacketUtils.sendPacketNoEvent(
                        C06PacketPlayerPosLook(
                                packet.x,
                                packet.y,
                                packet.z,
                                packet.yaw,
                                packet.pitch,
                                false
                        )
                )
            }
            mc.timer.timerSpeed = 1.0f
            flyTicks = 0
        }else if (packet is S08PacketPlayerPosLook && vanillaValue.get()) {
            lastSentX = packet.x
            lastSentY = packet.y
            lastSentZ = packet.z
            waitFlag = false
            if (!instantDmgValue.get()) {
                event.cancelEvent()
            }
            TransferUtils.noMotionSet = true
            PacketUtils.sendPacketNoEvent(
                    C06PacketPlayerPosLook(
                            packet.x,
                            packet.y,
                            packet.z,
                            packet.yaw,
                            packet.pitch,
                            false
                    )
            )
            isStarted = true
        }
        if (packet is C0FPacketConfirmTransaction) {
            val transUID = (packet.uid).toInt()
            if (transUID >= -31767 && transUID <= -30769) {
                event.cancelEvent()
                PacketUtils.sendPacketNoEvent(packet)
            }
        }
    }
}
