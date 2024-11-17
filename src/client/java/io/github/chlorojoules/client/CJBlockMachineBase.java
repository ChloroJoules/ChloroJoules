package io.github.chlorojoules.client;

import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.block.BlockContainer;
import net.minecraft.src.game.block.Material;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.EnumTools;
import net.minecraft.src.game.level.World;

import static io.github.chlorojoules.client.TileEntityMachineBase.machineEntity;

class BlockMachineBase extends BlockContainer {
	public BlockMachineBase(int id) {
		super(id, Material.rock);

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

	@Override
	public boolean blockActivated(
			World world, int x, int y, int z, EntityPlayer player) {

		// TODO: Machine GUI.
		//player.displayGUIMachine(machineEntity(world, x, y, z));

		return true;
	}

	@Override
	public void onBlockRemoval(World world, int x, int y, int z) {
		TileEntityMachineBase tileEntity = machineEntity(world, x, y, z);

		tileEntity.onBreak(world, x, y, z);

		super.onBlockRemoval(world, x, y, z);
	}

	@Override
	protected TileEntity getBlockEntity() {
		return new TileEntityMachineBase();
	}
}

