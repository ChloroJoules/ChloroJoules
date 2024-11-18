package io.github.chlorojoules.client.machine;

import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.block.BlockFluid;

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

	public CJMachineRecipeComponent(int index, Block block, int count) {
		this.index = index;

		if(block instanceof BlockFluid) {
			target = CJMachineRecipeTarget.TANK;
			this.id = block.getBlockID();
		}
		else {
			this.id = block.asRegisteredItem().getRegisteredItemId();
		}

		this.count = count;
	}

	public CJMachineRecipeComponent setTarget(CJMachineRecipeTarget value) {
		target = value;
		return this;
	}
}
