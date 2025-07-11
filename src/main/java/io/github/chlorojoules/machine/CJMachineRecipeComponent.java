package io.github.chlorojoules.machine;

import net.minecraft.common.block.Block;
import net.minecraft.common.item.Item;

public class CJMachineRecipeComponent {
	public CJMachineRecipeTarget target = CJMachineRecipeTarget.SLOT;
	public int index;
	public int id;
	public int count;

	public CJMachineRecipeComponent(int index, int id, int count) {
		this.index = index;
		this.id = id;
		this.count = count;
	}

	public CJMachineRecipeComponent(
			int index, Block block, int count, boolean as_block_id) {

		this.index = index;
		if(!as_block_id) {
			this.id = block.getItemID();
		}
		else {
			this.id = block.blockID;
		}

		this.count = count;
	}

	public CJMachineRecipeComponent(
			int index, Item item, int count) {

		this.index = index;
		this.id = item.itemID;
		this.count = count;
	}

	public CJMachineRecipeComponent setTarget(CJMachineRecipeTarget value) {
		target = value;
		return this;
	}
}
