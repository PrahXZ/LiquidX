// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "SoundModules", category = ModuleCategory.CLIENT, canEnable = false)
object SoundModule : Module() {
    val toggleIgnoreScreenValue = BoolValue("ToggleIgnoreScreen", false)
    private val toggleSoundValue = ListValue("ToggleSound", arrayOf("None", "Click", "Custom"), "Click")

    fun playSound(enable: Boolean) {
        when (toggleSoundValue.get().lowercase()) {
            "click" -> {
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.click"), 1F))
            }

            "custom" -> {
                if (enable) {
                    LiquidBounce.tipSoundManager.enableSound.asyncPlay()
                } else {
                    LiquidBounce.tipSoundManager.disableSound.asyncPlay()
                }
            }
        }
    }
}