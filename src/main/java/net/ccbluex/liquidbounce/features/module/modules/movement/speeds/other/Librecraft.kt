package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook


class Librecraft : SpeedMode("Librecraft") {

    private val timerBoostValue = BoolValue("TimerBoost", false)
    private val timer1 = FloatValue("Timer-1", 2.2f,1f, 4f)
    private val timer2 = FloatValue("Timer-1", 1.5f,1f, 2f)
    fun onMotion() {}
    override fun onUpdate() {
        if (!mc.thePlayer.isInWeb && !mc.thePlayer.isInLava && !mc.thePlayer.isInWater && !mc.thePlayer.isOnLadder && mc.thePlayer.ridingEntity == null) {
            if (isMoving()) {
                mc.gameSettings.keyBindJump.pressed = false
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    strafe(0.48f)
                    if (timerBoostValue.get()) {
                        if(mc.thePlayer.ticksExisted % 25 < 10) {
                            mc.timer.timerSpeed = timer1.get()
                        } else {
                            mc.timer.timerSpeed = timer2.get()
                        }
                }
                }
                strafe()
            }
        }
    }

    override fun onMove(event: MoveEvent) {}
}
