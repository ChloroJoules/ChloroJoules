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
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

import java.util.ArrayList;
import java.util.Arrays;

import static io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase.machineEntity;

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

	private boolean tryMultiReceive(
			CJTileEntityMachineBase machineEntity,
			CJTileEntityMachineBase linkedEntity) {

		if(isMulti(linkedEntity.getWorldBlockMetadata())) {
			int[] last = linkedEntity.linked.get(linkedEntity.next);

			return machineEntity.xCoord != last[0] ||
					machineEntity.yCoord != last[1] ||
					machineEntity.zCoord != last[2];
		}

		return false;
	}

	private void updateTransmit(CJTileEntityMachineBase machineEntity) {
		ItemStack stack = machineEntity.getStackInSlot(0);
		int currentItemID = -1;

		if(++machineEntity.next >= machineEntity.linked.size()) machineEntity.next = 0;

		if(stack != null) {
			if(stack.stackSize >= stack.getMaxStackSize()) return;

			currentItemID = stack.getItemID();
		}

		ItemStack filterStack = machineEntity.getStackInSlot(1);

		for(TileEntity tileEntity : machineEntity.adjacentTileEntities) {
			if(tileEntity == null) continue;
			if(!(tileEntity instanceof IInventory inventory)) continue;

			int slotIndex = CJInventoryHelper.getMatchingOutputIndex(
					inventory, currentItemID, filterStack,
					machineEntity.buttonStates.getFirst());

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
		CJTileEntityMachineBase linkedEntity =
				machineEntity(
						machineEntity.worldObj,
						machineEntity.linked.getFirst()[0],
						machineEntity.linked.getFirst()[1],
						machineEntity.linked.getFirst()[2]);

		if(tryMultiReceive(machineEntity, linkedEntity)) return;

		ItemStack stack = linkedEntity.getStackInSlot(0);
		if(stack == null) return;

		int currentItemID = stack.getItemID();

		for(TileEntity tileEntity : machineEntity.adjacentTileEntities) {
			if(tileEntity == null) continue;
			if(!(tileEntity instanceof IInventory inventory)) continue;

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
		CJTankVolume volume = machineEntity.tanks.getFirst();

		if(++machineEntity.next >= machineEntity.linked.size()) machineEntity.next = 0;

		for(TileEntity adjacent : machineEntity.adjacentTileEntities) {
			if(adjacent == null) continue;
			if(!(adjacent instanceof CJTileEntityMachineBase machine)) {
				continue;
			}
			if(machine.impl instanceof CJMachineTransferor) continue;

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
		CJTileEntityMachineBase linkedEntity =
				machineEntity(
						machineEntity.worldObj,
						machineEntity.linked.getFirst()[0],
						machineEntity.linked.getFirst()[1],
						machineEntity.linked.getFirst()[2]);

		if(tryMultiReceive(machineEntity, linkedEntity)) return;

		CJTankVolume volume = linkedEntity.tanks.getFirst();

		for(TileEntity adjacent : machineEntity.adjacentTileEntities) {
			if(adjacent == null) continue;
			if(!(adjacent instanceof CJTileEntityMachineBase machine)) {
				continue;
			}
			if(machine.impl instanceof CJMachineTransferor) continue;

			CJMachineBuilder adjacentMachineBuilder = machine.getBuilder();

			for(int j = 0; j < machine.tanks.size(); j++) {
				CJTankVolume adjacentVolume = machine.tanks.get(j);
				CJTank adjacentTank = adjacentMachineBuilder.tanks.get(j);

				if(adjacentTank.checkDamageExclusive(machine)) continue;
				if(adjacentTank.output && !adjacentTank.bidirectional) {
					continue;
				}

				// TODO: Generalize this for locked volumes being prioritised
				//		 For fluid inputs.
				if(!adjacentTank.id.equals("fuel") &&
						adjacentMachineBuilder.hasNamedTank("fuel") &&
						volume.fluidID == CJMod.fuelFluid) {

					continue;
				}

				if(adjacentVolume.transferFrom(volume, FLOW_RATE)) return;
				// Don't try to fill multiple tanks with the same fluid.
				if(adjacentVolume.fluidID == volume.fluidID) return;
			}
		}
	}

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		int meta = machineEntity.getWorldBlockMetadata();
		if(meta == INACTIVE) return;
		if(machineEntity.linked.isEmpty()) return;

		if(machineEntity.coordinateDisplays.getFirst() == null &&
				!isMulti(machineEntity.getWorldBlockMetadata())) {

			machineEntity.coordinateDisplays.set(
					0, new CJGuiCoordinateDisplay(
							machineEntity.linked.getFirst()));
		}

		if(machineEntity.isPaused) return;

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

	public static void breakLink(
			World worldObj, CJTileEntityMachineBase machineEntity) {

		if(machineEntity.breakingLink) return;
		machineEntity.breakingLink = true;

		ArrayList<int[]> linkedCopy = new ArrayList<>();
		for(int[] link : machineEntity.linked) linkedCopy.add(link.clone());

		for(int[] link : linkedCopy) {
			CJTileEntityMachineBase linkedEntity =
					machineEntity(worldObj, link[0], link[1], link[2]);

			if(isMulti(linkedEntity.getWorldBlockMetadata())) {
				linkedEntity.linked.removeIf(value -> Arrays.equals(
						value, machineEntity.getWorldPosition()));

				if(linkedEntity.next >= linkedEntity.linked.size()) {
					linkedEntity.next = 0;
				}
			}
			else breakLink(worldObj, linkedEntity);
		}

		machineEntity.linked.clear();
		machineEntity.setWorldBlockMetadata(INACTIVE);
		machineEntity.coordinateDisplays.set(0, null);

		machineEntity.breakingLink = false;
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

		for(int[] link : machineEntity.linked) {
			positions.setTag(new IntArrayTag(link));
		}

		tagCompound.setTag("link_positions", positions);
	}

	@Override
	public void readFromNBT(
			CJTileEntityMachineBase machineEntity, CompoundTag tagCompound) {

		ListTag<IntArrayTag> linkedPositions =
				tagCompound.getTagList("link_positions");

		for(IntArrayTag link : linkedPositions) {
			machineEntity.linked.add(link.getIntArray());
		}
	}

	@Override
	public boolean canPause(CJTileEntityMachineBase machineEntity) {
		int damage = machineEntity.getWorldBlockMetadata();

		return damage == TRANSMIT_ITEMS ||
				damage == TRANSMIT_FLUIDS ||
				damage == MULTI_TRANSMIT_ITEMS ||
				damage == MULTI_TRANSMIT_FLUIDS;
	}
}
