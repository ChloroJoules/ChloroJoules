package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

public class CJItemSoulGem extends Item {
	public CJItemSoulGem(String name) {
		super(name);
	}

	@Override
	public boolean onItemUse(
			ItemStack stack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side,
			float x, float y, float z) {

		int id = world.getBlockId(blockX, blockY, blockZ);

		if(id != CJMod.compactedJewelDust.blockID) return false;

		world.setBlockWithNotify(
				blockX, blockY, blockZ, CJMod.fluidAwareness.blockID);

		player.inventory.decrStackSize(player.inventory.currentItem, 1);

		return true;
	}
}
