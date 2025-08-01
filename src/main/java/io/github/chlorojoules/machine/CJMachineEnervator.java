package io.github.chlorojoules.machine;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.item.CJIItemSocket;
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
		machineEntity.operationLength = 100;

		if(catalyst.current == 0) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_optional");

			return;
		}

		if(jewelStack == null) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_jewel");

			return;
		}
	}
}
