package io.github.chlorojoules.client.gui;

import static io.github.chlorojoules.client.gui.CJGuiMachineBaseLayout.*;

public class CJGuiButton extends CJGuiElement {
	public static final int EMPTY = 0;
	public static final int JEWEL = 1;

	String tooltip;
	int label;

	public CJGuiButton(int x, int y, String tooltip, int label) {
		super(x, y);

		this.tooltip = tooltip;
		this.label = label;
	}

	// TODO: Allow to label with arbitrary item icons instead.
	public int[] getLabelCoords() {
		switch(label) {
			case JEWEL: return new int[] { LABEL_JEWEL_X, LABEL_JEWEL_Y };

			case EMPTY:
			default: return null;
		}
	}
}
