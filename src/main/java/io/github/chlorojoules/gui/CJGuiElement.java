package io.github.chlorojoules.gui;

// Just used for progress bar arrows right now. Can be used for any element
// Which doesn't have any real metadata associated with it.

public class CJGuiElement {
	public int xDisplayPosition;
	public int yDisplayPosition;
	public int[] damageExclusive = null;

	public CJGuiElement(int x, int y) {
		xDisplayPosition = x;
		yDisplayPosition = y;
	}

	public CJGuiElement setDamageExclusive(int[] damageExclusive) {
		this.damageExclusive = damageExclusive;

		return this;
	}
}
