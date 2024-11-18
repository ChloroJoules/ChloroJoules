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

		// TODO: If held item is a bucket which contains a fluid accepted by
		//		 A `CJTank` of this machine -- fill that tank and empty the
		//		 Bucket. If the bucket is empty -- fill from an available
		//		 Output tank.
		// TODO: When we autogenerate fluid buckets we will probably need to
		//       Append Vanilla ones.
		ItemStack heldItem = player.inventory.getCurrentItem();

		if(heldItem == null) return false;
		if(heldItem.itemID != Item.bucketWater.itemID) return false;

		CJTankVolume tankVolume = machineEntity.getPrimaryInputTank();

		int waterID = Block.waterMoving.getBlockID();
		if(tankVolume.fluidID == waterID || tankVolume.fluidID == 0) {
			int filled = tankVolume.addFluid(waterID, CJTank.BUCKET, true);
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
	protected TileEntity getBlockEntity() {
		return new CJTileEntityMachineBase(machineBuilder);
	}
}

