package io.github.chlorojoules.client.item;

import com.fox2code.foxloader.network.NetworkPlayer;
import io.github.chlorojoules.client.CJClient;
import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.client.gui.CJGuiCoordinate;
import io.github.chlorojoules.client.gui.CJGuiCoordinateDisplay;
import io.github.chlorojoules.client.machine.*;
import net.minecraft.fox2code.ChatColors;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.client.renderer.block.icon.Icon;
import net.minecraft.src.client.renderer.block.icon.IconRegister;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;
import org.lwjgl.input.Mouse;

public class CJItemLinker extends Item {
	public static final int ITEM_EMPTY = 0;
	public static final int ITEM_FULL = 1;
	public static final int FLUID_EMPTY = 2;
	public static final int FLUID_FULL = 3;

	private Icon[] linkerIcons;
	private String[] linkerIconNames;

	// TODO: Merge full/empty and use dv instead of splitting for consistency.
	public CJItemLinker(int id, String[] icons) {
		super(id - 256);

		linkerIconNames = icons;
		linkerIcons = new Icon[icons.length];
	}

	private void setItemTagIntArray(ItemStack stack, String key, int[] value) {
		NBTTagCompound tag;
		if(stack.hasTagCompound()) tag = stack.getTagCompound();
		else tag = new NBTTagCompound();

		tag.setIntArray(key, value);
		stack.setTagCompound(tag);
	}

	@Override
	public boolean onItemUse(
			ItemStack itemstack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side,
			float x, float y, float z) {

		if(!player.isSneaking()) return false;

		int id = world.getBlockId(blockX, blockY, blockZ);

		if(id != CJClient.transferor.getRegisteredBlockId()) {
			String string = StringTranslate.getInstance().translateKey(
					"message.cj_not_transferor");

			((NetworkPlayer) player).displayChatMessage(
					ChatColors.RED + string);

			return false;
		}

		if(itemstack.itemDamage == ITEM_EMPTY ||
				itemstack.itemDamage == FLUID_EMPTY) {

			setItemTagIntArray(
					itemstack, "linked_position",
					new int[] { blockX, blockY, blockZ });

			String string = StringTranslate.getInstance().translateKeyFormat(
					"message.cj_linked_position", blockX, blockY, blockZ);

			((NetworkPlayer) player).displayChatMessage(
					ChatColors.GREEN + string);

			itemstack.itemDamage++;

			return true;
		}

		NBTTagCompound tag = itemstack.getTagCompound();
		int[] position = tag.getIntArray("linked_position");

		CJTileEntityMachineBase inserterMachine =
				(CJTileEntityMachineBase) world.getBlockTileEntity(
						blockX, blockY, blockZ);

		CJTileEntityMachineBase extractorMachine =
				(CJTileEntityMachineBase) world.getBlockTileEntity(
						position[0], position[1], position[2]);

		String string = StringTranslate.getInstance().translateKeyFormat(
				"message.cj_linked_pair",
				blockX, blockY, blockZ,
				position[0], position[1], position[2]);

		((NetworkPlayer) player).displayChatMessage(ChatColors.GREEN + string);

		CJMachineTransferor extractor =
				(CJMachineTransferor) extractorMachine.impl;

		CJMachineTransferor inserter =
				(CJMachineTransferor) inserterMachine.impl;

		if(extractor.linked != null) extractor.breakLink(world);

		extractor.linked = new int[] { blockX, blockY, blockZ };
		inserter.linked = position;

		inserterMachine.coordinateDisplays.set(
				0, new CJGuiCoordinateDisplay(position));

		extractorMachine.coordinateDisplays.set(
				0, new CJGuiCoordinateDisplay(
						new int[] { blockX, blockY, blockZ }));

		if(itemstack.itemDamage == ITEM_FULL) {
			extractorMachine.setWorldBlockMetadata(
					CJMachineTransferor.TRANSMIT_ITEMS);

			inserterMachine.setWorldBlockMetadata(
					CJMachineTransferor.RECEIVE_ITEMS);
		}
		else {
			extractorMachine.setWorldBlockMetadata(
					CJMachineTransferor.TRANSMIT_FLUIDS);

			inserterMachine.setWorldBlockMetadata(
					CJMachineTransferor.RECEIVE_FLUIDS);
		}

		itemstack.itemDamage--;

		return true;
	}

	@Override
	public ItemStack onItemRightClick(
			ItemStack itemstack, World world, EntityPlayer player) {

		if(!player.isSneaking()) {
			// TODO: Figure out mixin on player swing item for mode switching
			//		 So we can turn this back on.
			/*
			NBTTagCompound tag = itemstack.getTagCompound();
			int[] position = tag.getIntArray("linked_position");

			StringTranslate translate = StringTranslate.getInstance();
			String string = translate.translateKeyFormat(
					"message.cj_link_query",
					position[0], position[1], position[2]);

			((NetworkPlayer) player).displayChatMessage(
					ChatColors.GREEN + string);

			return itemstack;
			*/

			String string;

			if(itemstack.itemDamage == ITEM_EMPTY ||
					itemstack.itemDamage == ITEM_FULL) {

				itemstack.setItemDamage(FLUID_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_fluid");
			}
			else {
				itemstack.setItemDamage(ITEM_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_item");
			}

			((NetworkPlayer) player).displayChatMessage(
					ChatColors.BLUE + string);
		}

		if(itemstack.itemDamage != ITEM_FULL) return itemstack;

		String string = StringTranslate.getInstance().translateKey(
				"message.cj_link_clear");

		((NetworkPlayer) player).displayChatMessage(
				ChatColors.GREEN + string);

		if(itemstack.itemDamage == ITEM_FULL) {
			itemstack.setItemDamage(ITEM_EMPTY);
		}
		else itemstack.setItemDamage(FLUID_EMPTY);

		return itemstack;
	}

	@Override
	public void registerIcons(IconRegister register) {
		for(int i = 0; i < linkerIconNames.length; ++i) {
			linkerIcons[i] = register.registerIcon(linkerIconNames[i]);
		}
	}

	@Override
	public Icon getIconFromDamage(int metadata) {
		return linkerIcons[metadata];
	}
}
