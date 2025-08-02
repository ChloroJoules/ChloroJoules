package io.github.chlorojoules.item;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.CJGuiCoordinateDisplay;
import io.github.chlorojoules.machine.CJMachineTransferor;
import net.minecraft.client.Minecraft;
import net.minecraft.common.block.icon.Icon;
import net.minecraft.common.block.icon.IconRegister;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;
import net.minecraft.common.world.World;

import java.util.ArrayList;

public class CJItemLinker extends Item {
	public static final int MAX_LINK = 16;

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

	public CJItemLinker(String id, String[] icons) {
		super(id);

		linkerIconNames = icons;
		linkerIcons = new Icon[icons.length];
	}

	@Override
	public boolean onItemUse(
			ItemStack stack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side,
			float x, float y, float z) {

		if(!player.isSneaking()) return false;

		int id = world.getBlockId(blockX, blockY, blockZ);

		if(id != CJMod.transferor.blockID) {
			String string = StringTranslate.getInstance().translateKey(
					"message.cj_not_transferor");

			Minecraft.getInstance().sndManager.playSoundFX(
					"random.glass", 1.0F, 1.0F);

			CJMod.sendChat(string);

			return true;
		}

		boolean isMulti = stack.itemDamage > FLUID_FULL;

		if(stack.itemDamage == ITEM_EMPTY ||
				stack.itemDamage == FLUID_EMPTY ||
				stack.itemDamage == MULTI_ITEM_EMPTY ||
				stack.itemDamage == MULTI_FLUID_EMPTY) {

			CompoundTag tag = stack.getTagCompoundNonNull();
			tag.setIntArray(
					"linked_position", new int[] { blockX, blockY, blockZ });

			String string = StringTranslate.getInstance().translateKeyFormat(
					"message.cj_linked_position", blockX, blockY, blockZ);

			CJMod.sendChat(string);

			Minecraft.getInstance().sndManager.playSoundFX(
					"random.click", 1.0F, 1.0F);

			stack.itemDamage++;

			return true;
		}

		CompoundTag tag = stack.getTagCompoundNonNull();
		int[] position = tag.getIntArray("linked_position");

		CJTileEntityMachineBase inserterMachine =
				(CJTileEntityMachineBase) world.getBlockTileEntity(
						blockX, blockY, blockZ);

		CJTileEntityMachineBase extractorMachine =
				(CJTileEntityMachineBase) world.getBlockTileEntity(
						position[0], position[1], position[2]);

		if(extractorMachine == null) {
			stack.setItemDamage(stack.getItemDamage() - 1);
			return true;
		}

		if(inserterMachine == extractorMachine) {
			String string = StringTranslate.getInstance().translateKey(
					"message.cj_same_transferor");

			CJMod.sendChat(string);

			return true;
		}

		if(Math.abs(blockX - position[0]) > MAX_LINK ||
				Math.abs(blockY - position[1]) > MAX_LINK ||
				Math.abs(blockZ - position[2]) > MAX_LINK) {

			String string = StringTranslate.getInstance().translateKeyFormat(
					"message.cj_too_far", MAX_LINK);

			CJMod.sendChat(string);

			Minecraft.getInstance().sndManager.playSoundFX(
					"random.glass", 1.0F, 1.0F);

			return true;
		}

		String string = StringTranslate.getInstance().translateKeyFormat(
				"message.cj_linked_pair",
				blockX, blockY, blockZ,
				position[0], position[1], position[2]);

		CJMod.sendChat(string);

		if(!isMulti) CJMachineTransferor.breakLink(world, extractorMachine);
		CJMachineTransferor.breakLink(world, inserterMachine);

		ArrayList<int[]> extractorLinked =
				CJMachineTransferor.getLinked(extractorMachine);

		ArrayList<int[]> inserterLinked =
				CJMachineTransferor.getLinked(inserterMachine);

		if(isMulti || extractorLinked.isEmpty()) {
			extractorLinked.add(new int[] { blockX, blockY, blockZ });
		}
		else extractorLinked.set(0, new int[] { blockX, blockY, blockZ });

		if(inserterLinked.isEmpty()) inserterLinked.add(position);
		else inserterLinked.set(0, position);

		inserterMachine.coordinateDisplays.set(
				0, new CJGuiCoordinateDisplay(position));

		extractorMachine.coordinateDisplays.set(
				0, new CJGuiCoordinateDisplay(
						new int[] { blockX, blockY, blockZ }));

		if(stack.itemDamage == ITEM_FULL) {
			extractorMachine.setWorldBlockMetadata(
					CJMachineTransferor.TRANSMIT_ITEMS);

			inserterMachine.setWorldBlockMetadata(
					CJMachineTransferor.RECEIVE_ITEMS);
		}
		else if(stack.itemDamage == FLUID_FULL) {
			extractorMachine.setWorldBlockMetadata(
					CJMachineTransferor.TRANSMIT_FLUIDS);

			inserterMachine.setWorldBlockMetadata(
					CJMachineTransferor.RECEIVE_FLUIDS);
		}
		else if(stack.itemDamage == MULTI_ITEM_FULL) {
			extractorMachine.setWorldBlockMetadata(
					CJMachineTransferor.MULTI_TRANSMIT_ITEMS);

			inserterMachine.setWorldBlockMetadata(
					CJMachineTransferor.RECEIVE_ITEMS);
		}
		else if(stack.itemDamage == MULTI_FLUID_FULL) {
			extractorMachine.setWorldBlockMetadata(
					CJMachineTransferor.MULTI_TRANSMIT_FLUIDS);

			inserterMachine.setWorldBlockMetadata(
					CJMachineTransferor.RECEIVE_FLUIDS);
		}

		Minecraft.getInstance().sndManager.playSoundFX(
				"random.armor", 1.0F, 1.0F);

		if(!isMulti) stack.itemDamage--;

		return true;
	}

	@Override
	public ItemStack onItemRightClick(
			ItemStack stack, World world, EntityPlayer player) {

		if(!player.isSneaking()) {
			String string;

			if(stack.itemDamage == ITEM_EMPTY ||
					stack.itemDamage == ITEM_FULL) {

				stack.setItemDamage(FLUID_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_fluid");
			}
			else if(stack.itemDamage == FLUID_EMPTY ||
					stack.itemDamage == FLUID_FULL) {

				stack.setItemDamage(MULTI_ITEM_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_multi_item");
			}
			else if(stack.itemDamage == MULTI_ITEM_EMPTY ||
					stack.itemDamage == MULTI_ITEM_FULL) {

				stack.setItemDamage(MULTI_FLUID_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_multi_fluid");
			}
			else {
				stack.setItemDamage(ITEM_EMPTY);
				string = StringTranslate.getInstance().translateKey(
						"message.cj_link_switch_item");
			}

			Minecraft.getInstance().sndManager.playSoundFX(
					"random.ratchet", 1.0F, 1.0F);

			CJMod.sendChat(string);
		}

		if(stack.itemDamage != ITEM_FULL &&
				stack.itemDamage != FLUID_FULL &&
				stack.itemDamage != MULTI_ITEM_FULL &&
				stack.itemDamage != MULTI_FLUID_FULL) return stack;

		String string = StringTranslate.getInstance().translateKey(
				"message.cj_link_clear");

		Minecraft.getInstance().sndManager.playSoundFX(
				"random.break", 1.0F, 1.0F);

		CJMod.sendChat(string);

		stack.itemDamage--;

		return stack;
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
