// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.ui.cape

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage

class SingleImageCape(override val name: String, val image: BufferedImage) : ICape {
    override val cape = ResourceLocation("liquidx/cape/${name.lowercase().replace(" ","_")}")

    init {
        Minecraft.getMinecraft().textureManager.loadTexture(cape, DynamicTexture(image))
    }

    override fun finalize() {
        Minecraft.getMinecraft().textureManager.deleteTexture(cape)
    }
}