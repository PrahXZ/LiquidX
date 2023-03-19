package net.ccbluex.liquidbounce.features.module.modules.movement.flys.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB
import kotlin.math.cos
import kotlin.math.sin

class AACFly : FlyMode("AACFly") {

    private var flys = ListValue("AACFly-Mode", arrayOf("AAC1.9.10", "AAC3.0.5", "AAC5.2.0", "AAC5.2.0-Vanilla"), "AAC1.9.10")

    // Val
    private val speedAAC1910Value = FloatValue("AAC1.9.10-Speed", 0.3f, 0.2f, 1.7f).displayable { flys.equals("AAC1.9.10") }
    private val fastAAC305Value = BoolValue("AAC3.0.5-Fast", true).displayable { flys.equals("AAC.3.0.5") }


    private val speedValue = FloatValue("AAC5.2.0-Speed", 2f, 0f, 5f).displayable { flys.equals("AAC5.2.0-Vanilla") }
    private val smoothValue = BoolValue("AAC5.2.0-Smooth", false).displayable { flys.equals("AAC5.2.0-Vanilla") }
    private val purseValue = IntegerValue("AAC5.2.0-Purse", 7, 3, 20).displayable { flys.equals("AAC5.2.0-Vanilla") }
    private val packetModeValue = ListValue("AAC5.2.0-PacketMode", arrayOf("Old", "Rise"), "Old").displayable { flys.equals("AAC5.2.0-Vanilla") }
    private val useC04Value = BoolValue("AAC5.2.0-UseC04", false).displayable { flys.equals("AAC5.2.0-Vanilla") }


    // Var
    private var aacJump = 0.0
    private var delay = 0

    private val packets = mutableListOf<C03PacketPlayer>()
    private val timer = MSTimer()
    private var nextFlag = false
    private var flyClip = false
    private var flyStart = false


    override fun onEnable() {
        aacJump = -3.8

        when (flys.get()) {
            "AAC5.2.0" -> {
                if (mc.isSingleplayer) {
                    LiquidBounce.hud.addNotification(Notification("Fly", "Use AAC5.2.0 Fly will crash single player", NotifyType.ERROR, 2000, 500))
                    fly.state = false
                    return
                }
                MovementUtils.resetMotion(true)
                PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
            }
            "AAC5.2.0-Vanilla" -> {
                if (mc.isSingleplayer) {
                    LiquidBounce.hud.addNotification(Notification("Fly", "Use AAC5.2.0 Fly will crash single player", NotifyType.ERROR, 2000, 500))
                    fly.state = false
                    return
                }
                packets.clear()
                nextFlag = false
                flyClip = false
                flyStart = false
                timer.reset()
                if (smoothValue.get()) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ)
                }
            }
        }
    }

    override fun onDisable() {
        when (flys.get()) {
            "AAC5.2.0-Vanilla" -> {
                sendPackets()
                packets.clear()
                mc.thePlayer.noClip = false
            }
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        when (flys.get()) {
            "AAC1.9.10" -> {
                if (mc.gameSettings.keyBindJump.isKeyDown) aacJump += 0.2

                if (mc.gameSettings.keyBindSneak.isKeyDown) aacJump -= 0.2

                if (fly.launchY + aacJump > mc.thePlayer.posY) {
                    mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                    mc.thePlayer.motionY = 0.8
                    MovementUtils.strafe(speedAAC1910Value.get())
                }

                MovementUtils.strafe()
            }
            "AAC3.0.5" -> {
                if (delay == 2) {
                    mc.thePlayer.motionY = 0.1
                } else if (delay > 2) {
                    delay = 0
                }

                if (fastAAC305Value.get()) {
                    if (mc.thePlayer.movementInput.moveStrafe.toDouble() == 0.0) mc.thePlayer.jumpMovementFactor =
                            0.08f else mc.thePlayer.jumpMovementFactor = 0f
                }

                delay++
            }
            "AAC5.2.0" -> {
                mc.thePlayer.motionY = 0.003
                MovementUtils.resetMotion(false)
            }
            "AAC5.2.0-Vanilla" -> {
                fly.antiDesync = true
                mc.thePlayer.noClip = !MovementUtils.isMoving()
                if (smoothValue.get()) {
                    if (!timer.hasTimePassed(1000) || !flyStart) {
                        MovementUtils.resetMotion(true)
                        mc.thePlayer.jumpMovementFactor = 0.00f
                        mc.timer.timerSpeed = 0.32F
                        return
                    } else {
                        if (!flyClip) {
                            mc.timer.timerSpeed = 0.19F
                        } else {
                            flyClip = false
                            mc.timer.timerSpeed = 1.2F
                        }
                    }
                }

                mc.thePlayer.capabilities.isFlying = false
                MovementUtils.resetMotion(true)
                if (mc.gameSettings.keyBindJump.isKeyDown) {
                    mc.thePlayer.motionY += speedValue.get() * 0.5
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown) {
                    mc.thePlayer.motionY -= speedValue.get() * 0.5
                }
                MovementUtils.strafe(speedValue.get())
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (flys.get()) {
            "AAC5.2.0" -> {
                if (packet is S08PacketPlayerPosLook) {
                    event.cancelEvent()
                    mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, packet.yaw, packet.pitch, false))
                    val dist = 0.14
                    val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                    mc.thePlayer.setPosition(mc.thePlayer.posX + -sin(yaw) * dist, mc.thePlayer.posY, mc.thePlayer.posZ + cos(yaw) * dist)
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
                } else if (packet is C03PacketPlayer) {
                    event.cancelEvent()
                }
            }
            "AAC5.2.0-Vanilla" -> {
                if (packet is S08PacketPlayerPosLook) {
                    flyStart = true
                    if (timer.hasTimePassed(2000)) {
                        flyClip = true
                        mc.timer.timerSpeed = 1.3F
                    }
                    nextFlag = true
                } else if (packet is C03PacketPlayer) {
                    val f = mc.thePlayer.width / 2.0
                    if(packet.y < 1145.14001919810) {
                        if (mc.theWorld.checkBlockCollision(AxisAlignedBB(packet.x - f, packet.y, packet.z - f, packet.x + f, packet.y + mc.thePlayer.height, packet.z + f))) {
                            return
                        }
                        packets.add(packet)
                        nextFlag = false
                        event.cancelEvent()
                        if (!(smoothValue.get() && !timer.hasTimePassed(1000)) && packets.size > purseValue.get()) {
                            sendPackets()
                        }
                    }
                }
            }
        }
    }

    private fun sendPackets() {
        var yaw = mc.thePlayer.rotationYaw
        var pitch = mc.thePlayer.rotationPitch
        if (packetModeValue.get() == "Old") {
            for (packet in packets) {
                if (packet.isMoving) {
                    PacketUtils.sendPacketNoEvent(packet)
                    if (packet.getRotating()) {
                        yaw = packet.yaw
                        pitch = packet.pitch
                    }
                    if (useC04Value.get()) {
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(packet.x, 1e+308, packet.z, true))
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(packet.x, 1e+308, packet.z, yaw, pitch, true))
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }
                }
            }
        } else {
            for (packet in packets) {
                if (packet.isMoving) {
                    PacketUtils.sendPacketNoEvent(packet)
                    if (packet.getRotating()) {
                        yaw = packet.yaw
                        pitch = packet.pitch
                    }
                    if (useC04Value.get()) {
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(packet.x, -1e+159, packet.z + 10, true))
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(packet.x, -1e+159, packet.z + 10, yaw, pitch, true))
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }
                }
            }
        }
        packets.clear()
    }

}