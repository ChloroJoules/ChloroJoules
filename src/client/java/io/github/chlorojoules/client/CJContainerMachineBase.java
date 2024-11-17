package io.github.chlorojoules.client;

import net.minecraft.src.client.gui.Container;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.entity.player.InventoryPlayer;
import net.minecraft.src.game.item.ItemStack;

import java.util.ArrayList;

// TODO: Mixin on `ItemBucket.onItemRightClick' to auto-register non-Vanilla
//		 Fluids to bucket items.

public class CJContainerMachineBase extends Container {
	private final CJTileEntityMachineBase machine;

	public ArrayList<CJTank> tanks = new ArrayList<CJTank>();

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

		// TODO: Add helper for adding fuel tanks.
		tanks.add(new CJTank(25, 35));

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
