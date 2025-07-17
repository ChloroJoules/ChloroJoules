package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.entity.EntityLiving;
import net.minecraft.common.entity.data.DamageType;
import net.minecraft.common.entity.data.DamageTypes;
import net.minecraft.common.entity.other.EntityItem;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.children.ItemTool;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.item.data.ToolMaterials;
import net.minecraft.common.world.World;

public class CJItemToolSoulSword extends ItemTool implements CJIItemSocket {
	public CJItemToolSoulSword(String id) {
		super(id, 0, ToolMaterials.OBSIDIAN, EnumTools.SWORD);

		setMaxDamage(0);
		addDescription(new CJItemDescriptionSocket());
		setHasSubtypes(true);

		weaponDamage = 1;
	}

	private static int getStackDamage(ItemStack stack) {
		return switch(CJRarityInfo.getDamageRarity(stack.getItemDamage())) {
			case PRIMAL -> 3;
			case MANUFACTURED -> 5;
			case REFINED -> 7;
			case AWAKENED -> 9;
			default -> 0;
		};
	}

	@Override
	public boolean hitEntity(
			ItemStack itemstack, EntityLiving target, EntityLiving attacker) {

		target.beenAttacked = false;
		target.attackEntityFrom(
				attacker, getStackDamage(itemstack), DamageTypes.PLAYER);

		if(target.health <= 0) {
			World world = target.worldObj;

			int count = (int) ((world.rand.nextFloat() * 0.7) *
							(CJRarityInfo.MAX_DAMAGE - itemstack.itemDamage));

			if(count <= 0) return true;

			ItemStack soulStack = new ItemStack(
					CJMod.soulEssence.itemID, count);

			EntityItem item = new EntityItem(
					world, target.posX, target.posY, target.posZ,
					soulStack);

			world.entityJoinedWorld(item);
		}

		return true;
	}

	@Override
	public boolean onBlockDestroyed(
			ItemStack itemstack, int id, int x, int y, int z,
			EntityLiving entity) {

		return true;
	}
}
