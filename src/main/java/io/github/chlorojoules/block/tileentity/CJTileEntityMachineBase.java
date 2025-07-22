package io.github.chlorojoules.block.tileentity;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.Tag;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.CJBlockMachineBase;
import io.github.chlorojoules.gui.CJGuiCoordinateDisplay;
import io.github.chlorojoules.machine.CJIMachine;
import io.github.chlorojoules.machine.CJMachineBuilder;

import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.entity.other.EntityItem;
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

import java.util.ArrayList;

public class CJTileEntityMachineBase extends TileEntity implements IInventory {
	private ArrayList<ItemStack> stacks;
	public ArrayList<CJTankVolume> tanks;
	public ArrayList<Boolean> buttonStates;
	public ArrayList<CJGuiCoordinateDisplay> coordinateDisplays;

	public String errorMessage = null;
	public boolean isWarning = false;

	public boolean isPassive = false;
	public int operationTicks = 0;
	public int operationLength = 1;
	public CJRarity jewelRarity = CJRarity.MANUFACTURED;

	public CJBlockMachineBase machine;
	public CJIMachine impl;

	public static CJTileEntityMachineBase machineEntity(
			World world, int x, int y, int z) {

		return (CJTileEntityMachineBase) world.getBlockTileEntity(x, y, z);
	}

	private void initFromBuilder(CJBlockMachineBase machine) {
		this.machine = machine;

		CJMachineBuilder builder = machine.machineBuilder;
		if(builder.machineImpl != null) {
			try {
				impl = builder.machineImpl
						.getDeclaredConstructor()
						.newInstance();
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		}

		stacks = new ArrayList<>();
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

		buttonStates = new ArrayList<>();
		for(int i = 0; i < builder.buttons.size(); i++) {
			buttonStates.add(false);
		}

		coordinateDisplays = new ArrayList<>();
		for(int i = 0; i < builder.linkCoordinates.size(); i++) {
			coordinateDisplays.add(null);
		}
	}

	public CJTileEntityMachineBase() {}

	public CJTileEntityMachineBase(CJBlockMachineBase machine) {
		initFromBuilder(machine);
	}

	public void onBreak(World world, int x, int y, int z) {
		for(ItemStack stack : stacks) {
			if(stack == null) continue;
			EntityItem entity = new EntityItem(world, x, y, z, stack);
			world.entityJoinedWorld(entity);
		}

		stacks = null;
		tanks = null;
		operationTicks = 0;

		if(impl != null) {
			impl.onBreak(world, x, y, z);
		}
	}

	@Override
	public void updateEntity() {
		if(impl == null) return;

		impl.updateMachine(this);
	}

	public int getWorldBlockID() {
		return worldObj.getBlockId(xCoord, yCoord, zCoord);
	}

	public int[] getWorldPosition() {
		return new int[] { xCoord, yCoord, zCoord };
	}

	public int getWorldBlockMetadata() {
		return worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	}

	public void setWorldBlockMetadata(int metadata) {
		worldObj.setBlockMetadata(xCoord, yCoord, zCoord, metadata);
		worldObj.notifyBlockChange(xCoord, yCoord, zCoord, getWorldBlockID());
	}

	private void writeStacks(CompoundTag tagCompound) {
		ListTag<Tag> itemsList = new ListTag<>();
		for(int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);

			if(stack == null) continue;

			CompoundTag slotTag = new CompoundTag();
			slotTag.setByte("slot", (byte) i);
			stack.writeToNBT(slotTag);
			itemsList.setTag(slotTag);
		}
		tagCompound.setTag("items", itemsList);
	}

	private void writeTanks(CompoundTag tagCompound) {
		ListTag<Tag> tanksList = new ListTag<>();
		for(int i = 0; i < tanks.size(); i++) {
			CJTankVolume tankVolume = tanks.get(i);

			if(tankVolume == null) continue;

			// NOTE: `max` and `lockFluid` are expected to be set statically
			//       Per-machine so we don't need to serialize them.
			CompoundTag volumeTag = new CompoundTag();
			volumeTag.setByte("volume", (byte) i);
			volumeTag.setInteger("current", tankVolume.current);
			volumeTag.setInteger("fluid", tankVolume.fluidID);
			tanksList.setTag(volumeTag);
		}
		tagCompound.setTag("tanks", tanksList);
	}

	private void writeButtons(CompoundTag tagCompound) {
		// Serialize buttons.
		ListTag<Tag> buttonList = new ListTag<>();
		for(int i = 0; i < machine.machineBuilder.buttons.size(); i++) {
			CompoundTag buttonTag = new CompoundTag();
			buttonTag.setBoolean("state", buttonStates.get(i));
			buttonList.setTag(buttonTag);
		}
		tagCompound.setTag("buttons", buttonList);
	}

	@Override
	public void writeToNBT(CompoundTag tagCompound) {
		super.writeToNBT(tagCompound);

		tagCompound.setString("cj_machine", machine.machineBuilder.name);

		if(impl != null) impl.writeToNBT(tagCompound);

		writeStacks(tagCompound);
		writeTanks(tagCompound);
		writeButtons(tagCompound);

		tagCompound.setShort("operation_ticks", (short) operationTicks);
	}

	private void readStacks(CompoundTag tagCompound) {
		ListTag<Tag> itemsList = tagCompound.getTagList("items");
		for(int i = 0; i < itemsList.size(); i++) {
			CompoundTag slotTag = (CompoundTag) itemsList.get(i);
			byte slotIndex = slotTag.getByte("slot");

			stacks.set(slotIndex, ItemStack.loadItemStackFromNBT(slotTag));
		}
	}

	private void readTanks(CompoundTag tagCompound) {
		ListTag<Tag> tanksList = tagCompound.getTagList("tanks");
		for(int i = 0; i < tanksList.size(); i++) {
			CompoundTag volumeTag = (CompoundTag) tanksList.get(i);
			byte tankIndex = volumeTag.getByte("volume");

			CJTankVolume volume = tanks.get(tankIndex);
			volume.current = volumeTag.getInteger("current");
			volume.fluidID = volumeTag.getInteger("fluid");
			tanks.set(tankIndex, volume);
		}
	}

	private void readButtons(CompoundTag tagCompound) {
		ListTag<Tag> buttonsList = tagCompound.getTagList("buttons");
		for(int i = 0; i < buttonsList.size(); i++) {
			CompoundTag buttonTag = (CompoundTag) buttonsList.get(i);

			buttonStates.set(i, buttonTag.getBoolean("state"));
		}
	}

	@Override
	public void readFromNBT(CompoundTag tagCompound) {
		super.readFromNBT(tagCompound);

		initFromBuilder(CJMod.machines.get(
				tagCompound.getString("cj_machine")));

		if(impl != null) impl.readFromNBT(tagCompound);

		readStacks(tagCompound);
		readTanks(tagCompound);
		readButtons(tagCompound);

		operationTicks = tagCompound.getShort("operation_ticks");
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
		return "tile." + machine.machineBuilder.name + ".name";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {}
}
