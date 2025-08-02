package io.github.chlorojoules.machine;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.IntArrayTag;
import com.mojang.nbt.ListTag;
import io.github.chlorojoules.CJInventoryHelper;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.CJGuiCoordinateDisplay;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

import java.util.ArrayList;
import java.util.Arrays;

import static io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase.machineEntity;

class CJMachineTransferorStorage {
	public IInventory[] adjacentInventories = null;

	public ArrayList<int[]> linked = new ArrayList<>();
	public int next = 0;
	public boolean breakingLink = false;
}

public class CJMachineTransferor implements CJIMachine {
	private static final int FLOW_RATE = 100;

	// Damage states.
	public static final int INACTIVE = 0;
	public static final int TRANSMIT_ITEMS = 1;
	public static final int RECEIVE_ITEMS = 2;
	public static final int TRANSMIT_FLUIDS = 3;
	public static final int RECEIVE_FLUIDS = 4;
	public static final int MULTI_TRANSMIT_ITEMS = 5;
	public static final int MULTI_TRANSMIT_FLUIDS = 6;

	private static boolean isMulti(int meta) {
		return meta > RECEIVE_FLUIDS;
	}

	private static CJMachineTransferorStorage getStorage(
			CJTileEntityMachineBase machineEntity) {

		if(machineEntity.machineStorage == null) {
			machineEntity.machineStorage = new CJMachineTransferorStorage();
		}

		return (CJMachineTransferorStorage) machineEntity.machineStorage;
	}

	public static ArrayList<int[]> getLinked(
			CJTileEntityMachineBase machineEntity) {

		return getStorage(machineEntity).linked;
	}

	private boolean tryMultiReceive(
			CJTileEntityMachineBase machineEntity,
			CJTileEntityMachineBase linkedEntity) {

		CJMachineTransferorStorage linkedStorage = getStorage(linkedEntity);

		if(isMulti(linkedEntity.getWorldBlockMetadata())) {
			int[] last = linkedStorage.linked.get(linkedStorage.next);

			return machineEntity.xCoord == last[0] &&
					machineEntity.yCoord == last[1] &&
					machineEntity.zCoord == last[2];
		}

		return true;
	}

	private void updateTransmit(CJTileEntityMachineBase machineEntity) {
		CJMachineTransferorStorage storage = getStorage(machineEntity);

		ItemStack stack = machineEntity.getStackInSlot(0);
		int currentItemID = -1;

		if(++storage.next >= storage.linked.size()) storage.next = 0;

		if(stack != null) {
			if(stack.stackSize >= stack.getMaxStackSize()) return;

			currentItemID = stack.getItemID();
		}

		for(IInventory inventory : storage.adjacentInventories) {
			if(inventory == null) continue;

			int slotIndex = CJInventoryHelper.getMatchingOutputIndex(
					inventory, currentItemID);

			if(slotIndex == -1) continue;
			ItemStack inStack = inventory.getStackInSlot(slotIndex);

			if(inStack == null) continue;

			// TODO: This is currently hardcoded to 1 item per tick.
			if(stack == null) {
				machineEntity.setInventorySlotContents(0, new ItemStack(
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
		CJMachineTransferorStorage storage = getStorage(machineEntity);

		CJTileEntityMachineBase linkedEntity =
				machineEntity(
						machineEntity.worldObj,
						storage.linked.getFirst()[0],
						storage.linked.getFirst()[1],
						storage.linked.getFirst()[2]);

		if(!tryMultiReceive(machineEntity, linkedEntity)) return;

		ItemStack stack = linkedEntity.getStackInSlot(0);
		if(stack == null) return;

		int currentItemID = stack.getItemID();

		for(IInventory inventory : storage.adjacentInventories) {
			if(inventory == null) continue;

			int slotIndex = CJInventoryHelper.getMatchingInputIndex(
					inventory, currentItemID);

			if(slotIndex == -1) continue;

			ItemStack outStack = inventory.getStackInSlot(slotIndex);

			if(outStack == null) {
				inventory.setInventorySlotContents(
						slotIndex, new ItemStack(
								stack.getItemID(), 1, stack.itemDamage));
			}
			else outStack.stackSize++;

			if(stack.stackSize == 1) {
				linkedEntity.setInventorySlotContents(0, null);
			}
			else stack.stackSize--;

			break;
		}
	}

	private void updateTransmitFluid(CJTileEntityMachineBase machineEntity) {
		CJMachineTransferorStorage storage = getStorage(machineEntity);

		CJTankVolume volume = machineEntity.tanks.getFirst();

		if(++storage.next >= storage.linked.size()) storage.next = 0;

		for(IInventory adjacent : storage.adjacentInventories) {
			if(adjacent == null) continue;
			if(!(adjacent instanceof CJTileEntityMachineBase machine)) {
				continue;
			}

			for(int j = 0; j < machine.tanks.size(); j++) {
				CJTankVolume adjacentVolume = machine.tanks.get(j);
				CJTank adjacentTank = machine.getBuilder().tanks.get(j);

				if(adjacentTank.checkDamageExclusive(machine)) continue;
				if(!adjacentTank.output && !adjacentTank.bidirectional) {
					continue;
				}

				if(volume.transferFrom(adjacentVolume, FLOW_RATE)) return;
			}
		}
	}

	private void updateReceiveFluid(CJTileEntityMachineBase machineEntity) {
		CJMachineTransferorStorage storage = getStorage(machineEntity);

		CJTileEntityMachineBase linkedEntity =
				machineEntity(
						machineEntity.worldObj,
						storage.linked.getFirst()[0],
						storage.linked.getFirst()[1],
						storage.linked.getFirst()[2]);

		if(!tryMultiReceive(machineEntity, linkedEntity)) return;

		CJTankVolume volume = linkedEntity.tanks.getFirst();

		for(IInventory adjacent : storage.adjacentInventories) {
			if(adjacent == null) continue;
			if(!(adjacent instanceof CJTileEntityMachineBase machine)) {
				continue;
			}

			CJMachineBuilder adjacentMachineBuilder = machine.getBuilder();

			for(int j = 0; j < machine.tanks.size(); j++) {
				CJTankVolume adjacentVolume = machine.tanks.get(j);
				CJTank adjacentTank = adjacentMachineBuilder.tanks.get(j);

				if(adjacentTank.checkDamageExclusive(machine)) continue;
				if(adjacentTank.output && !adjacentTank.bidirectional) {
					continue;
				}

				if(!adjacentTank.id.equals("fuel") &&
						adjacentMachineBuilder.hasNamedTank("fuel") &&
						volume.fluidID == CJMod.fuelFluid) {

					continue;
				}

				if(adjacentVolume.transferFrom(volume, FLOW_RATE)) return;
			}
		}
	}

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		CJMachineTransferorStorage storage = getStorage(machineEntity);

		int meta = machineEntity.getWorldBlockMetadata();
		if(meta == INACTIVE) return;
		if(storage.linked.isEmpty()) return;

		if(storage.adjacentInventories == null) {
			onNeighbourChange(
					machineEntity.worldObj,
					machineEntity.xCoord,
					machineEntity.yCoord,
					machineEntity.zCoord);
		}

		if(machineEntity.coordinateDisplays.getFirst() == null &&
				!isMulti(machineEntity.getWorldBlockMetadata())) {

			machineEntity.coordinateDisplays.set(
					0, new CJGuiCoordinateDisplay(storage.linked.getFirst()));
		}

		switch(meta) {
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

			default: break;
		}
	}

	@Override
	public void onNeighbourChange(World world, int x, int y, int z) {
		CJTileEntityMachineBase machineEntity = machineEntity(world, x, y, z);
		CJMachineTransferorStorage storage = getStorage(machineEntity);

		storage.adjacentInventories =
				CJInventoryHelper.getAdjacentInventories(world, x, y, z);

		for(int i = 0; i < storage.adjacentInventories.length; ++i) {
			if(storage.adjacentInventories[i] instanceof
					CJTileEntityMachineBase adjacentEntity) {

				if(adjacentEntity.impl instanceof CJMachineTransferor) {
					storage.adjacentInventories[i] = null;
				}
			}
		}
	}

	public static void breakLink(
			World worldObj, CJTileEntityMachineBase machineEntity) {

		CJMachineTransferorStorage storage = getStorage(machineEntity);

		if(storage.breakingLink) return;
		storage.breakingLink = true;

		ArrayList<int[]> linkedCopy = new ArrayList<>();
		for(int[] link : storage.linked) linkedCopy.add(link.clone());

		for(int[] link : linkedCopy) {
			CJTileEntityMachineBase linkedEntity =
					machineEntity(worldObj, link[0], link[1], link[2]);

			CJMachineTransferorStorage linkedStorage =
					getStorage(linkedEntity);

			if(isMulti(linkedEntity.getWorldBlockMetadata())) {
				linkedStorage.linked.removeIf(value -> Arrays.equals(
						value, machineEntity.getWorldPosition()));

				if(linkedStorage.next >= linkedStorage.linked.size()) {
					linkedStorage.next = 0;
				}
			}
			else breakLink(worldObj, linkedEntity);
		}

		storage.linked.clear();
		machineEntity.setWorldBlockMetadata(INACTIVE);
		machineEntity.coordinateDisplays.set(0, null);

		storage.breakingLink = false;
	}

	@Override
	public void onBreak(World world, int x, int y, int z) {
		CJTileEntityMachineBase machineEntity =
				machineEntity(world, x, y, z);

		breakLink(world, machineEntity);
	}

	@Override
	public void writeToNBT(
			CJTileEntityMachineBase machineEntity, CompoundTag tagCompound) {

		ListTag<IntArrayTag> positions = new ListTag<>();
		CJMachineTransferorStorage storage = getStorage(machineEntity);

		for(int[] link : storage.linked) {
			positions.setTag(new IntArrayTag(link));
		}

		tagCompound.setTag("link_positions", positions);
	}

	@Override
	public void readFromNBT(
			CJTileEntityMachineBase machineEntity, CompoundTag tagCompound) {

		ListTag<IntArrayTag> linkedPositions =
				tagCompound.getTagList("link_positions");

		CJMachineTransferorStorage storage = getStorage(machineEntity);

		for(IntArrayTag link : linkedPositions) {
			storage.linked.add(link.getIntArray());
		}
	}
}
