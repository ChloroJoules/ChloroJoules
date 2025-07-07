package io.github.chlorojoules.client.item;

import com.fox2code.foxloader.network.NetworkPlayer;
import io.github.chlorojoules.client.CJClient;
import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.client.gui.CJGuiCoordinate;
import io.github.chlorojoules.client.machine.*;
import net.minecraft.fox2code.ChatColors;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;
import org.lwjgl.input.Mouse;

public class CJItemLinkerFull extends Item implements CJIItemSocket {
	public CJItemLinkerFull(int id) {
		super(id - 256);
	}

	@Override
	public ItemStack onItemRightClick(
			ItemStack itemstack, World world, EntityPlayer player) {

		if(!player.isSneaking()) {
			NBTTagCompound tag = itemstack.getTagCompound();
			int[] position = tag.getIntArray("linked_position");

			StringTranslate translate = StringTranslate.getInstance();
			String string = translate.translateKeyFormat(
					"message.cj_link_query",
					position[0], position[1], position[2]);

			((NetworkPlayer) player).displayChatMessage(
					ChatColors.GREEN + string);

			return itemstack;
		}

		String string = StringTranslate.getInstance().translateKey(
				"message.cj_link_clear");

		((NetworkPlayer) player).displayChatMessage(
				ChatColors.GREEN + string);

		itemstack.itemID = CJClient.linker.getRegisteredItemId();

		return itemstack;
	}

	@Override
	public boolean onItemUse(
			ItemStack itemstack, EntityPlayer player, World world,
			int blockX, int blockY, int blockZ, int side,
			float x, float y, float z) {

		if(!player.isSneaking()) return false;

		int id = world.getBlockId(blockX, blockY, blockZ);

		if(id != CJClient.swooper.getRegisteredBlockId()) {
			String string = StringTranslate.getInstance().translateKey(
					"message.cj_not_swooper");

			((NetworkPlayer) player).displayChatMessage(
					ChatColors.RED + string);

			return true;
		}

		CJTileEntityMachineBase machine =
				(CJTileEntityMachineBase) world.getBlockTileEntity(
						blockX, blockY, blockZ);

		CJMachineBuilder builder = machine.machineBuilder;
		CJGuiCoordinate coordinate = builder.linkCoordinates.get(0);

		NBTTagCompound tag = itemstack.getTagCompound();
		int[] position = tag.getIntArray("linked_position");

		CJTileEntityMachineBase targetMachine =
				(CJTileEntityMachineBase) world.getBlockTileEntity(
						position[0], position[1], position[2]);

		CJMachineBuilder targetBuilder = targetMachine.machineBuilder;
		CJGuiCoordinate targetCoordinate =
				targetBuilder.linkCoordinates.get(0);

		String string = StringTranslate.getInstance().translateKeyFormat(
				"message.cj_linked_pair",
				blockX, blockY, blockZ,
				position[0], position[1], position[2]);

		((NetworkPlayer) player).displayChatMessage(ChatColors.GREEN + string);

		CJMachineWhooper whooper = (CJMachineWhooper) targetMachine.impl;
		CJMachineSwooper swooper = (CJMachineSwooper) machine.impl;

		if(whooper.linked != null) {
			CJMachineSwooper linkedSwooper =
					(CJMachineSwooper) whooper.linked.impl;

			// TODO: More consolidated make/break link interface.
			linkedSwooper.linked = null;
		}

		whooper.linked = machine;
		swooper.linked = targetMachine;

		coordinate.label = "message.cj_link_coordinate";
		machine.coordinateDisplays.set(0, position);
		builder.linkCoordinates.set(0, coordinate);

		targetCoordinate.label = "message.cj_link_coordinate";
		targetMachine.coordinateDisplays.set(
				0, new int[] { blockX, blockY, blockZ });

		targetBuilder.linkCoordinates.set(0, targetCoordinate);

		itemstack.itemID = CJClient.linker.getRegisteredItemId();

		return true;
	}
}
