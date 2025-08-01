package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.util.ChatColors;
import net.minecraft.common.util.i18n.StringTranslate;
import net.minecraft.common.entity.other.EntityItem;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

// TODO: Add damage so texture can appear greyed when no jewel is inserted.
public class CJItemSoulExtractor extends Item implements CJIItemSocket {
	public CJItemSoulExtractor(String id) {
		super(id);

		setMaxDamage(0);
		addDescription(new CJItemDescriptionSocket());
		setHasSubtypes(true);
	}

	@Override
	public boolean onItemUse(
			ItemStack itemstack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side,
			float x, float y, float z) {

		int rarityDamage = CJRarityInfo.getRarityDamage(CJRarity.MANUFACTURED);
		if(itemstack.itemDamage > rarityDamage) {
			String string = StringTranslate.getInstance().translateKey(
					"message.cj_poor_jewel");

			CJMod.sendChat(ChatColors.RED + string);

			return false;
		}

		int id = world.getBlockId(blockX, blockY, blockZ);

		if(id != Blocks.MOB_SPAWNER.blockID) return false;

		world.setBlock(blockX, blockY, blockZ, Blocks.AIR.blockID);

		ItemStack soulStack =
				new ItemStack(CJMod.soulCore.itemID, 1);

		EntityItem item =
				new EntityItem(world, blockX, blockY, blockZ, soulStack);

		world.entityJoinedWorld(item);

		Minecraft.getInstance().sndManager.playSoundFX(
				"random.levelup", 1.0F, 1.0F);

		return true;
	}
}
