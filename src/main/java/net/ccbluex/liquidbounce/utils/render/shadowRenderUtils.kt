// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.utils.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glEnable
import net.minecraft.client.renderer.GlStateManager

object shadowRenderUtils {
    @JvmStatic
    private val glCapMap: MutableMap<Int, Boolean> = HashMap()

    @JvmStatic
    var deltaTime = 0

    @JvmStatic
    private val DISPLAY_LISTS_2D = IntArray(4)
}
    
