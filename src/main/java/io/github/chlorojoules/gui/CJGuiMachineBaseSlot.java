package io.github.chlorojoules.gui;

import net.minecraft.common.block.container.Slot;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;

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
	public void onPickupFromSlot(EntityPlayer player, ItemStack item) {
		// TODO: This currently fires for inventory pickup aswell.
		//item.onCrafting(entityPlayer.worldObj, entityPlayer);
		super.onPickupFromSlot(player, item);
	}

	public CJGuiMachineBaseSlot setOutput(boolean value) {
		output = value;
		return this;
	}

	public boolean isOutput() {
		return output;
	}
}
