package io.github.chlorojoules.machine;

import io.github.chlorojoules.*;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.children.*;
import net.minecraft.common.block.fluid.Fluid;
import net.minecraft.common.block.fluid.Fluids;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;
import net.minecraft.common.world.World;

@SuppressWarnings("unused")
public class CJMachinePump implements CJIMachine {
	private Block[] adjacentBlocks;

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		StringTranslate translate = StringTranslate.getInstance();

		CJMachineBuilder machineBuilder = machineEntity.getBuilder();

		ItemStack jewelStack =
				machineBuilder.getNamedStack(machineEntity, "jewel");

		CJTankVolume fuel =
				machineBuilder.getNamedTankVolume(machineEntity, "fuel");

		CJTankVolume output =
				machineBuilder.getNamedTankVolume(machineEntity, "output");

		machineEntity.errorMessage = null;
		machineEntity.isWarning = false;
		machineEntity.isPassive = false;
		machineEntity.operationLength = 100;

		// Unfortunately nether portal ignition doesn't notify adjacent so we
		// Just need to check every frame.
		onNeighbourChange(
				machineEntity.worldObj,
				machineEntity.xCoord,
				machineEntity.yCoord,
				machineEntity.zCoord);

		if(jewelStack == null) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_jewel");

			return;
		}

		CJRarity rarity = CJRarityInfo.getJewelRarity(jewelStack.getItemID());

		int outputAmount = 0;
		int outputFluid = 0;
		int cost = 0;
		int outputBlockIndex = 0;
		for(int i = 0; i < adjacentBlocks.length; ++i) {
			Block block = adjacentBlocks[i];

			if(block instanceof BlockFluid) {
				int[] pos = getAdjacentFromIndex(machineEntity, i);
				Fluid fluid = Fluids.getFluidFromBlock(block);
				int damage = machineEntity.worldObj.getBlockMetadata(
						pos[0], pos[1], pos[2]);

				if(damage != 0) continue;

				outputAmount = CJTank.BUCKET;
				machineEntity.operationLength = 250;
				cost = 50 / CJRarityInfo.getRarityPowerScale(rarity);
				outputFluid = fluid.getMoving().blockID;

				outputBlockIndex = i;
				break;
			}
			else if(block instanceof BlockPortal) {
				outputAmount = 500;
				machineEntity.operationLength = 1000;
				cost = 750 / CJRarityInfo.getRarityPowerScale(rarity);
				outputFluid = CJMod.fluidDimension.blockID;
				outputBlockIndex = i;
				break;
			}
		}

		if(outputAmount == 0 ||
				(output.current != 0 && outputFluid != output.fluidID)) {

			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_fluid");

			return;
		}

		if(fuel.current < cost) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_power");

			return;
		}

		if(output.max - output.current < outputAmount) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_full");
		}

		machineEntity.operationLength /=
				CJRarityInfo.getRarityTimeScale(rarity);

		machineEntity.operationTicks++;

		if(machineEntity.operationTicks >= machineEntity.operationLength) {
			int[] pos = getAdjacentFromIndex(machineEntity, outputBlockIndex);

			machineEntity.worldObj.setBlockWithNotify(
					pos[0], pos[1], pos[2], 0);

			output.addFluid(outputFluid, outputAmount, false);
			fuel.removeFluid(CJMod.fuelFluid, cost, false);

			jewelStack.damageItem(1, null, true);
			machineEntity.operationTicks = 0;
		}
	}

	public int[] getAdjacentFromIndex(
			CJTileEntityMachineBase machineEntity, int index) {

		int x = machineEntity.xCoord;
		int y = machineEntity.yCoord;
		int z = machineEntity.zCoord;

		switch(index) {
			case 0: x += 1; break;
			case 1: x -= 1; break;
			case 2: z += 1; break;
			case 3: z -= 1; break;
		}

		return new int[] { x, y, z };
	}

	@Override
	public void onNeighbourChange(World world, int x, int y, int z) {
		adjacentBlocks = new Block[4];

		adjacentBlocks[0] = Blocks.BLOCKS_LIST[world.getBlockId(x + 1, y, z)];
		adjacentBlocks[1] = Blocks.BLOCKS_LIST[world.getBlockId(x - 1, y, z)];
		adjacentBlocks[2] = Blocks.BLOCKS_LIST[world.getBlockId(x, y, z + 1)];
		adjacentBlocks[3] = Blocks.BLOCKS_LIST[world.getBlockId(x, y, z - 1)];
	}
}
