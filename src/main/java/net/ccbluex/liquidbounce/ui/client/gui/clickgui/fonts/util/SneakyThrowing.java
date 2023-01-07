// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.util;

public final class SneakyThrowing {

	public static RuntimeException sneakyThrow(Throwable throwable) {
		return sneakyThrow0(throwable);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T sneakyThrow0(Throwable throwable) throws T {
		throw (T) throwable;
	}

	private SneakyThrowing() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
