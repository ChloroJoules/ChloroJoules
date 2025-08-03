package io.github.chlorojoules.item;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;

public class CJItemFluidContainer extends Item {
	public CJItemFluidContainer(String name) {
		super(name);
	}

	public static ItemStack getContainerMatchingFluid(
			ItemStack[] inventory, int fluidID) {

		for(ItemStack stack : inventory) {
			if(stack == null) continue;
			if(!(stack.getItem() instanceof CJItemFluidContainer)) continue;

			CompoundTag tagCompound = stack.getTagCompound();
			if(tagCompound == null) continue;

			CJTankVolume volume = new CJTankVolume();
			volume.readFromNBT(tagCompound);
			if(volume.fluidID != fluidID) continue;

			return stack;
		}

		return null;
	}
}
