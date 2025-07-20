package io.github.chlorojoules.item;

import net.minecraft.common.effect.Effects;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.children.ItemBowlSoup;
import net.minecraft.common.world.World;

public class CJItemConvertBowl extends ItemBowlSoup {
	ItemStack convert;

	public CJItemConvertBowl(String id, ItemStack convert) {
		super(id, 1);

		this.convert = convert;
	}

	@Override
	public ItemStack onEaten(
			ItemStack stack, World world, EntityPlayer player) {

		player.inventory.addItemStackToInventory(convert.copy());
		player.applyEffect(Effects.FRAGILITY, 40, (byte) 1);
		return super.onEaten(stack, world, player);
	}
}
