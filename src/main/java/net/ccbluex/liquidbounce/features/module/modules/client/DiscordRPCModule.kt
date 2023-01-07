// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.special.DiscordRPC
import net.ccbluex.liquidbounce.features.value.*

@ModuleInfo(name = "DiscordRPC", category = ModuleCategory.CLIENT,  defaultOn = true)
class DiscordRPCModule : Module() {
    val showServerValue = BoolValue("ShowServer", true)
    val showNameValue = BoolValue("ShowName", false)
    val showHealthValue = BoolValue("ShowHealth", false)
    val showOtherValue = BoolValue("ShowOther", false)
    val animated = BoolValue("ShouldAnimate?", true)

    override fun onEnable() {
        DiscordRPC.run()
    }

    override fun onDisable() {
        DiscordRPC.stop()
    }
}
