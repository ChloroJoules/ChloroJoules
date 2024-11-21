package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJTank;
import io.github.chlorojoules.client.CJTankVolume;
import io.github.chlorojoules.client.CJTileEntityMachineBase;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.block.tileentity.TileEntityChest;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;

import java.util.logging.Logger;

public class CJMachineWhooper implements CJIMachine {
	private CJTileEntityMachineBase[] adjacentMachines = null;
	private TileEntityChest[] adjacentChests = null;

	private TileEntity lastInsert = null;

	public static int getChestMatchingOutputIndex(
			TileEntityChest chest, int itemID) {

		for(int i = 0; i < chest.getSizeInventory(); i++) {
			ItemStack stack = chest.getStackInSlot(i);
			if(stack != null) {
				if(itemID != -1 && stack.itemID != itemID) continue;
				return i;
			}
		}

		return -1;
	}

	public static int getChestMatchingInputIndex(
			TileEntityChest chest, int itemID) {

		for(int i = 0; i < chest.getSizeInventory(); i++) {
			ItemStack stack = chest.getStackInSlot(i);

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

	public static void setInventorySlot(
			TileEntity entity, int index, ItemStack stack) {

		if(entity instanceof CJTileEntityMachineBase) {
			CJTileEntityMachineBase machine = (CJTileEntityMachineBase) entity;

			machine.stacks.set(index, stack);
		}
		else if(entity instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) entity;

			chest.setInventorySlotContents(index, stack);
		}
		else {
			throw new RuntimeException("Unsupported inventory type");
		}
	}

	public static ItemStack getInventorySlot(TileEntity entity, int index) {
		if(entity instanceof CJTileEntityMachineBase) {
			CJTileEntityMachineBase machine = (CJTileEntityMachineBase) entity;

			return machine.stacks.get(index);
		}
		else if(entity instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) entity;

			return chest.getStackInSlot(index);
		}

		return null;
	}

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		if(adjacentMachines == null) {
			adjacentMachines = new CJTileEntityMachineBase[6];
			adjacentChests = new TileEntityChest[6];

			onNeighbourChange(
					machineEntity.worldObj,
					machineEntity.getRegisteredX(),
					machineEntity.getRegisteredY(),
					machineEntity.getRegisteredZ());
		}

		ItemStack stack = machineEntity.stacks.get(0);
		int currentItemID = -1;
		if(stack != null) currentItemID = stack.itemID;

		// Inventory to Whooper.
		int inInventory = -1;
		for(int i = 0; i < adjacentMachines.length; ++i) {
			ItemStack inStack = null;
			int slotIndex = -1;

			CJTileEntityMachineBase adjacentMachine = adjacentMachines[i];
			TileEntityChest adjacentChest = adjacentChests[i];

			// TODO: How can we de-duplicate this section?
			if(adjacentMachine != null) {
				if(adjacentMachine == lastInsert) continue;

				// TODO: Get first non-null slot for multi-output machines.
				slotIndex = adjacentMachine.machineBuilder
						.getPrimaryOutputSlotIndex();

				if(slotIndex == -1) continue;
				inStack = adjacentMachine.stacks.get(slotIndex);
			}
			else if(adjacentChest != null) {
				if(adjacentChest == lastInsert) continue;

				slotIndex = getChestMatchingOutputIndex(
								adjacentChest, currentItemID);

				if(slotIndex == -1) continue;
				inStack = adjacentChest.getStackInSlot(slotIndex);
			}

			if(inStack == null) continue;

			// TODO: This is currently hardcoded to 1 item per tick.
			if(stack == null) {
				machineEntity.stacks.set(0, new ItemStack(
						inStack.itemID, 1, inStack.itemDamage));
			}
			else {
				stack.stackSize++;
			}

			if(adjacentMachine != null) {
				// TODO: Get first non-null slot for multi-output machines.
				//       Probably want something similar to the Flooper inner
				//       Loop here to "try" and transfer from each available
				//       Machine/Chest slot into the Whooper.
				if(inStack.stackSize == 1) {
					adjacentMachine.stacks.set(slotIndex, null);
				}
				else inStack.stackSize--;
			}
			else if(adjacentChest != null) {
				if(inStack.stackSize == 1) {
					adjacentChest.setInventorySlotContents(slotIndex, null);
				}
				else inStack.stackSize--;
			}

			inInventory = i;
			break;
		}

		if(stack == null) return;

		// Whooper to inventory.
		// TODO: This should use `quickMove` so the tile can filter which slot
		//       To insert into. This means we need to sort out machine
		//       Inventory quick move.
		boolean didInsert = false;
		for(int i = 0; i < adjacentMachines.length; ++i) {
			ItemStack outStack = null;
			int slotIndex = -1;

			if(i == inInventory) continue;

			CJTileEntityMachineBase adjacentMachine = adjacentMachines[i];
			TileEntityChest adjacentChest = adjacentChests[i];

			if(adjacentMachine != null) {
				for(int j = 0; j < adjacentMachine.stacks.size(); j++) {
					if(adjacentMachine.machineBuilder.slots.get(j).output) {
						continue;
					}

					outStack = adjacentMachine.stacks.get(j);

					if(outStack == null) {
						slotIndex = j;
						break;
					}

					if(outStack.stackSize >= outStack.getMaxStackSize()) {
						continue;
					}

					if(outStack.itemID == stack.itemID) {
						slotIndex = j;
						break;
					}
				}

				if(slotIndex == -1) continue;

				if(outStack == null) {
					adjacentMachine.stacks.set(
							slotIndex, new ItemStack(
									stack.itemID, 1, stack.itemDamage));
				}
				else {
					outStack.stackSize++;
				}

				lastInsert = adjacentMachine;
				didInsert = true;
			}
			else if(adjacentChest != null) {
				slotIndex = getChestMatchingInputIndex(
						adjacentChest, currentItemID);

				if(slotIndex == -1) continue;
				outStack = adjacentChest.getStackInSlot(slotIndex);

				if(outStack == null) {
					adjacentChest.setInventorySlotContents(
							slotIndex, new ItemStack(
									stack.itemID, 1, stack.itemDamage));
				}
				else {
					outStack.stackSize++;
				}

				lastInsert = adjacentChest;
				didInsert = true;
			}

			if(!didInsert) continue;

			if(stack.stackSize == 1) {
				machineEntity.stacks.set(0, null);
			}
			else stack.stackSize--;

			break;
		}
	}

	private void setAdjacentTileEntity(
			int index, World world, int x, int y, int z) {

		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if(tileEntity instanceof CJTileEntityMachineBase) {
			adjacentMachines[index] = (CJTileEntityMachineBase) tileEntity;
		}
		else {
			adjacentMachines[index] = null;
		}

		// TODO: Handle generic inventories vs. Vanilla machines etc..
		if(tileEntity instanceof TileEntityChest) {
			adjacentChests[index] = (TileEntityChest) tileEntity;
		}
		else {
			adjacentChests[index] = null;
		}
	}

	@Override
	public void onNeighbourChange(World world, int x, int y, int z) {
		if(adjacentMachines == null) return;

		setAdjacentTileEntity(0, world, x + 1, y, z);
		setAdjacentTileEntity(1, world, x - 1, y, z);
		setAdjacentTileEntity(2, world, x, y + 1, z);
		setAdjacentTileEntity(3, world, x, y - 1, z);
		setAdjacentTileEntity(4, world, x, y, z + 1);
		setAdjacentTileEntity(5, world, x, y, z - 1);
	}
}
