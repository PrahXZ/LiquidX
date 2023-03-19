package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.minecraft.network.play.client.C03PacketPlayer
import kotlin.math.abs

class MatrixNofall : NoFallMode("Matrix") {

    private val matrixmode = ListValue("Matrix-Mode", arrayOf("Matrix6.2.X", "Matrix6.2.X-Packet", "Matrix6.6.3", "MatrixCollide", "MatrixNew"), "MatrixNew")
    private val matrixSafe = BoolValue("SafeNoFall", true)

    // Values
    private var packet1Count = 0
    private var packetModify = false
    private var needSpoof = false
    private var matrixSend = false
    private var firstNfall = true
    private var nearGround = false
    private var matrixCanSpoof = false
    private var matrixFallTicks = 0
    private var matrixIsFall = false
    private var matrixLastMotionY = 0.0


    override fun onEnable() {
        matrixCanSpoof = false
        matrixFallTicks = 0
        matrixIsFall = false
        matrixLastMotionY = 0.0

        needSpoof = false
        packetModify = false
        packet1Count = 0

        matrixSend = false
        firstNfall = true
        nearGround = false
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onNoFall(event: UpdateEvent) {
        when (matrixmode.get()) {
            "Matrix6.2.X" -> {
                if(matrixIsFall) {
                    mc.thePlayer.motionX=0.0
                    mc.thePlayer.jumpMovementFactor=0f
                    mc.thePlayer.motionZ=0.0
                    if(mc.thePlayer.onGround) matrixIsFall = false
                }
                if(mc.thePlayer.fallDistance-mc.thePlayer.motionY>3) {
                    matrixIsFall = true
                    if(matrixFallTicks==0) matrixLastMotionY=mc.thePlayer.motionY
                    mc.thePlayer.motionY=0.0
                    mc.thePlayer.motionX=0.0
                    mc.thePlayer.jumpMovementFactor=0f
                    mc.thePlayer.motionZ=0.0
                    mc.thePlayer.fallDistance=3.2f
                    if(matrixFallTicks in 8..9) matrixCanSpoof=true
                    matrixFallTicks++
                }
                if(matrixFallTicks>12 && !mc.thePlayer.onGround) {
                    mc.thePlayer.motionY=matrixLastMotionY
                    mc.thePlayer.fallDistance = 0f
                    matrixFallTicks=0
                    matrixCanSpoof=false
                }
            }
            "Matrix6.2.X-Packet" -> {
                if(mc.thePlayer.onGround) {
                } else if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3f){
                    nofall.wasTimer = true
                    mc.timer.timerSpeed = (mc.timer.timerSpeed * if(mc.timer.timerSpeed < 0.6) { 0.25f } else { 0.5f }).coerceAtLeast(0.2f)
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(false))
                    mc.thePlayer.fallDistance = 0f
                }
            }
            "Matrix6.6.3" -> {
                val fallingPlayer = FallingPlayer(mc.thePlayer)
                val collLoc = fallingPlayer.findCollision(60) // null -> too far to calc or fall pos in void

                if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3 || (abs((collLoc?.y ?: 0) - mc.thePlayer.posY) < 3 && mc.thePlayer.fallDistance - mc.thePlayer.motionY > 2)) {
                    mc.thePlayer.fallDistance = 0.0f
                    matrixSend = true

                    if (matrixSafe.get()) {
                        mc.timer.timerSpeed = 0.3f
                        mc.thePlayer.motionX *= 0.5
                        mc.thePlayer.motionZ *= 0.5
                    } else {
                        mc.timer.timerSpeed = 0.5f
                    }
                } else {
                    mc.timer.timerSpeed = 1f
                }
            }
            "MatrixCollide" -> {
                if (mc.thePlayer.fallDistance.toInt() - mc.thePlayer.motionY > 3) {
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.fallDistance = 0.0f
                    mc.thePlayer.motionX *= 0.1
                    mc.thePlayer.motionZ *= 0.1
                    needSpoof = true
                }

                if (mc.thePlayer.fallDistance / 3 > packet1Count) {
                    packet1Count = mc.thePlayer.fallDistance.toInt() / 3
                    packetModify = true
                }
                if (mc.thePlayer.onGround) {
                    packet1Count = 0
                }
            }
        }
    }

    override fun onPacket(event: PacketEvent) {

        val packet = event.packet

        when (matrixmode.get()) {
            "Matrix6.2.X" -> {
                if(packet is C03PacketPlayer && matrixCanSpoof) {
                    packet.onGround = true
                    matrixCanSpoof = false
                }
            }
            "Matrix6.6.3" -> {
                if (packet is C03PacketPlayer && matrixSend) {
                    matrixSend = false
                    val fallingPlayer = FallingPlayer(mc.thePlayer)
                    val collLoc = fallingPlayer.findCollision(60)
                    if (abs((collLoc?.y ?: 0) - mc.thePlayer.posY) > 2) {
                        event.cancelEvent()
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y, packet.z, false))
                    }
                }
            }
            "MatrixCollide" -> {
                if(packet is C03PacketPlayer && needSpoof) {
                    packet.onGround = true
                    needSpoof = false
                }
            }
            "MatrixNew" -> {
                if(packet is C03PacketPlayer) {
                    if(!mc.thePlayer.onGround) {
                        if(mc.thePlayer.fallDistance > 2.69f){
                            mc.timer.timerSpeed = 0.3f
                            packet.onGround = true
                            mc.thePlayer.fallDistance = 0f
                        }
                        if(mc.thePlayer.fallDistance > 3.5){
                            mc.timer.timerSpeed = 0.3f
                        }else {
                            mc.timer.timerSpeed = 1F
                        }
                    }
                    if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0))
                                    .isNotEmpty()) {
                        if(!packet.isOnGround && mc.thePlayer.motionY < -0.6) {
                            packet.onGround = true
                        }
                    }

                }
            }
        }
    }
}