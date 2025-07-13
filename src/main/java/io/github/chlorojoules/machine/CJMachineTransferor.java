package io.github.chlorojoules.machine;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.CJInventoryHelper;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.CJGuiCoordinateDisplay;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

import static io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase.machineEntity;

public class CJMachineTransferor implements CJIMachine {
	// Damage states.
	public static final int INACTIVE = 0;
	public static final int TRANSMIT_ITEMS = 1;
	public static final int RECEIVE_ITEMS = 2;
	public static final int TRANSMIT_FLUIDS = 3;
	public static final int RECEIVE_FLUIDS = 4;
	public static final int MULTI_TRANSMIT_ITEMS = 5;
	public static final int MULTI_TRANSMIT_FLUIDS = 6;
	public static final int MAX_DAMAGE = MULTI_TRANSMIT_FLUIDS;

	private IInventory[] adjacentInventories = null;
	private CJTileEntityMachineBase[] adjacentMachines = null;

	public int[] linked = null;
	// TODO: Remove this and iterate linked for round robin.
	public int[] lastReceiver = null;

	private void updateTransmit(CJTileEntityMachineBase machineEntity) {
		ItemStack stack = machineEntity.stacks.get(0);
		int currentItemID = -1;

		if(stack != null) {
			if(stack.stackSize >= stack.getMaxStackSize()) return;

			currentItemID = stack.getItemID();
		}

		for(IInventory inventory : adjacentInventories) {
			if(inventory == null) continue;

			int slotIndex = CJInventoryHelper.getMatchingOutputIndex(
					inventory, currentItemID);

			if(slotIndex == -1) continue;
			ItemStack inStack = inventory.getStackInSlot(slotIndex);

			if(inStack == null) continue;

			// TODO: This is currently hardcoded to 1 item per tick.
			if(stack == null) {
				machineEntity.stacks.set(0, new ItemStack(
						inStack.getItemID(), 1, inStack.itemDamage));
			}
			else stack.stackSize++;

			if(inStack.stackSize == 1) {
				inventory.setInventorySlotContents(slotIndex, null);
			}
			else inStack.stackSize--;

			break;
		}
	}

	private void updateReceive(CJTileEntityMachineBase machineEntity) {
		CJTileEntityMachineBase linkedEntity =
				machineEntity(
						machineEntity.worldObj,
						linked[0], linked[1], linked[2]);

		CJMachineTransferor linkedTransferor =
				(CJMachineTransferor) linkedEntity.impl;

		if(linkedTransferor.lastReceiver != null &&
				linkedEntity.getWorldBlockMetadata() == MULTI_TRANSMIT_ITEMS) {

			if(machineEntity.xCoord == linkedTransferor.lastReceiver[0] &&
					machineEntity.yCoord == linkedTransferor.lastReceiver[1] &&
					machineEntity.zCoord == linkedTransferor.lastReceiver[2]) {

				lastReceiver = null;
				return;
			}
		}

		ItemStack stack = linkedEntity.stacks.get(0);
		if(stack == null) return;

		int currentItemID = stack.getItemID();

		for(IInventory inventory : adjacentInventories) {
			if(inventory == null) continue;

			int slotIndex = CJInventoryHelper.getMatchingInputIndex(
					inventory, currentItemID);

			if(slotIndex == -1) continue;

			linkedTransferor.lastReceiver = new int[] {
					machineEntity.xCoord,
					machineEntity.yCoord,
					machineEntity.zCoord
			};

			ItemStack outStack = inventory.getStackInSlot(slotIndex);

			if(outStack == null) {
				inventory.setInventorySlotContents(
						slotIndex, new ItemStack(
								stack.getItemID(), 1, stack.itemDamage));
			}
			else outStack.stackSize++;

			if(stack.stackSize == 1) {
				linkedEntity.stacks.set(0, null);
			}
			else stack.stackSize--;

			break;
		}
	}

	private void updateTransmitFluid(CJTileEntityMachineBase machineEntity) {
		CJTankVolume volume = machineEntity.tanks.get(0);

		for(CJTileEntityMachineBase adjacent : adjacentMachines) {
			if(adjacent == null) continue;

			for(int j = 0; j < adjacent.tanks.size(); j++) {
				CJTankVolume adjacentVolume = adjacent.tanks.get(j);
				CJTank adjacentTank = adjacent.machineBuilder.tanks.get(j);

				if(!adjacentTank.output && !adjacentTank.bidirectional) {
					continue;
				}

				// TODO: Hardcoded flow rate.
				if(volume.transferFrom(adjacentVolume, 10)) return;
			}
		}
	}

	private void updateReceiveFluid(CJTileEntityMachineBase machineEntity) {
		CJTileEntityMachineBase linkedEntity =
				machineEntity(
						machineEntity.worldObj,
						linked[0], linked[1], linked[2]);

		if(linkedEntity == null) return;

		CJMachineTransferor linkedTransferor =
				(CJMachineTransferor) linkedEntity.impl;

		if(linkedTransferor.lastReceiver != null &&
				linkedEntity.getWorldBlockMetadata() == MULTI_TRANSMIT_FLUIDS) {

			if(machineEntity.xCoord == linkedTransferor.lastReceiver[0] &&
					machineEntity.yCoord == linkedTransferor.lastReceiver[1] &&
					machineEntity.zCoord == linkedTransferor.lastReceiver[2]) {

				lastReceiver = null;
				return;
			}
		}

		CJTankVolume volume = linkedEntity.tanks.get(0);

		for(CJTileEntityMachineBase adjacent : adjacentMachines) {
			if(adjacent == null) continue;

			for(int j = 0; j < adjacent.tanks.size(); j++) {
				CJTankVolume adjacentVolume = adjacent.tanks.get(j);
				CJTank adjacentTank = adjacent.machineBuilder.tanks.get(j);

				if(adjacentTank.output && !adjacentTank.bidirectional) {
					continue;
				}

				if(adjacentVolume.transferFrom(volume, 10)) {
					linkedTransferor.lastReceiver = new int[] {
							machineEntity.xCoord,
							machineEntity.yCoord,
							machineEntity.zCoord
					};

					return;
				}
			}
		}
	}

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		if(adjacentInventories == null || adjacentMachines == null) {
			onNeighbourChange(
					machineEntity.worldObj,
					machineEntity.xCoord,
					machineEntity.yCoord,
					machineEntity.zCoord);
		}

		if(linked == null) return;

		if(machineEntity.coordinateDisplays.get(0) == null) {
			machineEntity.coordinateDisplays.set(
					0, new CJGuiCoordinateDisplay(linked));
		}

		switch(machineEntity.getWorldBlockMetadata()) {
			case MULTI_TRANSMIT_ITEMS:
			case TRANSMIT_ITEMS: {
				updateTransmit(machineEntity);
				break;
			}
			case RECEIVE_ITEMS: {
				updateReceive(machineEntity);
				break;
			}

			case MULTI_TRANSMIT_FLUIDS:
			case TRANSMIT_FLUIDS: {
				updateTransmitFluid(machineEntity);
				break;
			}
			case RECEIVE_FLUIDS: {
				updateReceiveFluid(machineEntity);
				break;
			}

			case INACTIVE:
			default: break;
		}
	}

	@Override
	public void onNeighbourChange(World world, int x, int y, int z) {
		adjacentInventories = CJInventoryHelper.getAdjacentInventories(
				world, x, y, z);

		adjacentMachines = CJInventoryHelper.getAdjacentMachines(
				world, x, y, z);

		if(linked != null) {
			CJTileEntityMachineBase linkedEntity =
					machineEntity(world, linked[0], linked[1], linked[2]);

			for(int i = 0; i < adjacentInventories.length; ++i) {
				IInventory inventory = adjacentInventories[i];
				CJTileEntityMachineBase machine = adjacentMachines[i];

				if(inventory == linkedEntity) {
					adjacentInventories[i] = null;
				}
				else if(machine == linkedEntity) {
					adjacentMachines[i] = null;
				}
			}
		}
	}

	public void breakLink(World worldObj) {
		int[] linkedHold = linked;
		linked = null;

		if(linkedHold != null) {
			CJTileEntityMachineBase linkedEntity =
					machineEntity(
							worldObj,
							linkedHold[0], linkedHold[1], linkedHold[2]);

			if(linkedEntity == null) return;

			CJMachineTransferor other =
					(CJMachineTransferor) linkedEntity.impl;

			other.breakLink(worldObj);
		}
	}

	@Override
	public void onBreak(World world, int x, int y, int z) {
		// TODO: Break multi links.
		breakLink(world);
	}

	@Override
	public void writeToNBT(CompoundTag tagCompound) {
		if(linked == null) return;

		tagCompound.setIntArray(
				"link_position",
				new int[] { linked[0], linked[1], linked[2]});
	}

	@Override
	public void readFromNBT(CompoundTag tagCompound) {
		linked = tagCompound.getIntArray("link_position");

		if(linked != null) {
			if(linked.length == 0) {
				linked = null;
			}
		}
	}
}
