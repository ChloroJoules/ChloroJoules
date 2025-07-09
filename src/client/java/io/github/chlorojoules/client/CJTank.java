package io.github.chlorojoules.client;

// TODO: This currently doesn't provide a way to disambiguate which tank should
//		 Be selected in a multi in/out scenario (neither do output slots).
//		 How should we handle this?

// NOTE: All units are in millibuckets.

import io.github.chlorojoules.client.gui.CJGuiElement;

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
