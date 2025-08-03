package io.github.chlorojoules.item;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

import java.util.ArrayList;

public class CJItemFluidContainer extends Item {
	public CJItemFluidContainer(String name) {
		super(name);

		setMaxDamage(CJTankVolume.DEFAULT_MAX);
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

	@Override
	public void onUpdate(
			ItemStack itemstack, World world, Entity entity, int slot,
			boolean inHand) {

		super.onUpdate(itemstack, world, entity, slot, inHand);

		CompoundTag tagCompound = itemstack.getTagCompound();
		if(tagCompound == null) return;

		CJTankVolume volume = new CJTankVolume();
		volume.readFromNBT(tagCompound);

		itemstack.setItemDamage(CJTankVolume.DEFAULT_MAX - volume.current);
	}

	public static boolean consumeFromContainers(
			ItemStack[] inventory, int fluidID, int required) {

		ArrayList<ItemStack> containers = new ArrayList<>();
		int consumed = 0;
		while(consumed < required) {
			ItemStack container =
					CJItemFluidContainer.getContainerMatchingFluid(
							inventory, fluidID);

			if(container == null) return false;

			CompoundTag tag = container.getTagCompound();
			CJTankVolume volume = new CJTankVolume();
			volume.readFromNBT(tag);

			consumed += Math.min(required - consumed, volume.current);
			containers.add(container);
		}

		consumed = 0;
		for(ItemStack container : containers) {
			CompoundTag tag = container.getTagCompound();
			CJTankVolume volume = new CJTankVolume();
			volume.readFromNBT(tag);

			consumed += volume.removeFluid(
					fluidID, Math.max(required - consumed, 0), false);

			volume.writeToNBT(tag);
		}

		return true;
	}
}
