package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.cos
import kotlin.math.sin

class BuzzFly : FlyMode("Buzz") {

    private val timer = MSTimer()
    private var allowFly = false

    private val storedC0F = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()
    private val storedC00 = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()

    override fun onWorld(event: WorldEvent) {
        LiquidBounce.moduleManager.getModule(Fly::class.java)!!.state = false
    }

    override fun onEnable() {
        timer.reset()
        storedC0F.clear()
        storedC00.clear()
    }

    override fun onDisable() {
        allowFly = false
        while (!storedC0F.isEmpty()) {
            PacketUtils.sendPacketNoEvent(storedC0F.take())
        }
        while (!storedC00.isEmpty()) {
            PacketUtils.sendPacketNoEvent(storedC00.take())
        }
        storedC0F.clear()
        storedC00.clear()
    }

    override fun onMove(event: MoveEvent) {
        if(!allowFly) {
            event.zeroXZ()
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0FPacketConfirmTransaction) {
            storedC0F.add(packet)
            event.cancelEvent()
        }
        if (packet is C00PacketKeepAlive) {
            storedC00.add(packet)
            event.cancelEvent()
        }
        if(storedC0F.size >= 120) {
            allowFly = true
            Chat.print("Transactions: "+storedC0F.size.toString()+" + KeepAlives: "+storedC00.size.toString())
            while (!storedC0F.isEmpty()) {
                PacketUtils.sendPacketNoEvent(storedC0F.take())
            }
            while (!storedC00.isEmpty()) {
                PacketUtils.sendPacketNoEvent(storedC00.take())
            }
            //mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 11.015625, mc.thePlayer.posZ, false))
        }
    }

    override fun onMotion(event: MotionEvent) {
        if (timer.hasTimePassed(20) && allowFly) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mc.thePlayer.setPosition(mc.thePlayer.posX + (-sin(yaw) * 0.25), mc.thePlayer.posY, mc.thePlayer.posZ + (cos(yaw) * 0.25))
            timer.reset()
        }

        if(allowFly) {
            mc.thePlayer.motionY = 0.0
        }
    }
}
