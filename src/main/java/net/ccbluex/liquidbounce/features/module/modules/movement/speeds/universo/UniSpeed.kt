package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.universo

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0FPacketConfirmTransaction

class UniSpeed : SpeedMode("UniversoCraft") {
	
    private var wasTimer = false
    private var ticks = 0
    private var trans = false
    private var MTicks = 0


    override fun onEnable() {
        trans = true
    }
    override fun onUpdate() {
         ticks++
         if (wasTimer) {
            mc.timer.timerSpeed = 1.00f
            wasTimer = false
            wasTimer = false
        }
        mc.thePlayer.jumpMovementFactor = 0.0265f

        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (MovementUtils.getSpeed() < 0.265f && !mc.thePlayer.onGround) {
            MovementUtils.strafe(0.225f)
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            ticks = 0
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
	    if (!mc.thePlayer.isAirBorne) {
                return //Prevent flag with Fly
            }
            mc.timer.timerSpeed = 1.00f
            wasTimer = true
            if(MovementUtils.getSpeed() < 0.48f) {
                MovementUtils.strafe(0.48f)
            }else{
                MovementUtils.strafe((MovementUtils.getSpeed()*0.985).toFloat())
            }
        }else if (!MovementUtils.isMoving()) {
            mc.timer.timerSpeed = 1.00f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onPacket(event: PacketEvent) {
        var packet = event.packet
        if (trans) {
            if (packet is C02PacketUseEntity) {
                MTicks = 0
            }

            if (packet is C0FPacketConfirmTransaction) {
                if (MTicks > 10 && mc.thePlayer.ticksExisted % 10 != 0)
                    packet.uid = (packet.uid*-1).toShort()
                event.cancelEvent()
                PacketUtils.sendPacketNoEvent(packet)
            }
        }
    }

    override fun onDisable() {
        trans = false
    }
}
