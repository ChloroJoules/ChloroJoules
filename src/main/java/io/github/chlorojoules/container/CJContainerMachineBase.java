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

public class CJContainerMachineBase extends Container {
	private final CJTileEntityMachineBase machineEntity;

	public ArrayList<CJTank> tanks = new ArrayList<>();

	void addPlayerInventory(InventoryPlayer inventory) {
		EntityPlayer player = inventory.player;

		// TODO: Can we de-magic this a little bit?
		for(int row = 0; row < 3; row++) {
			for(int column = 0; column < 9; column++) {
				addSlot(new CJGuiMachineBaseSlot(
						inventory, column + (row * 9) + 9,
						8 + (column * 18), 84 + (row * 18), null));
			}
		}

		for(int i = 0; i < 9; i++) {
			addSlot(new CJGuiMachineBaseSlot(
					inventory, i, 8 + i * 18, 142, null));
		}
	}

	public CJContainerMachineBase(
			InventoryPlayer inventoryPlayer, CJTileEntityMachineBase entity) {

		machineEntity = entity;

		EntityPlayer player = inventoryPlayer.player;

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
