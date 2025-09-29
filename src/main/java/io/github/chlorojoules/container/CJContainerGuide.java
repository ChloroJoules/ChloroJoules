package io.github.chlorojoules.container;

import net.minecraft.common.block.container.Container;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;

public class CJContainerGuide extends Container {
	@Override
	public ItemStack quickMove(int index) {
		return null;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityPlayer) {
		return false;
	}
}
