package io.github.chlorojoules.block;

import net.minecraft.common.block.Block;
import net.minecraft.common.block.data.Material;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.stats.Achievement;
import net.minecraft.common.world.World;

public class CJBlockAchievable extends Block {
	private Achievement achievement;

	public CJBlockAchievable(String name, Material material) {
		super(name, material);
	}

	public void setAchievement(Achievement achievement) {
		this.achievement = achievement;
	}

	@Override
	public void onCrafting(
			ItemStack itemstack, World world, EntityPlayer player) {

		if(this.achievement != null) {
			player.triggerAchievement(this.achievement);
		}

		super.onCrafting(itemstack, world, player);
	}
}
