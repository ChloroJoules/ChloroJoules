package io.github.chlorojoules.machine;

import io.github.chlorojoules.*;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.children.*;
import net.minecraft.common.entity.inventory.IInventory;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;
import net.minecraft.common.world.World;

import static io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase.machineEntity;

@SuppressWarnings("unused")
public class CJMachineEnervator implements CJIMachine {
	private BlockFlower[] adjacentFlowers;

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		StringTranslate translate = StringTranslate.getInstance();

		CJMachineBuilder machineBuilder = machineEntity.getBuilder();

		ItemStack jewelStack =
				machineBuilder.getNamedStack(machineEntity, "jewel");

		CJTankVolume catalyst =
				machineBuilder.getNamedTankVolume(machineEntity, "catalyst");

		CJTankVolume output =
				machineBuilder.getNamedTankVolume(machineEntity, "output");

		machineEntity.errorMessage = null;
		machineEntity.isWarning = false;
		machineEntity.isPassive = false;
		machineEntity.operationLength = 75;

		if(adjacentFlowers == null) {
			onNeighbourChange(
					machineEntity.worldObj,
					machineEntity.xCoord,
					machineEntity.yCoord,
					machineEntity.zCoord);
		}

		if(jewelStack == null) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_jewel");

			return;
		}

		if(catalyst.current == 0) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_optional");

			return;
		}

		int efficiency;
		int consumption;
		if(catalyst.fluidID == CJMod.fuelFluid) {
			efficiency = 3;
			consumption = 0;
		}
		else {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_bad_catalyst");

			return;
		}

		if(catalyst.current < consumption) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_optional");

			return;
		}

		CJRarity rarity = CJRarityInfo.getJewelRarity(jewelStack.getItemID());
		machineEntity.operationLength -=
				CJRarityInfo.getRarityTimeScale(rarity);

		int flowerCount = 0;
		for(BlockFlower flower : adjacentFlowers) {
			if(flower == null) continue;

			flowerCount++;

			if(flower instanceof BlockGoldenFlower) {
				machineEntity.operationLength -= 9;
				efficiency += 8;
			}
			else if(flower instanceof BlockSilverFlower) {
				machineEntity.operationLength -= 6;
				efficiency += 4;
			}
			else {
				machineEntity.operationLength -= 2;
				efficiency += 2;
			}
		}

		if(flowerCount == 0) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_flowers");

			return;
		}

		efficiency += CJRarityInfo.getRarityPowerScale(rarity);

		machineEntity.operationTicks++;

		if(machineEntity.operationTicks >= machineEntity.operationLength) {
			jewelStack.damageItem(1, null, true);
			output.addFluid(CJMod.fuelFluid, efficiency, false);
			catalyst.removeFluid(catalyst.fluidID, consumption, false);
			machineEntity.operationTicks = 0;
		}
	}

	@Override
	public void onNeighbourChange(World world, int x, int y, int z) {
		adjacentFlowers = new BlockFlower[4];

		Block block = Blocks.BLOCKS_LIST[world.getBlockId(x + 1, y, z)];
		if(block instanceof BlockFlower flower) adjacentFlowers[0] = flower;
		block = Blocks.BLOCKS_LIST[world.getBlockId(x - 1, y, z)];
		if(block instanceof BlockFlower flower) adjacentFlowers[0] = flower;
		block = Blocks.BLOCKS_LIST[world.getBlockId(x, y, z + 1)];
		if(block instanceof BlockFlower flower) adjacentFlowers[0] = flower;
		block = Blocks.BLOCKS_LIST[world.getBlockId(x, y, z - 1)];
		if(block instanceof BlockFlower flower) adjacentFlowers[0] = flower;
	}
}
