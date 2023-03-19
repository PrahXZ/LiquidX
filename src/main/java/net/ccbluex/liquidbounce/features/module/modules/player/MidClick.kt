// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.input.Mouse

@ModuleInfo(name = "MidClick", category = ModuleCategory.PLAYER)
class MidClick : Module() {

    private var wasDown = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.currentScreen != null) return

        if (!wasDown && Mouse.isButtonDown(2)) {
            val entity = mc.objectMouseOver.entityHit

            if (entity is EntityPlayer) {
                val playerName = stripColor(entity.getName())
                val friendsConfig = LiquidBounce.fileManager.friendsConfig

                if (!friendsConfig.isFriend(playerName)) {
                    friendsConfig.addFriend(playerName)
                    LiquidBounce.fileManager.saveConfig(friendsConfig)
                    ClientUtils.displayChatMessage("§a§l$playerName§c was added to your friends.")
                } else {
                    friendsConfig.removeFriend(playerName)
                    LiquidBounce.fileManager.saveConfig(friendsConfig)
                    ClientUtils.displayChatMessage("§b§l$playerName§c was removed from your friends.")
                }
            } else {
                ClientUtils.displayChatMessage("§4§lError: §fYou need to select a player.")
            }
        }

        wasDown = Mouse.isButtonDown(2)
    }
}