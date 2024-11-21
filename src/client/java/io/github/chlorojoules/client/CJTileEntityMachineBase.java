package io.github.chlorojoules.client;

import io.github.chlorojoules.client.machine.CJIMachine;
import io.github.chlorojoules.client.machine.CJMachineBuilder;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.entity.other.EntityItem;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;
import net.minecraft.src.game.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.logging.Logger;

public class CJTileEntityMachineBase extends TileEntity implements IInventory {
	// TODO: Make a tagging/name/enum system so we can keep track of slots
	//		 In a machine without magic numbers.
	//		 `CJContainerMachineBase` can contain this mapping which can then
	//		 Be read here.
	public ArrayList<ItemStack> stacks;
	public ArrayList<CJTankVolume> tanks;

	public String errorMessage = null;

	public boolean isPassive = false;
	public int operationTicks = 0;
	public int operationLength = 1;
	public CJRarity jewelRarity = CJRarity.MANUFACTURED;

	public CJMachineBuilder machineBuilder;
	public CJIMachine impl;

	public static CJTileEntityMachineBase machineEntity(
			World world, int x, int y, int z) {

		// TODO: Should we cache this?
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if(!(tileEntity instanceof CJTileEntityMachineBase)) return null;

		return (CJTileEntityMachineBase) tileEntity;
	}

	private void initFromBuilder(CJMachineBuilder builder) {
		machineBuilder = builder;

		try {
			impl = (CJIMachine) builder.machineImpl.newInstance();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}

		stacks = new ArrayList<>();
		// TODO: There's probably a better way to do this.
		for(int i = 0; i < builder.slots.size(); i++) stacks.add(null);

		tanks = new ArrayList<>();
		for(int i = 0; i < builder.tankVolumes.size(); i++) {
			CJTankVolume volume = builder.tankVolumes.get(i);
			CJTankVolume newVolume = new CJTankVolume();
			newVolume.max = volume.max;
			newVolume.fluidID = volume.fluidID;
			newVolume.lockFluid = volume.lockFluid;
			tanks.add(newVolume);
		}
	}

	public CJTileEntityMachineBase() {}

	public CJTileEntityMachineBase(CJMachineBuilder builder) {
		initFromBuilder(builder);
	}

	public void onBreak(World world, int x, int y, int z) {
		// TODO: Preserve fluid tanks etc. Should we make machines retain
		//  	 Inventory on break?

		for(ItemStack stack : stacks) {
			if(stack == null) continue;
			EntityItem entity = new EntityItem(world, x, y, z, stack);
			world.entityJoinedWorld(entity);
		}

		stacks = null;
		tanks = null;
		operationTicks = 0;
	}

	@Override
	public void updateEntity() {
		impl.updateMachine(this);
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		tagCompound.setString("cj_machine", machineBuilder.name);

		// Serialize slots.
		NBTTagList itemsList = new NBTTagList();
		for(int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);

			if(stack == null) continue;

			NBTTagCompound slotTag = new NBTTagCompound();
			slotTag.setByte("slot", (byte) i);
			stack.writeToNBT(slotTag);
			itemsList.setTag(slotTag);
		}
		tagCompound.setTag("items", itemsList);

		// Serialize tanks.
		NBTTagList tanksList = new NBTTagList();
		for(int i = 0; i < tanks.size(); i++) {
			CJTankVolume tankVolume = tanks.get(i);

			if(tankVolume == null) continue;

			// NOTE: `max` and `lockFluid` are expected to be set statically
			//       Per-machine so we don't need to serialize them.
			NBTTagCompound volumeTag = new NBTTagCompound();
			volumeTag.setByte("volume", (byte) i);
			volumeTag.setInteger("current", tankVolume.current);
			volumeTag.setInteger("fluid", tankVolume.fluidID);
			tanksList.setTag(volumeTag);
		}
		tagCompound.setTag("tanks", tanksList);

		// Serialize progress.
		NBTTagList progressBarList = new NBTTagList();
		for(int i = 0; i < machineBuilder.progressBars.size(); i++) {
			NBTTagCompound progressBarTag = new NBTTagCompound();
			progressBarTag.setByte("progress", (byte) i);
			progressBarTag.setShort("ticks", (short) operationTicks);
			progressBarList.setTag(progressBarTag);
		}
		tagCompound.setTag("progresses", progressBarList);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		initFromBuilder(
				CJClient.machines.get(tagCompound.getString("cj_machine")));

		// Deserialize slots.
		NBTTagList itemsList = tagCompound.getTagList("items");
		// TODO: Are tag lists always unordered? Is there a way we can
		//       Request they be ordered to remove the need for the extra
		//       `Slot` byte?
		for(int i = 0; i < itemsList.tagCount(); i++) {
			NBTTagCompound slotTag = (NBTTagCompound) itemsList.tagAt(i);
			byte slotIndex = slotTag.getByte("slot");

			stacks.set(slotIndex, new ItemStack(slotTag));
		}

		// Deserialize tanks.
		NBTTagList tanksList = tagCompound.getTagList("tanks");
		for(int i = 0; i < tanksList.tagCount(); i++) {
			NBTTagCompound volumeTag = (NBTTagCompound) tanksList.tagAt(i);
			byte tankIndex = volumeTag.getByte("volume");

			CJTankVolume volume = tanks.get(tankIndex);
			volume.current = volumeTag.getInteger("current");
			volume.fluidID = volumeTag.getInteger("fluid");
			tanks.set(tankIndex, volume);
		}

		// Deserialize progress.
		NBTTagList progressBarList = tagCompound.getTagList("progresses");
		for(int i = 0; i < progressBarList.tagCount(); i++) {
			NBTTagCompound progressBarTag =
					(NBTTagCompound) progressBarList.tagAt(i);

			// TODO: Does multiple progress bars even make sense?
			//byte progressBarIndex = progressBarTag.getByte("progress");
			operationTicks = progressBarTag.getShort("ticks");
		}
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
