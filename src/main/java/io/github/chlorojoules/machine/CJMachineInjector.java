package io.github.chlorojoules.machine;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.item.CJItemFluidContainer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;

@SuppressWarnings("unused")
public class CJMachineInjector extends CJMachineRecipeConsumer {
	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		StringTranslate translate = StringTranslate.getInstance();

		CJMachineBuilder machineBuilder = machineEntity.getBuilder();
		ItemStack input = machineBuilder.getNamedStack(machineEntity, "input");

		if(input == null ||
				!(input.getItem() instanceof CJItemFluidContainer)) {

			super.updateMachine(machineEntity);
			return;
		}

		ItemStack jewelStack =
				machineBuilder.getNamedStack(machineEntity, "jewel");

		CJTankVolume injectVolume =
				machineBuilder.getNamedTankVolume(machineEntity, "inject");

		machineEntity.errorMessage = null;
		machineEntity.isWarning = false;
		machineEntity.isPassive = false;
		machineEntity.operationLength = 100;

		if(jewelStack == null) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_jewel");

			return;
		}

		if(machineBuilder.getNamedButtonState(machineEntity, "refine_fuel")) {
			injectVolume =
					machineBuilder.getNamedTankVolume(machineEntity, "fuel");
		}

		CJRarity rarity = CJRarityInfo.getJewelRarity(jewelStack.getItemID());

		machineEntity.operationLength /=
				CJRarityInfo.getRarityTimeScale(rarity);

		if(injectVolume.current < 1) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_bad_recipe");

			return;
		}

		CompoundTag tagCompound = input.getTagCompound();
		CJTankVolume volume = new CJTankVolume();
		if(tagCompound != null) volume.readFromNBT(tagCompound);

		if(volume.fluidID != 0 && injectVolume.fluidID != volume.fluidID) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_bad_recipe");

			return;
		}

		if(volume.current >= volume.max) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_bad_recipe");

			return;
		}

		if(++machineEntity.operationTicks >= machineEntity.operationLength) {
			int amount = Math.min(Math.min(
					volume.max - volume.current, injectVolume.current),
					CJTank.BUCKET);

			int fluid = injectVolume.fluidID;
			amount = injectVolume.removeFluid(0, amount, false);
			volume.addFluid(fluid, amount, false);

			volume.writeToNBT(input.getTagCompoundNonNull());

			machineEntity.operationTicks = 0;
		}
	}
}
