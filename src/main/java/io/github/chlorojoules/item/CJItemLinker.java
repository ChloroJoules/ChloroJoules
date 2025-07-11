package io.github.chlorojoules.item;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.CJGuiCoordinateDisplay;
import io.github.chlorojoules.machine.CJMachineTransferor;
import net.minecraft.common.block.icon.Icon;
import net.minecraft.common.block.icon.IconRegister;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.ChatColors;
import net.minecraft.common.util.i18n.StringTranslate;
import net.minecraft.common.world.World;

public class CJItemLinker extends Item {
	public static final int ITEM_EMPTY = 0;
	public static final int ITEM_FULL = 1;
	public static final int FLUID_EMPTY = 2;
	public static final int FLUID_FULL = 3;
	public static final int MULTI_ITEM_EMPTY = 4;
	public static final int MULTI_ITEM_FULL = 5;
	public static final int MULTI_FLUID_EMPTY = 6;
	public static final int MULTI_FLUID_FULL = 7;

	private final Icon[] linkerIcons;
	private final String[] linkerIconNames;

	// TODO: Merge full/empty and use dv instead of splitting for consistency.
	public CJItemLinker(String id, String[] icons) {
		super(id);

		linkerIconNames = icons;
		linkerIcons = new Icon[icons.length];
	}

	private void setItemTagIntArray(ItemStack stack, String key, int[] value) {
		CompoundTag tag;
		if(stack.hasTagCompound()) tag = stack.getTagCompound();
		else tag = new CompoundTag();

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

		if(id != CJMod.transferor.blockID) {
			String string = StringTranslate.getInstance().translateKey(
					"message.cj_not_transferor");

			CJMod.sendChat(ChatColors.RED + string);

			return false;
		}

		boolean isMulti = itemstack.itemDamage > FLUID_FULL;

		if(itemstack.itemDamage == ITEM_EMPTY ||
				itemstack.itemDamage == FLUID_EMPTY ||
				itemstack.itemDamage == MULTI_ITEM_EMPTY ||
				itemstack.itemDamage == MULTI_FLUID_EMPTY) {

			setItemTagIntArray(
					itemstack, "linked_position",
					new int[] { blockX, blockY, blockZ });

			String string = StringTranslate.getInstance().translateKeyFormat(
					"message.cj_linked_position", blockX, blockY, blockZ);

			CJMod.sendChat(					ChatColors.GREEN + string);

			itemstack.itemDamage++;

			return true;
		}

		CompoundTag tag = itemstack.getTagCompound();
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

		CJMod.sendChat(ChatColors.GREEN + string);

		CJMachineTransferor extractor =
				(CJMachineTransferor) extractorMachine.impl;

		CJMachineTransferor inserter =
				(CJMachineTransferor) inserterMachine.impl;

		if(extractor.linked != null && !isMulti) {
			extractor.breakLink(world);
		}

		// TODO: Maintain list of linked in multi-mode.
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
		else if(itemstack.itemDamage == FLUID_FULL) {
			extractorMachine.setWorldBlockMetadata(
					CJMachineTransferor.TRANSMIT_FLUIDS);

			inserterMachine.setWorldBlockMetadata(
					CJMachineTransferor.RECEIVE_FLUIDS);
		}
		else if(itemstack.itemDamage == MULTI_ITEM_FULL) {
			extractorMachine.setWorldBlockMetadata(
					CJMachineTransferor.MULTI_TRANSMIT_ITEMS);

			inserterMachine.setWorldBlockMetadata(
					CJMachineTransferor.RECEIVE_ITEMS);
		}
		else if(itemstack.itemDamage == MULTI_FLUID_FULL) {
			extractorMachine.setWorldBlockMetadata(
					CJMachineTransferor.MULTI_TRANSMIT_FLUIDS);

			inserterMachine.setWorldBlockMetadata(
					CJMachineTransferor.RECEIVE_FLUIDS);
		}

		if(!isMulti) itemstack.itemDamage--;

		return true;
	}

	@Override
	public ItemStack onItemRightClick(
			ItemStack itemstack, World world, EntityPlayer player) {

		if(!player.isSneaking()) {
			// TODO: Figure out mixin on player swing item for mode switching
			//		 So we can turn this back on.
			/*
			CompoundTag tag = itemstack.getTagCompound();
			int[] position = tag.getIntArray("linked_position");

			StringTranslate translate = StringTranslate.getInstance();
			String string = translate.translateKeyFormat(
					"message.cj_link_query",
					position[0], position[1], position[2]);

			CJMod.sendChat(					ChatColors.GREEN + string);

			return itemstack;
			*/

			String string;

			if(itemstack.itemDamage == ITEM_EMPTY ||
					itemstack.itemDamage == ITEM_FULL) {

				itemstack.setItemDamage(FLUID_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_fluid");
			}
			else if(itemstack.itemDamage == FLUID_EMPTY ||
					itemstack.itemDamage == FLUID_FULL) {

				itemstack.setItemDamage(MULTI_ITEM_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_multi_item");
			}
			else if(itemstack.itemDamage == MULTI_ITEM_EMPTY ||
					itemstack.itemDamage == MULTI_ITEM_FULL) {

				itemstack.setItemDamage(MULTI_FLUID_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_multi_fluid");
			}
			else {

				itemstack.setItemDamage(ITEM_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_item");
			}

			CJMod.sendChat(					ChatColors.BLUE + string);
		}

		if(itemstack.itemDamage != ITEM_FULL &&
				itemstack.itemDamage != FLUID_FULL &&
				itemstack.itemDamage != MULTI_ITEM_FULL &&
				itemstack.itemDamage != MULTI_FLUID_FULL) return itemstack;

		String string = StringTranslate.getInstance().translateKey(
				"message.cj_link_clear");

		CJMod.sendChat(				ChatColors.GREEN + string);

		itemstack.itemDamage--;

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
