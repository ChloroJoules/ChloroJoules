package io.github.chlorojoules.gui;

public enum CJMachineSlotRenderType {
	DEFAULT,
	JEWEL,
	FERTILIZER,
	PASTE,
	OTHERWORLD;

	public static CJMachineSlotRenderType fromString(String id) {
		return switch(id.toLowerCase()) {
			//case "default" -> DEFAULT;
			case "jewel" -> JEWEL;
			case "fertilizer" -> FERTILIZER;
			case "paste" -> PASTE;
			case "otherworld" -> OTHERWORLD;

			default -> DEFAULT;
		};
	}
}
