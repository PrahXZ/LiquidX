// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.api;
@FunctionalInterface
public interface FontManager {
	FontFamily fontFamily(FontType fontType);
	default FontRenderer font(FontType fontType, int size) {
		return fontFamily(fontType).ofSize(size);
	}
}
