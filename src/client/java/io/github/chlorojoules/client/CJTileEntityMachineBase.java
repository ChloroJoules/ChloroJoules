package io.github.chlorojoules.client;

import io.github.chlorojoules.client.machine.CJMachineBuilder;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;
import net.minecraft.src.game.nbt.NBTTagList;

import java.util.ArrayList;

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

	public final CJMachineBuilder machineBuilder;

	public static CJTileEntityMachineBase machineEntity(
			World world, int x, int y, int z) {

		// TODO: Should we cache this?
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if(!(tileEntity instanceof CJTileEntityMachineBase)) return null;

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

	@Override
	public void updateEntity() {
		machineBuilder.machineImpl.updateMachine(this);
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		tagCompound.setString("id", "CJMachineBase");
		tagCompound.setString("cj_machine", machineBuilder.name);
		tagCompound.setInteger("x", this.xCoord);
		tagCompound.setInteger("y", this.yCoord);
		tagCompound.setInteger("z", this.zCoord);

		/*
		// Serialize slots.
		NBTTagList itemsList = new NBTTagList();
		for(int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);

			if(stack == null) continue;

			NBTTagCompound slotTag = new NBTTagCompound();
			slotTag.setByte("Slot", (byte) i);
			stack.writeToNBT(slotTag);
			itemsList.setTag(slotTag);
		}
		tagCompound.setTag("Items", itemsList);

		// Serialize tanks.
		NBTTagList tanksList = new NBTTagList();
		for(int i = 0; i < tanks.size(); i++) {
			CJTankVolume tankVolume = tanks.get(i);

			if(tankVolume == null) continue;

			// NOTE: `max` and `lockFluid` are expected to be set statically
			//       Per-machine so we don't need to serialize them.
			NBTTagCompound volumeTag = new NBTTagCompound();
			volumeTag.setByte("Volume", (byte) i);
			volumeTag.setInteger("Current", tankVolume.current);
			volumeTag.setInteger("Fluid", tankVolume.fluidID);
			tanksList.setTag(volumeTag);
		}
		tagCompound.setTag("Tanks", itemsList);

		// Serialize progress.
		NBTTagList progressBarList = new NBTTagList();
		for(int i = 0; i < machineBuilder.progressBars.size(); i++) {
			NBTTagCompound progressBarTag = new NBTTagCompound();
			progressBarTag.setByte("Progress", (byte) i);
			progressBarsTag.setShort("Ticks", (short) operationTicks);
			progressBarList.setTag(progressBarTag);
		}
		tagCompound.setTag("Progresses", itemsList);*/
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		// TODO: Get builder from registry.
		tagCompound.getString("cj_machine");

		xCoord = tagCompound.getInteger("x");
		yCoord = tagCompound.getInteger("y");
		zCoord = tagCompound.getInteger("z");

		/*
		// Deserialize slots.
		NBTTagList itemsList = tagCompound.getTagList("Items");
		// TODO: Are tag lists always unordered? Is there a way we can
		//       Request they be ordered to remove the need for the extra
		//       `Slot` byte?
		for(int i = 0; i < stacks.size(); i++) {
			NBTTagCompound slotTag = (NBTTagCompound) itemsList.tagAt(i);
			byte slotIndex = slotTag.getByte("Slot");

			stacks.set(slotIndex, new ItemStack(slotTag));
		}

		// Deserialize tanks.
		NBTTagList tanksList = tagCompound.getTagList("Tanks");
		for(int i = 0; i < tanks.size(); i++) {
			NBTTagCompound volumeTag = (NBTTagCompound) tanksList.tagAt(i);
			byte tankIndex = volumeTag.getByte("Volume");

			CJTankVolume volume = tanks.get(tankIndex);
			volume.current = volumeTag.getInteger("Current");
			volume.fluidID = volumeTag.getInteger("Fluid");
			tanks.set(tankIndex, volume);
		}

		// Deserialize progress.
		NBTTagList progressBarList = tagCompound.getTagList("Progresses");
		for(int i = 0; i < stacks.size(); i++) {
			NBTTagCompound progressBarTag =
					(NBTTagCompound) progressBarList.tagAt(i);

			byte progressBarIndex = progressBarTag.getByte("Progress");
			operationTicks = progressBarsTag.getShort("Ticks");

			machineBuilder.machineImpl.progressFromNBT(
					this, progressBarIndex, progressBarTag);
		}*/
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
