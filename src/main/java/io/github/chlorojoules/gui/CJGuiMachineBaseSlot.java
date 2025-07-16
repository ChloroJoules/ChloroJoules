package io.github.chlorojoules.gui;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.machine.CJMachineSlotInfo;
import net.minecraft.common.block.container.Slot;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;

public class CJGuiMachineBaseSlot extends Slot {
	private final EntityPlayer entityPlayer;

	public CJMachineSlotInfo info;

	public CJGuiMachineBaseSlot(
			EntityPlayer entityPlayer, IInventory inventory,
			int index, int x, int y, CJMachineSlotInfo info) {

		super(inventory, index, x, y);

		this.entityPlayer = entityPlayer;
		this.info = info;
	}

	@Override
	public boolean isItemValid(ItemStack item) {
		if(info == null) return true;

		return !info.output &&
				info.isAllowedItem(item);
	}

	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack item) {
		super.onPickupFromSlot(player, item);
	}
}
