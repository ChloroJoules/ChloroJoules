package io.github.chlorojoules.machine;

import io.github.chlorojoules.gui.CJGuiElement;
import io.github.chlorojoules.gui.CJGuiMachineBaseSlot;

public class CJMachineSlotInfo extends CJGuiElement {
	public boolean output;
	public boolean isGem = false;

	public CJMachineSlotInfo(int x, int y, boolean output) {
		super(x, y);

		this.output = output;
	}

	public CJMachineSlotInfo setGem(boolean value) {
		isGem = value;
		return this;
	}

	public boolean isGem() {
		return isGem;
	}
}
