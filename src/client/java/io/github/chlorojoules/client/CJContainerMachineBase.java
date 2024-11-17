package io.github.chlorojoules.client;

import net.minecraft.src.client.gui.Container;
import net.minecraft.src.client.gui.Slot;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.entity.player.InventoryPlayer;
import net.minecraft.src.game.item.ItemStack;

class CJSlotMachineBaseOutput extends Slot {
	private final EntityPlayer entityPlayer;

	public CJSlotMachineBaseOutput(
			EntityPlayer entityPlayer, IInventory inventory,
			int index, int x, int y) {

		super(inventory, index, x, y);
		this.entityPlayer = entityPlayer;
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		return false;
	}

	@Override
	public void onPickupFromSlot(ItemStack itemStack) {
		itemStack.onCrafting(entityPlayer.worldObj, entityPlayer);
		super.onPickupFromSlot(itemStack);
	}
}

public class CJContainerMachineBase extends Container {
	private CJTileEntityMachineBase machine;

	void addPlayerInventory(InventoryPlayer inventory) {
		for(int row = 0; row < 3; row++) {
			for(int column = 0; column < 9; column++) {
				Slot slot = new Slot(
						inventory,
						column + (row * 9) + 9, /* Index */
						8 + (column * 18), /* X */
						84 + (row * 18) /* Y */);

				addSlot(slot);
			}
		}

		for(int i = 0; i < 9; i++) {
			addSlot(new Slot(inventory, i, 8 + i * 18, 142));
		}
	}

	public CJContainerMachineBase(
			InventoryPlayer inventoryPlayer, CJTileEntityMachineBase machine) {

		this.machine = machine;

		IInventory inventory = (IInventory) machine;
		EntityPlayer player = inventoryPlayer.player;

		// TODO: GuiBuilder API to passthrough from machine creation.
		addSlot(new CJSlotMachineBaseOutput(player, inventory, 0, 100, 50));
		addSlot(new Slot(inventory, 1, 50, 50));

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
		// arg2 might be the progress amount/delta but it's unclear.
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
