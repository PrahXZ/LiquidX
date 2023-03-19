package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vulcan

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class VulcanNew : FlyMode("VulcanNew") {

    var stage = 0
    var ticks = 0
    var Disabled = false
    var timer = MSTimer()

    override fun onEnable() {
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1.2, mc.thePlayer.posZ)
        MovementUtils.resetMotion(true)
        Disabled = false
        timer.reset()
    }


    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            stage = 1
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if (timer.hasTimePassed(4000)) {
            Disabled = true
        }
    }

    override fun onDisable() {
        MovementUtils.resetMotion(true)
        Disabled = false
        timer.reset()
    }

    override fun onMove(event: MoveEvent) {
        if(stage != 1) event.zeroXZ()
        event.y = 0.0
        mc.timer.timerSpeed = 1f
        MovementUtils.strafe(5f)

        if(ticks + 8 == mc.thePlayer.ticksExisted) {
        }

    }
}