package io.github.chlorojoules.machine;

import io.github.chlorojoules.gui.CJGuiElement;

public class CJMachineSlotInfo extends CJGuiElement {
	public boolean output;

	public CJMachineSlotInfo(int x, int y, boolean output) {
		super(x, y);

		this.output = output;
	}
}
