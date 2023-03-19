package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.*
@ModuleInfo(name = "Rotations", category = ModuleCategory.CLIENT, canEnable = false)
object Rotations : Module() {
    val headValue = BoolValue("Head", true)
    val bodyValue = BoolValue("Body", true)
    val fixedValue = ListValue("SensitivityFixed", arrayOf("None", "Old", "New"), "New")
    val nanValue = BoolValue("NaNCheck", true)
}