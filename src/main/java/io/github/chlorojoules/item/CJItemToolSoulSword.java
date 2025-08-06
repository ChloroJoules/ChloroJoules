package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import net.minecraft.common.entity.EntityLiving;
import net.minecraft.common.entity.data.DamageTypes;
import net.minecraft.common.entity.other.EntityItem;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.children.ItemTool;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.item.data.ToolMaterials;
import net.minecraft.common.item.description.ItemDesc;
import net.minecraft.common.item.description.ItemDescTool;
import net.minecraft.common.world.World;

import java.util.List;

public class CJItemToolSoulSword extends ItemTool implements CJIItemSocket {
	public CJItemToolSoulSword(String id) {
		super(id, 0, ToolMaterials.OBSIDIAN, EnumTools.SWORD);

		setMaxDamage(0);
		addDescription(new CJItemDescriptionSocket());
		addDescription(new CJItemDescriptionSword());
		setHasSubtypes(true);

		List<ItemDesc> description = getItemDescription();
		int toolDesc;
		for(toolDesc = 0; toolDesc < description.size(); ++toolDesc) {
			ItemDesc desc = description.get(toolDesc);

			if(desc instanceof ItemDescTool) break;
		}
		description.remove(toolDesc);

		weaponDamage = 1;
	}

	public static int getRarityDamage(CJRarity rarity) {
		return switch(rarity) {
			case PRIMAL -> 3;
			case MANUFACTURED -> 5;
			case REFINED -> 7;
			case AWAKENED -> 9;
			default -> 0;
		};
	}

	private static int getStackDamage(ItemStack stack) {
		return getRarityDamage(
				CJRarityInfo.getDamageRarity(stack.getItemDamage()));
	}

	@Override
	public boolean hitEntity(
			ItemStack stack, EntityLiving target, EntityLiving attacker) {

		target.beenAttacked = false;
		target.attackEntityFrom(
				attacker, getStackDamage(stack), DamageTypes.PLAYER);

		if(target.health <= 0) {
			World world = target.worldObj;

			int count = (int) ((world.rand.nextFloat() * 0.7) *
							(CJRarityInfo.MAX_DAMAGE - stack.itemDamage));

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
			ItemStack stack, int id, int x, int y, int z,
			EntityLiving entity) {

		return true;
	}
}
