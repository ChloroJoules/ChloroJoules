package io.github.chlorojoules.item;

import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;

public class CJItemFluidContainer extends Item {
	public CJItemFluidContainer(String name) {
		super(name);
	}

	public static ItemStack getContainerMatchingFluid(
			Entity entity, int fluidID) {

		ItemStack[] inventory = entity.getInventory();

		for(ItemStack stack : inventory) {
			if(!(stack.getItem() instanceof CJItemFluidContainer)) continue;

			CJTankVolume volume = new CJTankVolume();
			volume.readFromNBT(stack.getTagCompound());
			if(volume.fluidID != fluidID) continue;

			return stack;
		}

		return null;
	}
}
