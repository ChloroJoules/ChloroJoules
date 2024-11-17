package io.github.chlorojoules.client;

import net.minecraft.src.client.gui.Container;
import net.minecraft.src.client.gui.Slot;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.entity.player.InventoryPlayer;
import net.minecraft.src.game.item.ItemStack;

class CJSlotMachineBase extends Slot {
	private final EntityPlayer entityPlayer;

	private boolean output = false;

	public CJSlotMachineBase(
			EntityPlayer entityPlayer, IInventory inventory,
			int index, int x, int y) {

		super(inventory, index, x, y);

		this.entityPlayer = entityPlayer;
	}

	@Override
	public boolean isItemValid(ItemStack item) {
		return !output;
	}

	@Override
	public void onPickupFromSlot(ItemStack item) {
		item.onCrafting(entityPlayer.worldObj, entityPlayer);
		super.onPickupFromSlot(item);
	}

	CJSlotMachineBase setOutput(boolean value) {
		output = value;
		return this;
	}

	boolean isOutput() {
		return output;
	}
}

public class CJContainerMachineBase extends Container {
	private final CJTileEntityMachineBase machine;

	void addPlayerInventory(InventoryPlayer inventory) {
		EntityPlayer player = inventory.player;

		// TODO: Can we de-magic this a little bit?
		for(int row = 0; row < 3; row++) {
			for(int column = 0; column < 9; column++) {
				addSlot(new CJSlotMachineBase(
						player, inventory, column + (row * 9) + 9,
						8 + (column * 18), 84 + (row * 18)));
			}
		}

		for(int i = 0; i < 9; i++) {
			addSlot(new CJSlotMachineBase(
					player, inventory, i, 8 + i * 18, 142));
		}
	}

	public CJContainerMachineBase(
			InventoryPlayer inventoryPlayer, CJTileEntityMachineBase machine) {

		this.machine = machine;

		EntityPlayer player = inventoryPlayer.player;

		// TODO: GuiBuilder API to passthrough from machine creation.
		addSlot(new CJSlotMachineBase(player, machine, 0, 100, 50)
				.setOutput(true));

		addSlot(new CJSlotMachineBase(player, machine, 1, 50, 50));

		addPlayerInventory(inventoryPlayer);
	}

	// TODO: Update machine interface in these two (?).
	@Override
	public void updateInventory() {
		super.updateInventory();
	}

	@Override
	public void func_20112_a(int slot, int arg2) {
		// This is for updating the progress bar (apparently)?
		// arg2 might be the progress amount/delta, but it's unclear.
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityPlayer) {
		return machine.canInteractWith(entityPlayer);
	}

	// TODO: Investigate how quick moves work.
	@Override
	public ItemStack quickMove(int index) {
		return null;
	}
}
