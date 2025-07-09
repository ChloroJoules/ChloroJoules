package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJInventoryHelper;
import io.github.chlorojoules.client.CJTank;
import io.github.chlorojoules.client.CJTankVolume;
import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.client.gui.CJGuiCoordinateDisplay;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;

import static io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase.machineEntity;

public class CJMachineTransferor implements CJIMachine {
	// Damage states.
	public static final int INACTIVE = 0;
	public static final int TRANSMIT_ITEMS = 1;
	public static final int RECEIVE_ITEMS = 2;
	public static final int TRANSMIT_FLUIDS = 3;
	public static final int RECEIVE_FLUIDS = 4;
	public static final int MAX_DAMAGE = RECEIVE_FLUIDS;

	private IInventory[] adjacentInventories = null;
	private CJTileEntityMachineBase[] adjacentMachines = null;

	public int[] linked = null;

	private void updateTransmit(CJTileEntityMachineBase machineEntity) {
		ItemStack stack = machineEntity.stacks.get(0);
		int currentItemID = -1;

		if(stack != null) {
			if(stack.stackSize >= stack.getMaxStackSize()) return;

			currentItemID = stack.itemID;
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
						inStack.itemID, 1, inStack.itemDamage));
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

		ItemStack stack = linkedEntity.stacks.get(0);
		if(stack == null) return;

		int currentItemID = stack.itemID;

		// TODO: This should use `quickMove` so the tile can filter which slot
		//       To insert into. This means we need to sort out machine
		//       Inventory quick move.
		for(IInventory inventory : adjacentInventories) {
			if(inventory == null) continue;

			int slotIndex = CJInventoryHelper.getMatchingInputIndex(
					inventory, currentItemID);

			if(slotIndex == -1) continue;

			ItemStack outStack = inventory.getStackInSlot(slotIndex);

			if(outStack == null) {
				inventory.setInventorySlotContents(
						slotIndex, new ItemStack(
								stack.itemID, 1, stack.itemDamage));
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

				if(!adjacentTank.output) continue;

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

		CJTankVolume volume = linkedEntity.tanks.get(0);

		for(CJTileEntityMachineBase adjacent : adjacentMachines) {
			if(adjacent == null) continue;

			for(int j = 0; j < adjacent.tanks.size(); j++) {
				CJTankVolume adjacentVolume = adjacent.tanks.get(j);
				CJTank adjacentTank = adjacent.machineBuilder.tanks.get(j);

				if(adjacentTank.output) continue;

				if(adjacentVolume.transferFrom(volume, 10)) return;
			}
		}
	}

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		if(adjacentInventories == null || adjacentMachines == null) {
			onNeighbourChange(
					machineEntity.worldObj,
					machineEntity.getRegisteredX(),
					machineEntity.getRegisteredY(),
					machineEntity.getRegisteredZ());
		}

		if(linked == null) return;

		if(machineEntity.coordinateDisplays.get(0) == null) {
			machineEntity.coordinateDisplays.set(
					0, new CJGuiCoordinateDisplay(linked));
		}

		switch(machineEntity.getWorldBlockMetadata()) {
			case TRANSMIT_ITEMS: {
				updateTransmit(machineEntity);
				break;
			}
			case RECEIVE_ITEMS: {
				updateReceive(machineEntity);
				break;
			}

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

			CJMachineTransferor other =
					(CJMachineTransferor) linkedEntity.impl;

			other.breakLink(worldObj);
		}
	}

	@Override
	public void onBreak(World world, int x, int y, int z) {
		breakLink(world);
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		tagCompound.setIntArray(
				"link_position",
				new int[] {
						linked[0],
						linked[1],
						linked[2]});
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		linked = tagCompound.getIntArray("link_position");
	}

/*

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		// TODO: Add init method which takes the `TileEntity`.
		if(adjacentMachines == null) {
			adjacentMachines = new CJTileEntityMachineBase[6];
			onNeighbourChange(
					machineEntity.worldObj,
					machineEntity.getRegisteredX(),
					machineEntity.getRegisteredY(),
					machineEntity.getRegisteredZ());
		}

		//       From -- make creating flooper chains easier.

		CJTankVolume volume = machineEntity.tanks.get(0);

		// Machine to Flooper.
		boolean didTransfer = false;
		int inTank = -1;
		for(int i = 0; i < adjacentMachines.length; ++i) {
			CJTileEntityMachineBase adjacent = adjacentMachines[i];
			if(adjacent == null) continue;

			for(int j = 0; j < adjacent.tanks.size(); j++) {
				CJTankVolume adjacentVolume = adjacent.tanks.get(j);
				CJTank adjacentTank = adjacent.machineBuilder.tanks.get(j);

				if(!adjacentTank.output) continue;

				if(tankTransfer(adjacentVolume, volume, 10)) {
					inTank = i;
					didTransfer = true;
					break;
				}
			}

			if(didTransfer) break;
		}

		// Flooper to machine.
		didTransfer = false;
		for(int i = 0; i < adjacentMachines.length; ++i) {
			CJTileEntityMachineBase adjacent = adjacentMachines[i];
			if(adjacent == null) continue;
			if(i == inTank) continue;

			for(int j = 0; j < adjacent.tanks.size(); j++) {
				CJTankVolume adjacentVolume = adjacent.tanks.get(j);
				CJTank adjacentTank = adjacent.machineBuilder.tanks.get(j);

				if(adjacentTank.output) continue;

				if(tankTransfer(volume, adjacentVolume, 10)) {
					didTransfer = true;
					break;
				}
			}

			if(didTransfer) break;
		}
	}
*/
}
