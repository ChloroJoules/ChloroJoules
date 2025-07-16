package io.github.chlorojoules.gui;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public enum CJGuiGravity {
	TOP,
	TOP_LEFT,
	TOP_RIGHT,

	BOTTOM,
	BOTTOM_LEFT,
	BOTTOM_RIGHT,

	LEFT,
	RIGHT,

	CENTER;

	public static CJGuiGravity fromString(String id) {
		return switch(id.toLowerCase()) {
			case "top" -> TOP;
			case "top left" -> TOP_LEFT;
			case "top right" -> TOP_RIGHT;

			case "bottom" -> BOTTOM;
			case "bottom left" -> BOTTOM_LEFT;
			case "bottom right" -> BOTTOM_RIGHT;

			case "left" -> LEFT;
			case "right" -> RIGHT;

			case "center" -> CENTER;

			default -> TOP_LEFT;
		};
	}

	public int alignX(int x) {
		return switch(this) {
			case TOP, TOP_LEFT, BOTTOM, BOTTOM_LEFT, LEFT -> x;
			case TOP_RIGHT, BOTTOM_RIGHT, RIGHT -> WORKING_WIDTH - x;

			case CENTER -> (WORKING_WIDTH / 2) + x;
		};
	}

	public int alignY(int y) {
		return switch(this) {
			case TOP, TOP_LEFT, TOP_RIGHT -> y;
			case BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT -> WORKING_HEIGHT - y;

			case LEFT, RIGHT, CENTER -> (WORKING_HEIGHT / 2) + y;
		};
	}
}

