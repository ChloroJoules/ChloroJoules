package io.github.chlorojoules.item;

import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.stats.Achievement;
import net.minecraft.common.world.World;

public class CJItemAchievable extends Item {
	Achievement achievement;

	public CJItemAchievable(String id) {
		super(id);
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
