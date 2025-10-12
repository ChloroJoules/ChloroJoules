package io.github.chlorojoules.gui;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public enum CJGuiButtonLabelKind {
	ITEM,
	FILTER;

	public static CJGuiButtonLabelKind fromString(String id) {
		return switch(id.toLowerCase()) {
			//case "item" -> ITEM;
			case "filter" -> FILTER;

			default -> ITEM;
		};
	}

	public String getTooltip(boolean state) {
		return switch(this) {
			case FILTER ->
					state ? "message.cj_blacklist" : "message.cj_whitelist";

			default -> "<invalid>";
		};
	}

	public int getX(boolean state) {
		return switch(this) {
			case FILTER -> state ? BUTTON_BLACKLIST_X : BUTTON_WHITELIST_X;

			default -> state ? BUTTON_ACTIVE_X : BUTTON_INACTIVE_X;
		};
	}

	public int getY(boolean state) {
		return switch(this) {
			case FILTER -> state ? BUTTON_BLACKLIST_Y : BUTTON_WHITELIST_Y;

			default -> state ? BUTTON_ACTIVE_Y : BUTTON_INACTIVE_Y;
		};
	}
}
