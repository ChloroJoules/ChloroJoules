package io.github.chlorojoules.gui;

public enum CJMachineSlotRenderType {
	DEFAULT,
	JEWEL,
	FERTILIZER,
	PASTE;

	public static CJMachineSlotRenderType fromString(String id) {
		return switch(id.toLowerCase()) {
			case "default" -> DEFAULT;
			case "jewel" -> JEWEL;
			case "fertilizer" -> FERTILIZER;
			case "paste" -> PASTE;

			default -> DEFAULT;
		};
	}
}
