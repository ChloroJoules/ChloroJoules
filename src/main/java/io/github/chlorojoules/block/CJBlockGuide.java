package io.github.chlorojoules.block;

import io.github.chlorojoules.block.tileentity.CJTileEntityGuide;
import io.github.chlorojoules.container.CJContainerGuide;
import io.github.chlorojoules.gui.CJGuiGuide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.EntityPlayerSP;
import net.minecraft.common.block.children.BlockContainer;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.networking.Packet;
import net.minecraft.common.networking.Packet100OpenWindow;
import net.minecraft.common.world.World;
import net.minecraft.server.entity.player.EntityPlayerMP;

public class CJBlockGuide extends BlockContainer {
	public CJBlockGuide(String id) {
		super(id, Materials.ROCK);

		super.setBlockName(id);
	}

	public static void displayGUIMP(EntityPlayerMP player) {
		player.getNextWindowId();

		Packet packet = new Packet100OpenWindow(
				player.currentWindowId, 0, "cj_guide", 0);

		player.playerNetServerHandler.sendPacket(packet);

		player.currentContainer = new CJContainerGuide();
		player.currentContainer.windowId = player.currentWindowId;
		player.currentContainer.onCraftGuiOpened(player);
		player.mcServer.getPlayerInteractionHandler().blockActivation(
				"cj_gui", player);
	}

	public static void displayGUISP() {
		Minecraft.getInstance().displayGuiScreen(new CJGuiGuide());
	}

	public static void displayGUI(EntityPlayer player) {
		if(!player.worldObj.isRemote) {
			if(player instanceof EntityPlayerMP mp) {
				displayGUIMP(mp);
			}
			else if(player instanceof EntityPlayerSP) {
				displayGUISP();
			}
		}
	}

	public boolean blockActivated(
			World world, int x, int y, int z, EntityPlayer player) {

		displayGUI(player);

		return true;
	}

	@Override
	public TileEntity getBlockEntity() {
		return new CJTileEntityGuide();
	}
}
