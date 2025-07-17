package io.github.chlorojoules.item;

import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.children.ItemBucket;
import net.minecraft.common.world.World;

public class CJItemBucket extends ItemBucket {
	public CJItemBucket(String id, int fluid) {
		super(id, fluid);
	}

	@Override
	public ItemStack onItemRightClick(
			ItemStack itemstack, World world, EntityPlayer player) {

		return itemstack;
	}
}
