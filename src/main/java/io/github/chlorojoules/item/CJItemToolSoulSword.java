package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarityInfo;
import net.minecraft.common.entity.EntityLiving;
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

		// TODO: Adjust by Jewel value.
		weaponDamage = 8;
	}

	@Override
	public boolean hitEntity(
			ItemStack itemstack, EntityLiving target, EntityLiving attacker) {

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
