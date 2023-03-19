package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.aac

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class AACNofall : NoFallMode("AAC") {

    private val aacmode = ListValue("AAC-Mode", arrayOf("AAC3.3.11", "AAC3.3.15","AACv4", "AAC4.4.X-Flag", "AAC5.0.14", "AAC5.0.4", "LAAC"), "AAC3.3.11")


    // AACv4
    private var aac4Fakelag = false
    private var packetModify = false
    private val aac4Packets = mutableListOf<C03PacketPlayer>()

    // AAC5.0.14
    private var aac5Check = false
    private var aac5doFlag = false
    private var aac5Timer = 0

    // AAC5.0.4
    private var isDmgFalling = false

    // LAAC
    private var jumped = false

    override fun onEnable() {
        aac4Packets.clear()
        packetModify = false
        aac4Fakelag = false

        aac5Check = false
        aac5Timer = 0
        aac5doFlag = false

        jumped = false
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        when (aacmode.get()) {
            "AACv4" -> {
                if (packet is C03PacketPlayer && aac4Fakelag) {
                    event.cancelEvent()
                    if (packetModify) {
                        packet.onGround = true
                        packetModify = false
                    }
                    aac4Packets.add(packet)
                }
            }
            "AAC4.4.X-Flag" -> {
                if (packet is S12PacketEntityVelocity && mc.thePlayer.fallDistance > 1.8) {
                    packet.motionY = (packet.motionY * -0.1).toInt()
                }
                if (packet is C03PacketPlayer) {
                    if (mc.thePlayer.fallDistance > 1.6) {
                        packet.onGround = true
                    }
                }
            }
            "AAC5.0.4" -> {
                if(packet is C03PacketPlayer) {
                    if(isDmgFalling) {
                        if (packet.onGround && mc.thePlayer.onGround) {
                            isDmgFalling = false
                            packet.onGround = true
                            mc.thePlayer.onGround = false
                            packet.y += 1.0
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y - 1.0784, packet.z, false))
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(packet.x, packet.y - 0.5, packet.z, true))
                        }
                    }
                }
            }
        }
    }

    override fun onMotion(event: MotionEvent) {
        if (aacmode.equals("AACv4")) {
            if(event.eventState == EventState.PRE) {
                if (!inVoid()) {
                    if (aac4Fakelag) {
                        aac4Fakelag = false
                        if (aac4Packets.size > 0) {
                            for (packet in aac4Packets) {
                                mc.thePlayer.sendQueue.addToSendQueue(packet)
                            }
                            aac4Packets.clear()
                        }
                    }
                    return
                }
                if (mc.thePlayer.onGround && aac4Fakelag) {
                    aac4Fakelag = false
                    if (aac4Packets.size > 0) {
                        for (packet in aac4Packets) {
                            mc.thePlayer.sendQueue.addToSendQueue(packet)
                        }
                        aac4Packets.clear()
                    }
                    return
                }
                if (mc.thePlayer.fallDistance > 2.5 && aac4Fakelag) {
                    packetModify = true
                    mc.thePlayer.fallDistance = 0f
                }
                if (inAir(4.0, 1.0)) {
                    return
                }
                if (!aac4Fakelag) {
                    aac4Fakelag = true
                }
            }
        }
    }

    override fun onNoFall(event: UpdateEvent) {
        when (aacmode.get()) {
            "AAC3.3.11" -> {
                if (mc.thePlayer.fallDistance > 2) {
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionX = mc.thePlayer.motionZ
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 10E-4, mc.thePlayer.posZ, mc.thePlayer.onGround))
                    mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                }
            }
            "AAC3.3.15" -> {
                if (mc.thePlayer.fallDistance > 2) {
                if (!mc.isIntegratedServerRunning) {
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, Double.NaN, mc.thePlayer.posZ, false))
                }
                mc.thePlayer.fallDistance = -9999f
                }
            }
            "AAC5.0.14" -> {
                var offsetYs = 0.0
                aac5Check = false
                while (mc.thePlayer.motionY - 1.5 < offsetYs) {
                    val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + offsetYs, mc.thePlayer.posZ)
                    val block = BlockUtils.getBlock(blockPos)
                    val axisAlignedBB = block!!.getCollisionBoundingBox(mc.theWorld, blockPos, BlockUtils.getState(blockPos))
                    if (axisAlignedBB != null) {
                        offsetYs = -999.9
                        aac5Check = true
                    }
                    offsetYs -= 0.5
                }
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.fallDistance = -2f
                    aac5Check = false
                }
                if (aac5Timer > 0) {
                    aac5Timer -= 1
                }
                if (aac5Check && mc.thePlayer.fallDistance > 2.5 && !mc.thePlayer.onGround) {
                    aac5doFlag = true
                    aac5Timer = 18
                } else {
                    if (aac5Timer < 2) aac5doFlag = false
                }
                if (aac5doFlag) {
                    if (mc.thePlayer.onGround) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ, true))
                    } else {
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, true))
                    }
                }
            }
            "AAC5.0.4" -> {
                if (mc.thePlayer.fallDistance > 3) {
                    isDmgFalling = true
                }
            }
            "LAAC" -> {
                if (!jumped && mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInWeb) {
                    mc.thePlayer.motionY = -6.0
                }
            }
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if (aacmode.equals("LAAC")) {
            if (mc.thePlayer.onGround) {
                jumped = false
            }

            if (mc.thePlayer.motionY > 0) {
                jumped = true
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        if (aacmode.equals("LAAC")) {
            if (!jumped && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isInWeb && mc.thePlayer.motionY < 0.0) {
                event.x = 0.0
                event.z = 0.0
            }
        }
    }

    override fun onJump(event: JumpEvent) {
        jumped = true
    }

    // AACv4
    private fun inVoid(): Boolean {
        if (mc.thePlayer.posY < 0) {
            return false
        }
        var off = 0
        while (off < mc.thePlayer.posY + 2) {
            val bb = AxisAlignedBB(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    mc.thePlayer.posX,
                    off.toDouble(),
                    mc.thePlayer.posZ
            )
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isNotEmpty()) {
                return true
            }
            off += 2
        }
        return false
    }
    private fun inAir(height: Double, plus: Double): Boolean {
        if (mc.thePlayer.posY < 0) return false
        var off = 0
        while (off < height) {
            val bb = AxisAlignedBB(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    mc.thePlayer.posX,
                    mc.thePlayer.posY - off,
                    mc.thePlayer.posZ
            )
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true
            }
            off += plus.toInt()
        }
        return false
    }
}