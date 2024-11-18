package io.github.chlorojoules.client.gui;

import static io.github.chlorojoules.client.gui.CJGuiMachineBaseLayout.*;

public class CJGuiGravityInfo {
	private static int getAnchoredX(CJGuiGravity anchor, int x, int width) {
		switch(anchor) {
			case TOP_LEFT:
			case BOTTOM_LEFT: return x;
			case TOP_RIGHT:
			case BOTTOM_RIGHT: return WORKING_WIDTH - (x + width);

			case CENTER: return ((WORKING_WIDTH - width) / 2) + x;
		}

		return x;
	}

	private static int getAnchoredY(CJGuiGravity anchor, int y, int height) {
		switch(anchor) {
			case TOP_LEFT:
			case TOP_RIGHT: return y;
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT: return WORKING_HEIGHT - (y + height);

			case CENTER: return ((WORKING_HEIGHT - height) / 2) + y;
		}

		return y;
	}

	public static int getSlotAnchoredX(
			CJGuiGravity anchor, int x, boolean output) {

		int width = output ? SLOT_OUT_WIDTH : SLOT_IN_WIDTH;

		return getAnchoredX(anchor, x, width);
	}

	public static int getSlotAnchoredY(
			CJGuiGravity anchor, int y, boolean output) {

		int height = output ? SLOT_OUT_HEIGHT : SLOT_IN_HEIGHT;

		return getAnchoredY(anchor, y, height);
	}

	public static int getTankAnchoredX(CJGuiGravity anchor, int x) {
		return getAnchoredX(anchor, x, FLUID_WIDTH);
	}

	public static int getTankAnchoredY(CJGuiGravity anchor, int y) {
		return getAnchoredY(anchor, y, FLUID_HEIGHT);
	}

	public static int getProgressBarAnchoredX(CJGuiGravity anchor, int x) {
		return getAnchoredX(anchor, x, PROGRESS_WIDTH);
	}

	public static int getProgressBarAnchoredY(CJGuiGravity anchor, int y) {
		return getAnchoredY(anchor, y, PROGRESS_HEIGHT);
	}
}
