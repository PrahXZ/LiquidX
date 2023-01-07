// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.utils.item

import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

class ArmorPiece(val itemStack: ItemStack, val slot: Int) {
    val armorType = (itemStack.item as ItemArmor).armorType
}