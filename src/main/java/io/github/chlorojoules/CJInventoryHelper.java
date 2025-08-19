package io.github.chlorojoules;

import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.machine.CJMachineBuilder;
import io.github.chlorojoules.machine.CJMachineSlotInfo;
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

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

	private static boolean doesMatchFilter(
			IInventory inventory, int index, int itemID, ItemStack filter,
			boolean blacklist) {

		ItemStack stack = inventory.getStackInSlot(index);
		if(stack != null) {
			if(itemID != -1 && stack.getItemID() != itemID) {
				return false;
			}

			if(filter != null) {
				if(blacklist) {
					return stack.getItemID() != filter.getItemID();
				}
				else {
					return stack.getItemID() == filter.getItemID();
				}
			}

			return true;
		}

		return false;
	}

	public static int getMatchingOutputIndex(
			IInventory inventory, int itemID, ItemStack filter,
			boolean blacklist) {

		if(inventory instanceof CJTileEntityMachineBase machine) {
			CJMachineBuilder builder = machine.machine.machineBuilder;
			for(int i = 0; i < builder.slots.size(); ++i) {
				CJMachineSlotInfo slot = builder.slots.get(i);

				if(!slot.output) continue;

				if(doesMatchFilter(machine, i, itemID, filter, blacklist)) {
					return i;
				}
			}

			return -1;
		}

		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			if(doesMatchFilter(inventory, i, itemID, filter, blacklist)) {
				return i;
			}
		}

		return -1;
	}

	public static int getMatchingInputIndex(
			IInventory inventory, int itemID) {

		if(inventory instanceof CJTileEntityMachineBase machine) {
			CJMachineBuilder machineBuilder =
					machine.machine.machineBuilder;

			for(int j = 0; j < machine.getSizeInventory(); j++) {
				CJMachineSlotInfo slot = machineBuilder.slots.get(j);
				if(slot.output || slot.checkDamageExclusive(machine)) {
					continue;
				}

				ItemStack outStack = machine.getStackInSlot(j);

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
