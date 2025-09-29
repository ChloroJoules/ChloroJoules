package io.github.chlorojoules.block.tileentity;

import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;

public class CJTileEntityGuide extends TileEntity implements IInventory {
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {}

	@Override
	public String getInvName() {
		return "tile.cj_guide.name";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {}
}
