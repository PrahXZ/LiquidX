/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.misc

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.event.WorldEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.features.special.AutoDisable
import net.skiddermc.fdpclient.ui.client.hud.element.elements.Notification
import net.skiddermc.fdpclient.ui.client.hud.element.elements.NotifyType
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.IntegerValue
import net.skiddermc.fdpclient.value.ListValue
import net.minecraft.event.ClickEvent
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.IChatComponent
import java.util.*
import kotlin.concurrent.schedule

@ModuleInfo(name = "AutoPlay", category = ModuleCategory.MISC)
class AutoPlay : Module() {

    private val modeValue = ListValue("Server", arrayOf("RedeSky", "BlocksMC", "Minemora", "Hypixel", "Jartex", "Pika", "HyCraft", "MineFC/HeroMC_Bedwars", "Supercraft"), "RedeSky")

    private val bwModeValue = ListValue("Mode", arrayOf("SOLO", "4v4v4v4"), "4v4v4v4").displayable { modeValue.equals("MineFC/HeroMC_Bedwars") }
    private val autoStartValue = BoolValue("AutoStartAtLobby", true).displayable { modeValue.equals("MineFC/HeroMC_Bedwars") }
    private val replayWhenKickedValue = BoolValue("ReplayWhenKicked", true).displayable { modeValue.equals("MineFC/HeroMC_Bedwars") }
    private val showGuiWhenFailedValue = BoolValue("ShowGuiWhenFailed", true).displayable { modeValue.equals("MineFC/HeroMC_Bedwars") }
    private val delayValue = IntegerValue("JoinDelay", 3, 0, 7)

    private var clicking = false
    private var queued = false
    private var clickState = 0
    private var waitForLobby = false

    override fun onEnable() {
        clickState = 0
        clicking = false
        queued = false
        waitForLobby = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (modeValue.get().lowercase()) {
            "redesky" -> {
                if (clicking && (packet is C0EPacketClickWindow || packet is C07PacketPlayerDigging)) {
                    event.cancelEvent()
                    return
                }
                if (clickState == 2 && packet is S2DPacketOpenWindow) {
                    event.cancelEvent()
                }
            }
            "hypixel" -> {
                if (clickState == 1 && packet is S2DPacketOpenWindow) {
                    event.cancelEvent()
                }
            }
        }

        if (packet is S2FPacketSetSlot) {
            val item = packet.func_149174_e() ?: return
            val windowId = packet.func_149175_c()
            val slot = packet.func_149173_d()
            val itemName = item.unlocalizedName
            val displayName = item.displayName

            when (modeValue.get().lowercase()) {
                "redesky" -> {
                    if (clickState == 0 && windowId == 0 && slot == 42 && itemName.contains("paper", ignoreCase = true) && displayName.contains("Jogar novamente", ignoreCase = true)) {
                        clickState = 1
                        clicking = true
                        queueAutoPlay {
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(6))
                            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                            clickState = 2
                        }
                    } else if (clickState == 2 && windowId != 0 && slot == 11 && itemName.contains("enderPearl", ignoreCase = true)) {
                        Timer().schedule(500L) {
                            clicking = false
                            clickState = 0
                            mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slot, 0, 0, item, 1919))
                        }
                    }
                }
                "blocksmc", "hypixel" -> {
                    if (clickState == 0 && windowId == 0 && slot == 43 && itemName.contains("paper", ignoreCase = true)) {
                        queueAutoPlay {
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(7))
                            repeat(2) {
                                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(item))
                            }
                        }
                        clickState = 1
                    }
                    if (modeValue.equals("hypixel") && clickState == 1 && windowId != 0 && itemName.equals("item.fireworks", ignoreCase = true)) {
                        mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slot, 0, 0, item, 1919))
                        mc.netHandler.addToSendQueue(C0DPacketCloseWindow(windowId))
                    }
                }
            }
        } else if (packet is S02PacketChat) {
            val text = packet.chatComponent.unformattedText
            val component = packet.chatComponent
            when (modeValue.get().lowercase()) {
                "supercraft" -> {
                    if (text.contains("Ganador: " + mc.session.username, true) || text.contains(mc.session.username + " fue asesinado", true)) {
                        queueAutoPlay {
                            mc.thePlayer.sendChatMessage("/sw leave")
                            mc.thePlayer.sendChatMessage("/sw randomjoin solo")
                        }
                    }
                    if (text.contains("El juego ya fue iniciado.", true)) {
                        FDPClient.hud.addNotification(Notification(this.name, "Failed to join, retrying...", NotifyType.ERROR, 1755))
                        queueAutoPlay {
                            mc.thePlayer.sendChatMessage("/sw leave")
                            mc.thePlayer.sendChatMessage("/sw randomjoin solo")
                        }
                    }
                }
                "minemora" -> {
                    if (text.contains("Has click en alguna de las siguientes opciones", true)) {
                        queueAutoPlay {
                            mc.thePlayer.sendChatMessage("/join")
                        }
                    }
                }
                "hycraft" -> {
                    component.siblings.forEach { sib ->
                        val clickEvent = sib.chatStyle.chatClickEvent
                        if(clickEvent != null && clickEvent.action == ClickEvent.Action.RUN_COMMAND && clickEvent.value.contains("playagain")) {
                            queueAutoPlay {
                                mc.thePlayer.sendChatMessage(clickEvent.value)
                            }
                        }
                    }
                }
                "blocksmc" -> {
                    if (clickState == 1 && text.contains("Only VIP players can join full servers!", true)) {
                        FDPClient.hud.addNotification(Notification(this.name, "Join failed! trying again...", NotifyType.WARNING, 3000))
                        // connect failed so try to join again
                        Timer().schedule(1500L) {
                            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(7))
                            repeat(2) {
                                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
                            }
                        }
                    }
                }
                "jartex" -> {
                    if (text.contains("Play Again?", true)) {
                        component.siblings.forEach { sib ->
                            val clickEvent = sib.chatStyle.chatClickEvent
                            if(clickEvent != null && clickEvent.action == ClickEvent.Action.RUN_COMMAND && clickEvent.value.startsWith("/")) {
                                queueAutoPlay {
                                    mc.thePlayer.sendChatMessage(clickEvent.value)
                                }
                            }
                        }
                    }
                }
                "pika" -> {
                    if (text.contains("Click here to play again", true)) {
                        component.siblings.forEach { sib ->
                            val clickEvent = sib.chatStyle.chatClickEvent
                            if(clickEvent != null && clickEvent.action == ClickEvent.Action.RUN_COMMAND && clickEvent.value.startsWith("/")) {
                                queueAutoPlay {
                                    mc.thePlayer.sendChatMessage(clickEvent.value)
                                }
                            }
                        }
                    }
                    if (text.contains(mc.getSession().username + " has been")) {
                        queueAutoPlay {
                            mc.thePlayer.sendChatMessage("/play skywars-normal-solo")
                        }
                    }
                }
                "hypixel" -> {
                    fun process(component: IChatComponent) {
                        val value = component.chatStyle.chatClickEvent?.value
                        if (value != null && value.startsWith("/play", true)) {
                            queueAutoPlay {
                                mc.thePlayer.sendChatMessage(value)
                            }
                        }
                        component.siblings.forEach {
                            process(it)
                        }
                    }
                    process(packet.chatComponent)
                }

                "minefc/heromc_bedwars" -> {
                    if (text.contains("Bạn đã bị loại!", false) || text.contains("đã thắng trò chơi", false)) {
                        mc.thePlayer.sendChatMessage("/bw leave")
                        waitForLobby = true
                    }
                    
                    if (
                            ( (    waitForLobby || autoStartValue.get()) && text.contains("¡Hiển thị", false) ) || 
                            ( replayWhenKickedValue.get()                && text.contains("[Anticheat] You have been kicked from the server!", false))
                       ) {
                        
                        queueAutoPlay {
                            mc.thePlayer.sendChatMessage("/bw join ${bwModeValue.get()}")
                        }
                        waitForLobby = false
                    }
                    
                    if (showGuiWhenFailedValue.get() && text.contains("giây", false) && text.contains("thất bại", false)) {
                        FDPClient.hud.addNotification(Notification(this.name, "Failed to join, showing GUI...", NotifyType.ERROR, 1000))
                        mc.thePlayer.sendChatMessage("/bw gui ${bwModeValue.get()}")
                    }
                }
                
             }
        }
    }

    private fun queueAutoPlay(delay: Long = delayValue.get().toLong() * 1000, runnable: () -> Unit) {
        if (queued) {
            return
        }
        queued = true
        AutoDisable.handleGameEnd()
        if (this.state) {
            Timer().schedule(delay) {
                queued = false
                if (state) {
                    runnable()
                }
            }
            FDPClient.hud.addNotification(Notification(this.name, "Sending you to next game in ${delayValue.get()}s...", NotifyType.INFO, delayValue.get() * 1000))
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        clicking = false
        clickState = 0
        queued = false
    }

    override val tag: String
        get() = modeValue.get()

    override fun handleEvents() = true
}