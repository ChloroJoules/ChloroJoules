package io.github.chlorojoules.machine;

import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.item.ItemStack;

public class CJMachineRecipeComponent {
	public CJMachineRecipeTarget target;
	public int index;

	public ItemStack stack;

	public CJTankVolume volume;

	public String tag;
	public int tagCount;
	public boolean isTag = false;

	public float chance = 1.0f;
	public boolean optional = false;

	public CJMachineRecipeComponent(int index, ItemStack stack) {
		this.target = CJMachineRecipeTarget.SLOT;
		this.index = index;
		this.stack = stack;
	}

	public CJMachineRecipeComponent(int index, CJTankVolume volume) {
		this.target = CJMachineRecipeTarget.TANK;
		this.index = index;
		this.volume = volume;
	}

	public CJMachineRecipeComponent(
			CJMachineRecipeTarget target, int index, String tag, int count) {

		this.target = target;
		this.index = index;
		this.tag = tag;
		this.tagCount = count;
		this.isTag = true;
	}

	public CJMachineRecipeComponent setChance(float value) {
		this.chance = value;
		return this;
	}

	public CJMachineRecipeComponent setOptional(boolean value) {
		this.optional = value;
		return this;
	}

	public int getStackSize() {
		if(isTag) return tagCount;
		return stack.stackSize;
	}
}
