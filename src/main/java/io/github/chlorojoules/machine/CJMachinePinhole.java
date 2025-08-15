package io.github.chlorojoules.machine;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;

@SuppressWarnings("unused")
public class CJMachinePinhole implements CJIMachine {
	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		StringTranslate translate = StringTranslate.getInstance();

		CJMachineBuilder machineBuilder = machineEntity.getBuilder();

		ItemStack jewelStack =
				machineBuilder.getNamedStack(machineEntity, "jewel");

		ItemStack fragmentStack =
				machineBuilder.getNamedStack(machineEntity, "fragment");

		CJTankVolume fuelVolume =
				machineBuilder.getNamedTankVolume(machineEntity, "fuel");

		machineEntity.errorMessage = null;
		machineEntity.isWarning = false;
		machineEntity.isPassive = false;
		machineEntity.operationLength = 100;
		machineEntity.riftDensity = 0;

		if(jewelStack == null) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_jewel");

			return;
		}

		if(fragmentStack == null) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_fragment");

			return;
		}

		CJRarity rarity = CJRarityInfo.getJewelRarity(jewelStack.getItemID());
		if(rarity != CJRarity.AWAKENED) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_poor_jewel");

			return;
		}

		int fuel = 100;
		if(fuelVolume.current <= fuel) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_power");

			return;
		}

		machineEntity.riftDensity = 1;
		machineEntity.operationTicks++;

		if(machineEntity.operationTicks >= machineEntity.operationLength) {
			machineEntity.operationTicks = 0;

			fuelVolume.removeFluid(0, fuel, false);

			ItemStack stack;
			if(machineEntity.worldObj.rand.nextFloat() < 0.7f) {
				stack = new ItemStack(CJMod.otherworldEssence);
			}
			else {
				int max = CJMod.otherworldEligibleItems.size();
				int index = machineEntity.worldObj.rand.nextInt(max);

				stack = new ItemStack(
						CJMod.otherworldEligibleItems.get(index));
			}

			CJMod.spawnItem(
					machineEntity.worldObj, machineEntity.xCoord + 0.5,
					machineEntity.yCoord + 1.5, machineEntity.zCoord + 0.5,
					stack, true);
		}
	}

	public void renderTileEntityAt(
			CJTileEntityMachineBase machineEntity,
			double x, double y, double z, float deltaTicks, int progress) {

		double scale = (Math.sin(
				machineEntity.renderDelta / 50.0) * 0.5 + 0.5) * 0.05 + 0.15;

		CJMachineRiftBeacon.renderCubeVortex(
				machineEntity, x, y + 1.5, z, deltaTicks, progress,
				(float) scale, 0.0f, 1.0f, 1.0f, 0.6f);
	}
}
