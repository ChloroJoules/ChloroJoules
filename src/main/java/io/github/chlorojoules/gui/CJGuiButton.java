package io.github.chlorojoules.gui;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJGuiButton extends CJGuiElement {
	String tooltip;
	int label;

	public CJGuiButton(int x, int y, String tooltip, int label) {
		super(x, y);

		this.tooltip = tooltip;
		this.label = label;
	}
}
