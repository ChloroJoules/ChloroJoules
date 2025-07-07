package io.github.chlorojoules.client.machine;

import com.fox2code.foxloader.registry.RegisteredBlock;
import com.fox2code.foxloader.registry.RegisteredItem;

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
			int index, RegisteredBlock block, int count, boolean as_block_id) {

		this.index = index;
		if(!as_block_id) {
			this.id = block.asRegisteredItem().getRegisteredItemId();
		}
		else {
			this.id = block.getRegisteredBlockId();
		}

		this.count = count;
	}

	public CJMachineRecipeComponent(
			int index, RegisteredItem item, int count) {

		this.index = index;
		this.id = item.getRegisteredItemId();
		this.count = count;
	}

	public CJMachineRecipeComponent setTarget(CJMachineRecipeTarget value) {
		target = value;
		return this;
	}
}
