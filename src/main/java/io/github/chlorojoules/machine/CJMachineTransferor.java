package io.github.chlorojoules.machine;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.IntArrayTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.Tag;
import io.github.chlorojoules.CJInventoryHelper;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.CJGuiCoordinateDisplay;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

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

	public ArrayList<int[]> linked = new ArrayList<>();
	public int lastReceiver = 0;
	public boolean breakingLink = false;

	private boolean isMulti(int meta) {
		return meta > RECEIVE_FLUIDS;
	}

	private boolean tryMultiReceive(
			CJTileEntityMachineBase machineEntity,
			CJTileEntityMachineBase linkedEntity) {

		CJMachineTransferor linkedTransferor =
				(CJMachineTransferor) linkedEntity.impl;

		if(isMulti(linkedEntity.getWorldBlockMetadata())) {
			int[] last =
					linkedTransferor.linked.get(linkedTransferor.lastReceiver);

			return machineEntity.xCoord != last[0] ||
					machineEntity.yCoord != last[1] ||
					machineEntity.zCoord != last[2];
		}

		return false;
	}

	private void didMultiReceive(CJTileEntityMachineBase linkedEntity) {
		CJMachineTransferor linkedTransferor =
				(CJMachineTransferor) linkedEntity.impl;

		if(++linkedTransferor.lastReceiver >=
				linkedTransferor.linked.size()) {

			linkedTransferor.lastReceiver = 0;
		}
	}

	private void updateTransmit(CJTileEntityMachineBase machineEntity) {
		ItemStack stack = machineEntity.stacks.getFirst();
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
						linked.getFirst()[0],
						linked.getFirst()[1],
						linked.getFirst()[2]);

		if(tryMultiReceive(machineEntity, linkedEntity)) return;

		ItemStack stack = linkedEntity.stacks.getFirst();
		if(stack == null) return;

		int currentItemID = stack.getItemID();

		for(IInventory inventory : adjacentInventories) {
			if(inventory == null) continue;

			int slotIndex = CJInventoryHelper.getMatchingInputIndex(
					inventory, currentItemID);

			if(slotIndex == -1) continue;

			didMultiReceive(linkedEntity);

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
		CJTankVolume volume = machineEntity.tanks.getFirst();

		for(IInventory adjacent : adjacentInventories) {
			if(adjacent == null) continue;
			if(!(adjacent instanceof CJTileEntityMachineBase machine)) {
				continue;
			}

			for(int j = 0; j < machine.tanks.size(); j++) {
				CJTankVolume adjacentVolume = machine.tanks.get(j);
				CJTank adjacentTank = machine.machineBuilder.tanks.get(j);

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
						linked.getFirst()[0],
						linked.getFirst()[1],
						linked.getFirst()[2]);

		if(tryMultiReceive(machineEntity, linkedEntity)) return;

		CJTankVolume volume = linkedEntity.tanks.getFirst();

		for(IInventory adjacent : adjacentInventories) {
			if(adjacent == null) continue;
			if(!(adjacent instanceof CJTileEntityMachineBase machine)) {
				continue;
			}

			for(int j = 0; j < machine.tanks.size(); j++) {
				CJTankVolume adjacentVolume = machine.tanks.get(j);
				CJTank adjacentTank = machine.machineBuilder.tanks.get(j);

				if(adjacentTank.output && !adjacentTank.bidirectional) {
					continue;
				}

				if(adjacentVolume.transferFrom(volume, 10)) {
					didMultiReceive(linkedEntity);
					return;
				}
			}
		}
	}

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		int meta = machineEntity.getWorldBlockMetadata();
		if(meta == INACTIVE) return;
		if(linked.isEmpty()) return;

		if(adjacentInventories == null) {
			onNeighbourChange(
					machineEntity.worldObj,
					machineEntity.xCoord,
					machineEntity.yCoord,
					machineEntity.zCoord);
		}

		if(machineEntity.coordinateDisplays.getFirst() == null &&
				!isMulti(machineEntity.getWorldBlockMetadata())) {

			machineEntity.coordinateDisplays.set(
					0, new CJGuiCoordinateDisplay(linked.getFirst()));
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
		adjacentInventories = CJInventoryHelper.getAdjacentInventories(
				world, x, y, z);

		for (int[] link : linked) {
			CJTileEntityMachineBase linkedEntity =
					machineEntity(world, link[0], link[1], link[2]);

			for(int j = 0; j < adjacentInventories.length; ++j) {
				IInventory inventory = adjacentInventories[j];
				if(inventory == linkedEntity) adjacentInventories[j] = null;
			}
		}
	}

	public void breakLink(
			World worldObj, CJTileEntityMachineBase machineEntity) {

		if(breakingLink) return;
		breakingLink = true;

		for(int[] link : linked) {
			CJTileEntityMachineBase linkedEntity =
					machineEntity(worldObj, link[0], link[1], link[2]);

			CJMachineTransferor other =
					(CJMachineTransferor) linkedEntity.impl;

			other.breakLink(worldObj, linkedEntity);
		}

		linked.clear();
		machineEntity.setWorldBlockMetadata(INACTIVE);
		machineEntity.coordinateDisplays.set(0, null);

		breakingLink = false;
	}

	@Override
	public void onBreak(World world, int x, int y, int z) {
		CJTileEntityMachineBase machineEntity =
				machineEntity(world, x, y, z);

		breakLink(world, machineEntity);
	}

	@Override
	public void writeToNBT(CompoundTag tagCompound) {
		ListTag<IntArrayTag> positions = new ListTag<>();
		for(int[] link : linked) {
			positions.setTag(new IntArrayTag(link));
		}

		tagCompound.setTag("link_positions", positions);
	}

	@Override
	public void readFromNBT(CompoundTag tagCompound) {
		ListTag<IntArrayTag> linkedPositions =
				tagCompound.getTagList("link_positions");

		for(IntArrayTag link : linkedPositions) {
			linked.add(link.getIntArray());
		}
	}
}
