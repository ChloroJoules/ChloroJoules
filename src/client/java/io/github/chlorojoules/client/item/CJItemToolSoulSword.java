package io.github.chlorojoules.client.item;

import io.github.chlorojoules.client.CJClient;
import io.github.chlorojoules.client.CJRarityInfo;
import net.minecraft.src.game.entity.EntityLiving;
import net.minecraft.src.game.entity.other.EntityItem;
import net.minecraft.src.game.item.EnumToolMaterial;
import net.minecraft.src.game.item.EnumTools;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.item.ItemTool;
import net.minecraft.src.game.level.World;

public class CJItemToolSoulSword extends ItemTool implements CJIItemSocket {
	public CJItemToolSoulSword(int id) {
		super(id - 256, 0, EnumToolMaterial.EMERALD, EnumTools.SWORD);

		setMaxDamage(CJRarityInfo.MAX_DAMAGE);
		addDescription(new CJItemDescriptionSocket());

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
					CJClient.soulEssence.getRegisteredItemId(), count);

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
