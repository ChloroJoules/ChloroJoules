package io.github.chlorojoules.client;

import static io.github.chlorojoules.client.CJGuiMachineBaseLayout.*;

public class CJGuiGravityInfo {
	public static int getSlotAnchoredX(
			CJGuiGravity anchor, int x, boolean output) {

		switch(anchor) {
			case TOP_LEFT:
			case BOTTOM_LEFT: return x;
			case TOP_RIGHT:
			case BOTTOM_RIGHT: {
				int width = output ? SLOT_OUT_WIDTH : SLOT_IN_WIDTH;
				return WORKING_WIDTH - (x + width);
			}
		}

		return x;
	}

	public static int getSlotAnchoredY(
			CJGuiGravity anchor, int y, boolean output) {

		switch(anchor) {
			case TOP_LEFT:
			case TOP_RIGHT: return y;
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT: {
				int height = output ? SLOT_OUT_HEIGHT : SLOT_IN_HEIGHT;
				return WORKING_HEIGHT - (y + height);
			}
		}

		return y;
	}

	public static int getTankAnchoredX(CJGuiGravity anchor, int x) {
		switch(anchor) {
			case TOP_LEFT:
			case BOTTOM_LEFT: return x;
			case TOP_RIGHT:
			case BOTTOM_RIGHT: return WORKING_WIDTH - (x + FLUID_WIDTH);
		}

		return x;
	}

	public static int getTankAnchoredY(CJGuiGravity anchor, int y) {
		switch(anchor) {
			case TOP_LEFT:
			case TOP_RIGHT: return y;
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT: return WORKING_HEIGHT - (y + FLUID_HEIGHT);
		}

		return y;
	}
}
