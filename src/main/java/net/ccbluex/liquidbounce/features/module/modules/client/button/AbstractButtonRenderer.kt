// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.font.FontLoaders
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import java.awt.Color

abstract class AbstractButtonRenderer(protected val button: GuiButton) {
    abstract fun render(mouseX: Int, mouseY: Int, mc: Minecraft)

    open fun drawButtonText(mc: Minecraft) {
        FontLoaders.JELLO20.DisplayFonts(
            button.displayString,
            button.xPosition + button.width / 2f - FontLoaders.F18.DisplayFontWidths(FontLoaders.JELLO20,button.displayString) / 2f,
            button.yPosition + button.height / 2f - FontLoaders.F18.height / 2f,
            if (button.enabled) Color.WHITE.rgb else Color.GRAY.rgb,
            FontLoaders.JELLO20
        )
    }
}