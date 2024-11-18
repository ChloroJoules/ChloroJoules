package io.github.chlorojoules.client.gui;

import net.minecraft.src.client.gui.Slot;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.ItemStack;

public class CJGuiMachineBaseSlot extends Slot {
	private final EntityPlayer entityPlayer;

	private boolean output = false;

	public CJGuiMachineBaseSlot(
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
		// TODO: This currently fires for inventory pickup aswell.
		//item.onCrafting(entityPlayer.worldObj, entityPlayer);
		super.onPickupFromSlot(item);
	}

	public CJGuiMachineBaseSlot setOutput(boolean value) {
		output = value;
		return this;
	}

	public boolean isOutput() {
		return output;
	}
}
