package io.github.chlorojoules;

import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.machine.CJMachineSlotInfo;
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

import static io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase.machineEntity;

public class CJInventoryHelper {
	private static IInventory getAdjacentInventory(
			World world, int x, int y, int z) {

		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if(entity instanceof IInventory) {
			return (IInventory) entity;
		}

		return null;
	}

	public static IInventory[] getAdjacentInventories(
			World world, int x, int y, int z) {

		IInventory[] adjacentInventories = new IInventory[6];

		adjacentInventories[0] = getAdjacentInventory(world, x + 1, y, z);
		adjacentInventories[1] = getAdjacentInventory(world, x - 1, y, z);
		adjacentInventories[2] = getAdjacentInventory(world, x, y + 1, z);
		adjacentInventories[3] = getAdjacentInventory(world, x, y - 1, z);
		adjacentInventories[4] = getAdjacentInventory(world, x, y, z + 1);
		adjacentInventories[5] = getAdjacentInventory(world, x, y, z - 1);

		return adjacentInventories;
	}

	public static CJTileEntityMachineBase[] getAdjacentMachines(
			World world, int x, int y, int z) {

		CJTileEntityMachineBase[] adjacentMachines =
				new CJTileEntityMachineBase[6];

		adjacentMachines[0] = machineEntity(world, x + 1, y, z);
		adjacentMachines[1] = machineEntity(world, x - 1, y, z);
		adjacentMachines[2] = machineEntity(world, x, y + 1, z);
		adjacentMachines[3] = machineEntity(world, x, y - 1, z);
		adjacentMachines[4] = machineEntity(world, x, y, z + 1);
		adjacentMachines[5] = machineEntity(world, x, y, z - 1);

		return adjacentMachines;
	}

	public static int getMatchingOutputIndex(
			IInventory inventory, int itemID) {

		if(inventory instanceof CJTileEntityMachineBase) {
			CJTileEntityMachineBase machine =
					(CJTileEntityMachineBase) inventory;

			for(int i = 0; i < machine.machineBuilder.slots.size(); ++i) {
				CJMachineSlotInfo slot = machine.machineBuilder.slots.get(i);

				if(!slot.output) continue;

				if(machine.stacks.get(i) != null) {
					return i;
				}
			}

			return -1;
		}

		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack != null) {
				if(itemID != -1 && stack.getItemID() != itemID) {
					continue;
				}

				return i;
			}
		}

		return -1;
	}

	public static int getMatchingInputIndex(
			IInventory inventory, int itemID) {

		if(inventory instanceof CJTileEntityMachineBase) {
			CJTileEntityMachineBase machine =
					(CJTileEntityMachineBase) inventory;

			for(int j = 0; j < machine.stacks.size(); j++) {
				if(machine.machineBuilder.slots.get(j).output) {
					continue;
				}

				ItemStack outStack = machine.stacks.get(j);

				if(outStack == null) return j;

				if(outStack.stackSize >= outStack.getMaxStackSize()) {
					continue;
				}

				if(outStack.getItemID() == itemID) return j;
			}

			return -1;
		}

		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);

			if(stack == null) {
				return i;
			}

			if(stack.stackSize >= stack.getMaxStackSize()) continue;

			if(itemID != -1 && stack.getItemID() == itemID) {
				return i;
			}
		}

		return -1;
	}
}
