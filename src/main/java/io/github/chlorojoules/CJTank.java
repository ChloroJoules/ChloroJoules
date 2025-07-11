package io.github.chlorojoules;

import io.github.chlorojoules.gui.CJGuiElement;

// NOTE: All units are in millibuckets.

// This is to a fluid as a `Slot` is to an item.
public class CJTank extends CJGuiElement {
	// Millibuckets per bucket.
	public static final int BUCKET = 1000;

	public boolean output = false;
	public boolean bidirectional = false;

	public CJTank(int x, int y) {
		super(x, y);
	}
}
