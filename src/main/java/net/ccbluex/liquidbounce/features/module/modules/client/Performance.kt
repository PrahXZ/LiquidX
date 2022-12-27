/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue

@ModuleInfo(name = "Performance", category = ModuleCategory.CLIENT, defaultOn = true, canEnable = false, array = false)
object Performance : Module() {
    @JvmField
    var staticParticleColorValue = BoolValue("StaticParticleColor", true)
    @JvmField
    var fastEntityLightningValue = BoolValue("FastEntityLightning", true)
    @JvmField
    var fastBlockLightningValue = BoolValue("FastBlockLightning", true)
}

