/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.exploit.ABlink
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.network.play.client.*

@ModuleInfo(name = "PacketFixer", category = ModuleCategory.MISC)
class PacketFixer : Module() {

    private val badPackets3Y = BoolValue("BadPackets 3Y", false)
    private val badPackets3A = BoolValue("BadPackets 3A", false)
    private val omniSprint13E = BoolValue("OmniSprint 13E", false)
    private val scaffold14D = BoolValue("Scaffold 14D", false)
    private val scaffold14E = BoolValue("Scaffold 14E", false)

    var x = 0.0
    var y = 0.0
    var z = 0.0
    var pitch = 0.0
    var yaw = 0.0
    var prevSlot = 0
    private var jam = 0
    private var packetCount = 0

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.thePlayer == null || mc.theWorld == null || event.isCancelled) return

        if (packet is C03PacketPlayer.C04PacketPlayerPosition) {
            this.x = packet.x
            this.y = packet.y
            this.z = packet.z
            this.jam = 0
        }

        if (packet is C03PacketPlayer.C06PacketPlayerPosLook) {
            this.x = packet.x
            this.y = packet.y
            this.z = packet.z
            this.jam = 0
        }

        if (packet is C03PacketPlayer.C06PacketPlayerPosLook) {
            this.yaw = packet.yaw.toDouble()
            this.pitch = packet.pitch.toDouble()
        }

        if(packet is C03PacketPlayer.C05PacketPlayerLook) {
            this.yaw = packet.yaw.toDouble()
            this.pitch = packet.pitch.toDouble()
        }

        if(badPackets3Y.get()) {
            if(LiquidBounce.moduleManager[Blink::class.java]!!.state || LiquidBounce.moduleManager[FreeCam::class.java]!!.state || LiquidBounce.moduleManager[ABlink::class.java]!!.state) {
                if(packet is C00PacketKeepAlive) {
                    event.cancelEvent()
                }
            }
        }

        if (badPackets3A.get() && packet is C03PacketPlayer && packet !is C03PacketPlayer.C04PacketPlayerPosition && packet !is C03PacketPlayer.C06PacketPlayerPosLook) {
            this.jam += 1
            if (this.jam >= 21) {
                this.jam = 0
                event.cancelEvent()
                mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(this.x, this.y, this.z, this.yaw.toFloat(), this.pitch.toFloat(), packet.onGround))
            }
        }

        if (omniSprint13E.get() && packet is C0BPacketEntityAction) {
            event.cancelEvent()
        }

        if (scaffold14D.get() && packet is C09PacketHeldItemChange) {
            if (packet.slotId == this.prevSlot) {
                event.cancelEvent()
            } else {
                this.prevSlot = packet.slotId
            }
        }

        if (scaffold14E.get() && packet is C08PacketPlayerBlockPlacement) {
            packet.facingX = packet.facingX.coerceIn(-1.00000F, 1.00000F)
            packet.facingY = packet.facingY.coerceIn(-1.00000F, 1.00000F)
            packet.facingZ = packet.facingZ.coerceIn(-1.00000F, 1.00000F)
        }

    }

}
