package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import net.minecraft.common.effect.Effects;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.item.children.ItemBowlSoup;
import net.minecraft.common.world.World;

public class CJItemPasteBowl extends ItemBowlSoup {
	public CJItemPasteBowl(String id) {
		super(id, 1);
	}

	@Override
	public ItemStack onEaten(
			ItemStack stack, World world, EntityPlayer player) {

		player.inventory.addItemStackToInventory(new ItemStack(CJMod.paste));
		player.applyEffect(Effects.FRAGILITY, 2, (byte) 1);
		return super.onEaten(stack, world, player);
	}
}
