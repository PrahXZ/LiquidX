package net.ccbluex.liquidbounce.features.special

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_RELEASE
import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_VERSION
import net.ccbluex.liquidbounce.LiquidBounce.UID
import net.ccbluex.liquidbounce.features.module.modules.client.DiscordRPCModule
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import org.json.JSONObject
import java.time.OffsetDateTime
import kotlin.concurrent.thread

object DiscordRPC {
    private val ipcClient = IPCClient(708831176907030558)
    private val timestamp = OffsetDateTime.now()
    private var running = false


    fun run() {
        ipcClient.setListener(object : IPCListener {
            override fun onReady(client: IPCClient?) {
                running = true
                thread {
                    while (running) {
                        update()
                        try {
                            Thread.sleep(1000L)
                        } catch (ignored: InterruptedException) {
                        }
                    }
                }
            }

            override fun onClose(client: IPCClient?, json: JSONObject?) {
                running = false
            }
        })
        try {
            ipcClient.connect()
        } catch (e: Exception) {
            ClientUtils.logError("DiscordRPC failed to start")
        } catch (e: RuntimeException) {
            ClientUtils.logError("DiscordRPC failed to start")
        }
    }

    private fun update() {
        val builder = RichPresence.Builder()
        val discordRPCModule = LiquidBounce.moduleManager[DiscordRPCModule::class.java]!!
        builder.setStartTimestamp(timestamp)
        builder.setLargeImage(if (discordRPCModule.animated.get()){"https://prahxz.github.io/Damar.gif"} else {"univelso2"})
        builder.setDetails("Release: $CLIENT_VERSION $CLIENT_RELEASE")
        ServerUtils.getRemoteIp().also {
            val str = (if(discordRPCModule.showServerValue.get()) "Server: $it\n" else "\n") + (if(discordRPCModule.showNameValue.get()) "Username: ${if(mc.thePlayer != null) mc.thePlayer.name else mc.session.username}\n" else "\n") + (if(discordRPCModule.showHealthValue.get()) "HP: ${mc.thePlayer.health}\n" else "\n") + (if(discordRPCModule.showOtherValue.get()) "PlayTime: ${if(mc.isSingleplayer) "SinglePlayer\n" else SessionUtils.getFormatSessionTime()} Kills: ${StatisticsUtils.getKills()} Deaths: ${StatisticsUtils.getDeaths()}\n" else "\n")
            builder.setState(if(it.equals("idling", true)) "UID: $UID" else str)
        }

        // Check ipc client is connected and send rpc
        if (ipcClient.status == PipeStatus.CONNECTED) ipcClient.sendRichPresence(builder.build())
    }

    fun stop() {
        if (ipcClient.status == PipeStatus.CONNECTED) ipcClient.close()
    }
}
