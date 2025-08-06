package io.github.chlorojoules.item;

import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.children.ItemGoldenBucket;
import net.minecraft.common.world.World;

public class CJItemGoldenBucket extends ItemGoldenBucket {
	public CJItemGoldenBucket(String id, int fluid) {
		super(id, fluid);
	}

	@Override
	public ItemStack onItemRightClick(
			ItemStack stack, World world, EntityPlayer player) {

		return stack;
	}
}
