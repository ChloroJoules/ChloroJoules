package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.util.ChatColors;
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
			ItemStack stack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side,
			float x, float y, float z) {

		int rarityDamage = CJRarityInfo.getRarityDamage(CJRarity.MANUFACTURED);
		if(stack.itemDamage > rarityDamage) {
			String string = stack.itemDamage == CJRarityInfo.MAX_DAMAGE ?
					"message.cj_no_jewel" :
					"message.cj_poor_jewel";

			player.addChatMessage(ChatColors.RED + string);

			return false;
		}

		int id = world.getBlockId(blockX, blockY, blockZ);

		if(id != Blocks.MOB_SPAWNER.blockID) return false;

		world.setBlock(blockX, blockY, blockZ, Blocks.AIR.blockID);

		CJMod.spawnItem(
				world, blockX, blockY, blockZ,
				new ItemStack(CJMod.soulCore.itemID, 1), false);

		Minecraft.getInstance().sndManager.playSoundFX(
				"random.levelup", 1.0F, 1.0F);

		stack.setItemDamage(CJRarityInfo.MAX_DAMAGE);

		return true;
	}
}
