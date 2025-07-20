package io.github.chlorojoules.container;

import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.CJGuiMachineBaseSlot;
import io.github.chlorojoules.machine.CJMachineBuilder;
import io.github.chlorojoules.machine.CJMachineSlotInfo;

import net.minecraft.common.block.container.Container;
import net.minecraft.common.block.container.Slot;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.entity.player.InventoryPlayer;
import net.minecraft.common.item.ItemStack;

import java.util.ArrayList;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJContainerMachineBase extends Container {
	private final CJTileEntityMachineBase machineEntity;

	public ArrayList<CJTank> tanks = new ArrayList<>();

	void addPlayerInventory(InventoryPlayer inventory) {
		for(int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
			for(int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++) {
				addSlot(new CJGuiMachineBaseSlot(
						inventory,
						column +
								(row * PLAYER_INVENTORY_COLUMNS) +
								PLAYER_INVENTORY_COLUMNS,
						PLAYER_INVENTORY_X + (column * SLOT_IN_WIDTH),
						PLAYER_INVENTORY_Y + (row * SLOT_IN_WIDTH), null));
			}
		}

		for(int i = 0; i < PLAYER_INVENTORY_COLUMNS; i++) {
			addSlot(new CJGuiMachineBaseSlot(
					inventory, i, PLAYER_INVENTORY_X + i * SLOT_IN_WIDTH,
					PLAYER_INVENTORY_HOTBAR_Y, null));
		}
	}

	public CJContainerMachineBase(
			InventoryPlayer inventoryPlayer, CJTileEntityMachineBase entity) {

		machineEntity = entity;

		CJMachineBuilder machineBuilder = entity.machine.machineBuilder;
		for(int i = 0; i < machineBuilder.slots.size(); i++) {
			CJMachineSlotInfo info = machineBuilder.slots.get(i);

			if(info.checkDamageExclusive(machineEntity)) continue;

			CJGuiMachineBaseSlot slot =
					new CJGuiMachineBaseSlot(
							machineEntity, i,
							info.getXPlacement(), info.getYPlacement(),
							info);

			addSlot(slot);
		}

		for(int i = 0; i < machineBuilder.tanks.size(); ++i) {
			CJTank tank = machineBuilder.tanks.get(i);

			if(tank.checkDamageExclusive(machineEntity)) continue;

			tanks.add(tank);
		}

		addPlayerInventory(inventoryPlayer);
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityPlayer) {
		return machineEntity.canInteractWith(entityPlayer);
	}

	@Override
	public ItemStack quickMove(int index) {
		Slot slot = slots.get(index);
		if(!slot.getHasStack()) return null;

		ItemStack stack = slot.getStack();
		ItemStack returnStack = stack.copy();

		CJMachineBuilder machineBuilder = machineEntity.machine.machineBuilder;

		// Machine inventory to player.
		if(index < machineBuilder.slots.size()) {
			mergeItemStack(
					stack, machineBuilder.slots.size(), slots.size(), true);
		}
		// Player inventory to machine.
		else {
			boolean didJewel = false;
			if(machineBuilder.jewelSlotIndex != -1) {
				Slot jewelSlot = slots.get(machineBuilder.jewelSlotIndex);

				if(!jewelSlot.getHasStack()) {
					jewelSlot.putStack(stack.copy());
					stack.stackSize = 0;
					didJewel = true;
				}
			}

			if(!didJewel) {
				// TODO: Need to override this to only consider jewel slots if
				//		 Item is a jewel.
				// TODO: Need to override this to not allow insertion into
				//		 Output slots. If we require that jewel slot is first
				//		 And output slots are last we can just clip them out
				//		 Of this range.
				mergeItemStack(stack, 0, machineBuilder.slots.size(), false);
			}
		}

		if(stack.stackSize == 0) slot.putStack(null);
		else slot.onSlotChanged();

		if(stack.stackSize == returnStack.stackSize) return null;

		return returnStack;
	}
}
