// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.Breadcrumbs
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import org.lwjgl.opengl.GL11
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "Blink", category = ModuleCategory.PLAYER)
class Blink : Module() {

    private val inboundValue = BoolValue("Inbound", false)
    private val outboundValue = BoolValue("Outbound", true)
    private val UniversoCraftFix = BoolValue("UniversoFix", false)
    private val pulseValue = BoolValue("Pulse", false)
    private val pulseDelayValue = IntegerValue("PulseDelay", 1000, 500, 5000).displayable { pulseValue.get() }

    private val pulseTimer = MSTimer()
    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayServer>>()
    private var fakePlayer: EntityOtherPlayerMP? = null
    private var disableLogger = false
    private val positions = LinkedList<DoubleArray>()

    override fun onEnable() {
        if (mc.thePlayer == null) return
        if (!pulseValue.get()) {
            fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
            fakePlayer!!.clonePlayer(mc.thePlayer, true)
            fakePlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
            fakePlayer!!.rotationYawHead = mc.thePlayer.rotationYawHead
            mc.theWorld.addEntityToWorld(-1337, fakePlayer)
        }
        synchronized(positions) {
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight() / 2, mc.thePlayer.posZ))
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ))
        }
        pulseTimer.reset()
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return
        blink()
        if (fakePlayer != null) {
            mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
            fakePlayer = null
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (UniversoCraftFix.get()) {
            if(packet is C00PacketKeepAlive) {
                event.cancelEvent()
            }
        }



        if (mc.thePlayer == null || disableLogger) return
        if (packet is C03PacketPlayer) { // Cancel all movement stuff
            event.cancelEvent()
        }
        if (packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook ||
            packet is C08PacketPlayerBlockPlacement ||
            packet is C0APacketAnimation ||
            packet is C0BPacketEntityAction || packet is C02PacketUseEntity) {
            event.cancelEvent()
            packets.add(packet as Packet<INetHandlerPlayServer>)
        }
        if (packet is S08PacketPlayerPosLook && inboundValue.get()) event.cancelEvent()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        synchronized(positions) {
            positions.add(
                doubleArrayOf(
                    mc.thePlayer.posX,
                    mc.thePlayer.entityBoundingBox.minY,
                    mc.thePlayer.posZ
                )
            )
        }
        if (pulseValue.get() && pulseTimer.hasTimePassed(pulseDelayValue.get().toLong())) {
            blink()
            pulseTimer.reset()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val breadcrumbs = LiquidBounce.moduleManager[Breadcrumbs::class.java]!!
        synchronized(positions) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glLineWidth(2F)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            RenderUtils.glColor(breadcrumbs.color)
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ
            for (pos in positions) GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }

    override val tag: String
        get() = packets.size.toString()

    private fun blink() {
        try {
            disableLogger = true
            while (!packets.isEmpty()) {
                mc.netHandler.addToSendQueue(packets.take())
            }
            disableLogger = false
        } finally {
            disableLogger = false
        }
        synchronized(positions) { positions.clear() }
    }
}
