package io.github.chlorojoules.client.item;

import com.fox2code.foxloader.network.NetworkPlayer;
import io.github.chlorojoules.client.CJClient;
import io.github.chlorojoules.client.CJRarity;
import io.github.chlorojoules.client.CJRarityInfo;
import net.minecraft.fox2code.ChatColors;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.entity.other.EntityItem;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.item.description.ItemDescTool;
import net.minecraft.src.game.level.World;

public class CJItemSoulExtractor extends Item implements CJIItemSocket {
	public CJItemSoulExtractor(int id) {
		super(id - 256);

		setMaxDamage(0);
		addDescription(new CJItemDescriptionSocket());
		setHasSubtypes(true);
	}

	@Override
	public boolean onItemUse(
			ItemStack itemstack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side,
			float x, float y, float z) {

		// TODO: Sound effects.

		int rarityDamage = CJRarityInfo.getRarityDamage(CJRarity.REFINED);
		if(itemstack.itemDamage > rarityDamage) {
			String string = StringTranslate.getInstance().translateKey(
					"message.cj_poor_jewel");

			((NetworkPlayer) player).displayChatMessage(
					ChatColors.RED + string);

			return false;
		}

		int id = world.getBlockId(blockX, blockY, blockZ);

		if(id != Block.mobSpawner.getBlockID()) return false;

		world.setBlock(blockX, blockY, blockZ, Block.air.getBlockID());

		ItemStack soulStack =
				new ItemStack(CJClient.soulCore.getRegisteredItemId(), 1);

		EntityItem item =
				new EntityItem(world, blockX, blockY, blockZ, soulStack);

		world.entityJoinedWorld(item);

		return true;
	}
}
