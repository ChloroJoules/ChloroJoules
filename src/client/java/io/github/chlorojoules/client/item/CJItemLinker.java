package io.github.chlorojoules.client.item;

import com.fox2code.foxloader.network.NetworkPlayer;
import net.minecraft.fox2code.ChatColors;

import io.github.chlorojoules.client.CJClient;

import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;

public class CJItemLinker extends Item implements CJIItemSocket {
	public CJItemLinker(int id) {
		super(id - 256);
	}

	@Override
	public boolean onItemUse(
			ItemStack itemstack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side,
			float x, float y, float z) {

		if(!player.isSneaking()) return false;

		int id = world.getBlockId(blockX, blockY, blockZ);

		if(id != CJClient.whooper.getRegisteredBlockId()) {
			String string = StringTranslate.getInstance().translateKey(
					"message.cj_not_extractor");

			((NetworkPlayer) player).displayChatMessage(
					ChatColors.RED + string);

			return false;
		}

		NBTTagCompound tag;
		if(itemstack.hasTagCompound()) tag = itemstack.getTagCompound();
		else tag = new NBTTagCompound();

		tag.setIntArray(
				"linked_position", new int[] { blockX, blockY, blockZ });

		itemstack.setTagCompound(tag);

		String string = StringTranslate.getInstance().translateKeyFormat(
				"message.cj_linked_position", blockX, blockY, blockZ);

		((NetworkPlayer) player).displayChatMessage(ChatColors.GREEN + string);

		itemstack.itemID = CJClient.linkerFull.getRegisteredItemId();

		return true;
	}
}
