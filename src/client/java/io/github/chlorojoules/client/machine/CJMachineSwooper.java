package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJInventoryHelper;
import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;

public class CJMachineSwooper implements CJIMachine {
	private IInventory[] adjacentInventories = null;

	// TODO: Avoid exporting back to linked device.
	public CJTileEntityMachineBase linked = null;

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		if(adjacentInventories == null) {
			onNeighbourChange(
					machineEntity.worldObj,
					machineEntity.getRegisteredX(),
					machineEntity.getRegisteredY(),
					machineEntity.getRegisteredZ());
		}

		if(linked == null) return;

		ItemStack stack = linked.stacks.get(0);
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
				linked.stacks.set(0, null);
			}
			else stack.stackSize--;

			break;
		}
	}

	@Override
	public void onNeighbourChange(World world, int x, int y, int z) {
		adjacentInventories = CJInventoryHelper.getAdjacentInventories(
				world, x, y, z);
	}
}
