package net.ccbluex.liquidbounce.features.module.modules.movement.flys.spartan

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.minecraft.network.play.client.C03PacketPlayer

class SpartanFly : FlyMode("Spartan") {

    private var flys = ListValue("Spartan-Mode", arrayOf("Spartan", "SpartanFast", "Spartan2"), "Spartan")

    private val speedValue = FloatValue("SpartanFast-Speed", 2f, 0f, 5f).displayable { flys.equals("SpartanFast") }

    // Val
    private val timer = TickTimer()


    override fun onEnable() {
        when (flys.get()) {
            "SpartanFly" -> {
                repeat(65) {
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.049, mc.thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                }
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ, true))

                mc.thePlayer.motionX *= 0.1
                mc.thePlayer.motionZ *= 0.1
                mc.thePlayer.swingItem()
            }
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        when (flys.get()) {
            "Spartan" -> {
                fly.antiDesync = true
                mc.thePlayer.motionY = 0.0
                timer.update()
                if (timer.hasTimePassed(12)) {
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 8, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 8, mc.thePlayer.posZ, true))
                    timer.reset()
                }
            }
            "SpartanFast" -> {
                fly.antiDesync = true
                MovementUtils.resetMotion(true)
                if (mc.gameSettings.keyBindJump.isKeyDown) {
                    mc.thePlayer.motionY += speedValue.get() * 0.5
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown) {
                    mc.thePlayer.motionY -= speedValue.get() * 0.5
                }

                MovementUtils.strafe(speedValue.get())
            }
            "Spartan2" -> {
                fly.antiDesync = true
                MovementUtils.strafe(0.264f)

                if (mc.thePlayer.ticksExisted % 8 == 0) {
                    mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 10, mc.thePlayer.posZ, true))
                }
            }
        }
    }
}