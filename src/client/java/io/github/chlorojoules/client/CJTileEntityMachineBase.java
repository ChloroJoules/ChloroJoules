package io.github.chlorojoules.client;

import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;

public class CJTileEntityMachineBase extends TileEntity implements IInventory {
	// TODO: Make a tagging/name/enum system so we can keep track of slots
	//		 In a machine without magic numbers.
	//		 `CJContainerMachineBase` can contain this mapping which can then
	//		 Be read here.
	// NOTE: For now, let convention be that slot 0 is always output.
	public ItemStack[] stacks = new ItemStack[2];
	public CJTankVolume[] tanks = new CJTankVolume[1];

	public static CJTileEntityMachineBase machineEntity(
			World world, int x, int y, int z) {

		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		return (CJTileEntityMachineBase) tileEntity;
	}

	public CJTileEntityMachineBase() {
		tanks[0] = new CJTankVolume();
		tanks[0].current = 3 * CJTank.BUCKET;
		tanks[0].fluidID = CJBlockMachineBase.lavaMoving.blockID;
	}

	public void onBreak(World world, int x, int y, int z) {
		// TODO: Drop item stacks.
		// TODO: Preserve fluid tanks etc. Should we make machines retain
		//  	 Inventory on break?
		/*
		dropStack = new ItemStack(item.itemID, size, item.getItemDamage());
		EntityItem entity = new EntityItem(world, x, y, z, dropStack);

		world.entityJoinedWorld(entity);
		if(entity.hasTagCompound()) {
			entity.item.setTagCompound(stack.getTagCompound());
		}
		 */
	}

	@Override
	public void updateEntity() {
		// TODO: Machine behaviour interface.

		ItemStack stack = stacks[0];
		if(stack == null) {
			if(tanks[0].removeFluid(null, 10, false) == 10) {
				stacks[0] = new ItemStack(Item.snowball);
				this.onInventoryChanged();
			}
		}
		else if(stack.stackSize < stack.getMaxStackSize()) {
			if(tanks[0].removeFluid(null, 10, false) == 10) {
				stacks[0].stackSize++;
				this.onInventoryChanged();
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		// TODO: Machine NBT.
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		// TODO: Machine NBT.
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		if(stacks[slot] == null) return null;

		ItemStack stack = stacks[slot];

		if(stacks[slot].stackSize <= size) {
			stacks[slot] = null;
		}
		else {
			if(stacks[slot].stackSize == 0) {
				stacks[slot] = null;
			}

			return stack.splitStack(size);
		}

		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		stacks[slot] = stack;

		int limit = getInventoryStackLimit();
		if(stack != null && stack.stackSize > limit) {
			stack.stackSize = limit;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public int getSizeInventory() {
		return stacks.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return stacks[slot];
	}

	@Override
	public String getInvName() {
		return "inventory.cj_machinebase";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {}
}
