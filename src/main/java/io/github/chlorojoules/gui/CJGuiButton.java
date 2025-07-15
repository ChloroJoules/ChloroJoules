package io.github.chlorojoules.gui;

import net.minecraft.common.item.ItemStack;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJGuiButton extends CJGuiElement {
	String tooltip;
	ItemStack label;

	public CJGuiButton(int x, int y, String tooltip, ItemStack label) {
		super(x, y);

		this.tooltip = tooltip;
		this.label = label;
	}
}
