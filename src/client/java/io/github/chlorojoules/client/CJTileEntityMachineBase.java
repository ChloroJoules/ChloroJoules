package io.github.chlorojoules.client;

import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;

import java.util.ArrayList;

public class CJTileEntityMachineBase extends TileEntity implements IInventory {
	// TODO: Make a tagging/name/enum system so we can keep track of slots
	//		 In a machine without magic numbers.
	//		 `CJContainerMachineBase` can contain this mapping which can then
	//		 Be read here.
	public ArrayList<ItemStack> stacks;
	public ArrayList<CJTankVolume> tanks;

	public String errorMessage = null;

	private final CJMachineBuilder machineBuilder;

	public static CJTileEntityMachineBase machineEntity(
			World world, int x, int y, int z) {

		// TODO: Should we cache this?
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		return (CJTileEntityMachineBase) tileEntity;
	}

	public CJTileEntityMachineBase(CJMachineBuilder builder) {
		machineBuilder = builder;

		stacks = new ArrayList<>();
		// TODO: There's probably a better way to do this.
		for(int i = 0; i < builder.slots.size(); i++) stacks.add(null);

		tanks = new ArrayList<>(builder.tankVolumes);
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

	// TODO: Make this return different tanks depending on attempted fluid
	//       Insertion.
	public CJTankVolume getPrimaryInputTank() {
		for(int i = 0; i < tanks.size(); i++) {
			if(!machineBuilder.tanks.get(i).output) return tanks.get(i);
		}

		return null;
	}

	@Override
	public void updateEntity() {
		machineBuilder.machineImpl.updateMachine(this);
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
		ItemStack stack = stacks.get(slot);

		if(stack == null) return null;

		if(stack.stackSize <= size) {
			stacks.set(slot, null);
		}
		else {
			if(stack.stackSize == 0) {
				stacks.set(slot, null);
			}

			return stack.splitStack(size);
		}

		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		stacks.set(slot, stack);

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
		return stacks.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return stacks.get(slot);
	}

	@Override
	public String getInvName() {
		return "inventory." + machineBuilder.name;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {}
}
