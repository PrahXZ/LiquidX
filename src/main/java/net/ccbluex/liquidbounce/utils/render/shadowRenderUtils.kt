/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
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
    
