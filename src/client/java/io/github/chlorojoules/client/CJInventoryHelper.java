package io.github.chlorojoules.client;

import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.block.tileentity.TileEntityChest;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;

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

	public static int getMatchingOutputIndex(
			IInventory inventory, int itemID) {

		if(inventory instanceof CJTileEntityMachineBase) {
			CJTileEntityMachineBase machine =
					(CJTileEntityMachineBase) inventory;

			// TODO: Get first non-null slot for multi-output machines.
			return machine.machineBuilder.getPrimaryOutputSlotIndex();
		}

		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack != null) {
				if(itemID != -1 && stack.itemID != itemID) {
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

				if(outStack.itemID == itemID) return j;
			}

			return -1;
		}

		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);

			if(stack == null) {
				return i;
			}

			if(stack.stackSize >= stack.getMaxStackSize()) continue;

			if(itemID != -1 && stack.itemID == itemID) {
				return i;
			}
		}

		return -1;
	}
}
