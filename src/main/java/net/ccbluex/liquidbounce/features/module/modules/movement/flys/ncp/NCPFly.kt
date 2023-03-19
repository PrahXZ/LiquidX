package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer
import kotlin.math.cos
import kotlin.math.sin

class NCPFly : FlyMode("NCPFly") {

    private var flys = ListValue("NCPFly-Mode", arrayOf("NCP-Latest", "NCP-Packet", "OldNCP"), "NCP-Latest")

    // Val
    private val speedlatestValue = FloatValue("NCP-Latest-Speed", 15f, 10f, 20f).displayable { flys.equals("NCP-Latest") }
    private val timerlatestValue = FloatValue("NCP-Latest-Timer", 0.8F , 0.5f , 1.0f).displayable { flys.equals("NCP-Latest") }

    private val speedpacketValue = FloatValue("NCP-Packet-Speed", 0.28f, 0.27f, 0.29f).displayable { flys.equals("NCP-Packet") }
    private val timerpacketValue = FloatValue("NCP-Packet-Timer", 1.1F , 1.0f , 1.3f).displayable { flys.equals("NCP-Packet") }

    // Var
    private var jumped = false //NCPLatest


    override fun onEnable() {
        when (flys.get()) {
            "NCP-Latest" -> {
                if(!mc.thePlayer.onGround) {
                    fly.state = false
                } else {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = 0.2
                    jumped = true
                }
            }
            "OldNCP" -> {
                if (!mc.thePlayer.onGround) {
                    return
                }

                repeat(3) {
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.01, mc.thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                }

                mc.thePlayer.jump()
                mc.thePlayer.swingItem()
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }

    override fun onUpdate(event: UpdateEvent) {
        when (flys.get()) {
            "NCP-Latest" -> {
                mc.timer.timerSpeed = timerlatestValue.get()
                if(jumped) {
                    jumped = false
                    mc.thePlayer.motionY = -0.0784
                }
                MovementUtils.strafe(speedlatestValue.get())
            }
            "NCP-Packet" -> {
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                val x = -sin(yaw) * speedpacketValue.get()
                val z = cos(yaw) * speedpacketValue.get()
                MovementUtils.resetMotion(true)
                mc.timer.timerSpeed = timerpacketValue.get()
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.motionY, mc.thePlayer.motionZ + z, false))
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.motionY - 490, mc.thePlayer.motionZ + z, true))
                mc.thePlayer.posX += x
                mc.thePlayer.posZ += z
            }
            "OldNCP" -> {
                fly.antiDesync = true
                if (fly.launchY > mc.thePlayer.posY) {
                    mc.thePlayer.motionY = -0.000000000000000000000000000000001
                }

                if (mc.gameSettings.keyBindSneak.isKeyDown) {
                    mc.thePlayer.motionY = -0.2
                }

                if (mc.gameSettings.keyBindJump.isKeyDown && mc.thePlayer.posY < fly.launchY - 0.1) {
                    mc.thePlayer.motionY = 0.2
                }

                MovementUtils.strafe()
            }
        }
    }

}