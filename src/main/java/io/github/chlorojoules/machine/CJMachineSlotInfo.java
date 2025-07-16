package io.github.chlorojoules.machine;

import io.github.chlorojoules.gui.CJGuiElement;
import io.github.chlorojoules.gui.CJGuiMachineBaseSlot;
import net.minecraft.common.item.ItemStack;

public class CJMachineSlotInfo extends CJGuiElement {
	public static final int DEFAULT = 0;
	public static final int GEM = 1;
	public static final int FERTILIZER = 2;

	public boolean output;
	public ItemStack[] allowedItems = null;
	public int renderType = DEFAULT;

	public CJMachineSlotInfo(int x, int y, boolean output) {
		super(x, y);

		this.output = output;
	}

	public CJMachineSlotInfo setRenderType(int value) {
		renderType = value;
		return this;
	}

	public CJMachineSlotInfo setAllowedItems(ItemStack[] value) {
		allowedItems = value;
		return this;
	}

	public boolean isAllowedItem(ItemStack stack) {
		if(allowedItems == null) return true;

		for(ItemStack allowed : allowedItems) {
			if(stack.getItemID() == allowed.getItemID() &&
					stack.getItemDamage() == allowed.getItemDamage()) {

				return true;
			}
		}

		return false;
	}
}
