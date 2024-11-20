package io.github.chlorojoules.client;

import io.github.chlorojoules.client.gui.CJGuiMachineBase;
import io.github.chlorojoules.client.machine.CJMachineBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.block.BlockContainer;
import net.minecraft.src.game.block.Material;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.entity.player.InventoryPlayer;
import net.minecraft.src.game.item.EnumTools;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemBucket;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.level.World;

import static io.github.chlorojoules.client.CJTileEntityMachineBase.machineEntity;

public class CJBlockMachineBase extends BlockContainer {
	private final CJMachineBuilder machineBuilder;

	public CJBlockMachineBase(int id, CJMachineBuilder builder) {
		super(id, Material.rock);

		machineBuilder = builder;

		// Machines have the same basic block properties by default.
		super.setHardness(1.5F);
		super.setResistance(10.0F);
		super.setSound(Block.soundStone);
		super.setEffectiveTool(EnumTools.PICKAXE);
	}

	// TODO: Override the following for rotation.
	// TODO: `doWrenchRotation' should also allow to wrench machines to
	// 		 Pick them up more easily.
	/*
	doWrenchRotation
	onBlockPlacedBy
	allocateTextures
	 */

	private boolean tryFillBucket(
			World world, int x, int y, int z, EntityPlayer player) {

		CJTileEntityMachineBase machineEntity = machineEntity(world, x, y, z);

		ItemStack heldItem = player.inventory.getCurrentItem();
		if(heldItem == null) return false;

		int heldID = heldItem.itemID;

		// If the bucket is empty -- fill from an available output tank.
		if(heldID == Item.bucketEmpty.itemID) {
			int index = machineBuilder.getPrimaryOutputTankIndex();

			if(index == -1) return false;

			CJTankVolume volume = machineEntity.tanks.get(index);

			int bucketID = -1;

			for(int i = 0; i < CJClient.bucketFluids.size(); i++) {
				int fluid = CJClient.bucketFluids.get(i);

				if(fluid != volume.fluidID) continue;

				bucketID = CJClient.buckets.get(i).getRegisteredItemId();
			}

			if(bucketID == -1) return false;

			if(volume.removeFluid(0, CJTank.BUCKET, true) == CJTank.BUCKET) {
				InventoryPlayer inventory = player.inventory;
				ItemStack stack =
						inventory.mainInventory[inventory.currentItem];

				stack.itemID = bucketID;
				return true;
			}

			return false;
		}

		int fluidID = -1;
		for(int i = 0; i < CJClient.buckets.size(); i++) {
			ItemBucket bucket = CJClient.buckets.get(i);

			if(bucket.itemID != heldID) continue;

			fluidID = CJClient.bucketFluids.get(i);
		}

		if(fluidID == -1) return false;

		// TODO: Make this return different tanks depending on attempted fluid
		//       Insertion.
		int tankIndex = machineBuilder.getPrimaryInputTankIndex();
		CJTankVolume tankVolume = machineEntity.tanks.get(tankIndex);

		if(tankVolume.fluidID == fluidID || tankVolume.fluidID == 0) {
			int filled = tankVolume.addFluid(fluidID, CJTank.BUCKET, true);
			if(filled == CJTank.BUCKET) {
				InventoryPlayer inventory = player.inventory;
				ItemStack stack =
						inventory.mainInventory[inventory.currentItem];

				stack.itemID = Item.bucketEmpty.itemID;
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean blockActivated(
			World world, int x, int y, int z, EntityPlayer player) {

		CJTileEntityMachineBase machineEntity = machineEntity(world, x, y, z);

		if(tryFillBucket(world, x, y, z, player)) {
			// TODO: Sound effect.
			return true;
		}

		CJGuiMachineBase gui = new CJGuiMachineBase(
				player.inventory, machineEntity, machineBuilder);

		Minecraft.theMinecraft.displayGuiScreen(gui);

		return true;
	}

	@Override
	public void onBlockRemoval(World world, int x, int y, int z) {
		CJTileEntityMachineBase machine = machineEntity(world, x, y, z);

		machine.onBreak(world, x, y, z);

		super.onBlockRemoval(world, x, y, z);
	}

	@Override
	public void onNeighborBlockChange(
			World world, int x, int y, int z, int ext) {

		machineBuilder.machineImpl.onNeighbourChange(world, x, y, z);

		super.onNeighborBlockChange(world, x, y, z, ext);
	}

	@Override
	protected TileEntity getBlockEntity() {
		return new CJTileEntityMachineBase(machineBuilder);
	}
}

