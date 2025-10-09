package io.github.chlorojoules.machine;

import io.github.chlorojoules.*;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.children.*;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;

@SuppressWarnings("unused")
public class CJMachineEnervator implements CJIMachine {
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
		for(Block flower : machineEntity.adjacentBlocks) {
			if(flower == null) continue;

			flowerCount++;

			switch(flower) {
				case BlockSilverFlower blockSilverFlower -> {
					machineEntity.operationLength -= 6;
					efficiency += 4;
				}
				case BlockGoldenFlower blockGoldenFlower -> {
					machineEntity.operationLength -= 9;
					efficiency += 8;
				}
				case BlockFlower blockFlower -> {
					machineEntity.operationLength -= 2;
					efficiency += 2;
				}
				default -> {}
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
}
