package io.github.chlorojoules;

import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.machine.CJMachineBuilder;
import io.github.chlorojoules.machine.CJMachineSlotInfo;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.item.ItemStack;

public class CJInventoryHelper {
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
