// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.ui.cape

import net.minecraft.util.ResourceLocation

interface ICape {

    val name: String

    val cape: ResourceLocation

    fun finalize()
}