// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.*

@ModuleInfo(name = "Chams", category = ModuleCategory.RENDER)
class Chams : Module() {
    val targetsValue = BoolValue("Targets", true)
    val chestsValue = BoolValue("Chests", true)
    val itemsValue = BoolValue("Items", true)

    val localPlayerValue = BoolValue("LocalPlayer", true)
    val legacyMode = BoolValue("Legacy-Mode", false)
    val texturedValue = BoolValue("Textured", false).displayable { legacyMode.get() }
    val colorModeValue = ListValue("Color", arrayOf("Custom", "Slowly", "Fade"), "Custom").displayable { legacyMode.get() }
    val behindColorModeValue = ListValue("Behind-Color", arrayOf("Same", "Opposite", "Red"), "Red").displayable { legacyMode.get() }
    val redValue = IntegerValue("Red", 0, 0, 255).displayable { legacyMode.get() }
    val greenValue = IntegerValue("Green", 200, 0, 255).displayable { legacyMode.get() }
    val blueValue = IntegerValue("Blue", 0, 0, 255).displayable { legacyMode.get() }
    val alphaValue = IntegerValue("Alpha", 255, 0, 255).displayable { legacyMode.get() }
    val saturationValue = FloatValue("Saturation", 1F, 0F, 1F).displayable { legacyMode.get() }
    val brightnessValue = FloatValue("Brightness", 1F, 0F, 1F).displayable { legacyMode.get() }
}