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

		CJMachineBuilder machineBuilder = machineEntity.getBuilder();

		// Machine inventory to player.
		if(index < machineBuilder.slots.size()) {
			mergeItemStack(
					stack, machineBuilder.slots.size(), slots.size(), true);
		}
		// Player inventory to machine.
		else mergeItemStack(stack);

		if(stack.stackSize == 0) slot.putStack(null);
		else slot.onSlotChanged();

		if(stack.stackSize == returnStack.stackSize) return null;

		return returnStack;
	}

	protected void mergeItemStack(ItemStack item) {
		if(item.isStackable()) {
			for(int i = 0; i < machineEntity.getSizeInventory(); ++i) {
				CJGuiMachineBaseSlot slot =
						(CJGuiMachineBaseSlot) this.slots.get(i);

				ItemStack inStack = slot.getStack();

				if(item.stackSize <= 0) break;

				if(slot.info.output) continue;
				if(!slot.info.isAllowedItem(item)) continue;

				if(inStack != null
						&& inStack.getItemID() == item.getItemID()
						&& (!item.getHasSubtypes() || item.getItemDamage() ==
								inStack.getItemDamage())
						&& ItemStack.areNbtEqual(item, inStack)) {

					int combinedSize = inStack.stackSize + item.stackSize;
					if(combinedSize <= item.getMaxStackSize()) {
						item.stackSize = 0;
						inStack.stackSize = combinedSize;
						slot.onSlotChanged();
					}
					else if(inStack.stackSize < item.getMaxStackSize()) {
						item.stackSize =
								item.stackSize -
								(item.getMaxStackSize() - inStack.stackSize);

						inStack.stackSize = item.getMaxStackSize();
						slot.onSlotChanged();
					}
				}
			}
		}

		if(item.stackSize > 0) {
			for(int i = 0; i < machineEntity.getSizeInventory(); ++i) {
				CJGuiMachineBaseSlot slot =
						(CJGuiMachineBaseSlot) this.slots.get(i);

				if(slot == null) continue;
				if(slot.info.output) continue;
				if(!slot.info.isAllowedItem(item)) continue;

				if(slot.getStack() == null) {
					slot.putStack(item.copy());
					slot.onSlotChanged();
					item.stackSize = 0;
					break;
				}
			}
		}
	}
}
