package io.github.chlorojoules.client;

import net.minecraft.client.Minecraft;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.block.BlockContainer;
import net.minecraft.src.game.block.Material;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.item.EnumTools;
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

	@Override
	public boolean blockActivated(
			World world, int x, int y, int z, EntityPlayer player) {

		// TODO: If held item is a bucket which contains a fluid accepted by
		//		 A `CJTank` of this machine -- fill that tank and empty the
		//		 Bucket. If the bucket is empty -- fill from an available
		//		 Output tank.
		CJTileEntityMachineBase machine = machineEntity(world, x, y, z);
		CJGuiMachineBase gui = new CJGuiMachineBase(
				player.inventory, machine, machineBuilder);

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

