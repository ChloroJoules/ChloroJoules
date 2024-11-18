package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.*;
import io.github.chlorojoules.client.gui.CJGuiMachineBaseLayout;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.nbt.NBTTagCompound;

public class CJMachineLiquefier implements CJIMachine {
	public static final int INPUT_SLOT = 0;
	public static final int JEWEL_SLOT = 1;

	public static final int INPUT_TANK = 0;
	public static final int OUTPUT_TANK = 1;

	private static final int BASE_OPERATION_COST = 10;
	private static final int BASE_OPERATION_TIME = 50;

	// TODO: Common counters like this should have a helper partial machine
	//       Implementation.
	private int ticksSinceOperation = 0;
	private int operationTime = BASE_OPERATION_TIME;

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		machineEntity.errorMessage = null;

		StringTranslate translate = StringTranslate.getInstance();

		ItemStack stack = machineEntity.stacks.get(INPUT_SLOT);
		ItemStack jewel = machineEntity.stacks.get(JEWEL_SLOT);

		CJTankVolume inputTank = machineEntity.tanks.get(INPUT_TANK);
		CJTankVolume outputTank = machineEntity.tanks.get(OUTPUT_TANK);

		if(jewel == null) {
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_jewel");

			return;
		}

		CJRarity jewelRarity = CJRarityInfo.getJewelRarity(jewel.itemID);

		// TODO: Make Jewel slot visually distinct and reject non-Jewel items.
		if(jewelRarity == CJRarity.INVALID) {
			machineEntity.errorMessage =
					translate.translateKey("message.cj_bad_jewel");

			return;
		}

		if(stack == null) return;
		else {
			// TODO: Machine recipe registry in client.
			int itemID = Block.leaves.asRegisteredItem().getRegisteredItemId();

			if(stack.itemID != itemID) {
				machineEntity.errorMessage =
						translate.translateKey("message.cj_bad_recipe");

				return;
			}
		}

		if(outputTank.current == outputTank.max) return;

		operationTime = BASE_OPERATION_TIME / CJRarityInfo.getRarityTimeScale(
				jewelRarity);

		int cost = BASE_OPERATION_COST / CJRarityInfo.getRarityPowerScale(
				jewelRarity);

		boolean passive = false;
		if(inputTank.current < cost) {
			if(jewelRarity == CJRarity.PRIMAL) {
				operationTime *= 2;
				passive = true;
			}
			else {
				machineEntity.errorMessage =
						translate.translateKey("message.cj_no_power");

				return;
			}
		}

		if(ticksSinceOperation++ < operationTime) return;

		if(!passive) {
			if(inputTank.removeFluid(0, cost, true) != cost) {
				machineEntity.errorMessage =
						translate.translateKey("message.cj_no_power");

				return;
			}
		}

		if(stack.stackSize == 1) machineEntity.stacks.set(INPUT_SLOT, null);
		else stack.stackSize--;

		outputTank.addFluid(Block.lavaMoving.getBlockID(), 50, false);

		ticksSinceOperation = 0;
	}

	@Override
	public int getProgress(CJTileEntityMachineBase machineEntity, int index) {
		int maxWidth = CJGuiMachineBaseLayout.PROGRESS_WIDTH;

		return (ticksSinceOperation * maxWidth) / operationTime;
	}

	@Override
	public void progressToNBT(
			CJTileEntityMachineBase machineEntity, int index,
			NBTTagCompound progressBarsTag) {

		progressBarsTag.setShort("Ticks", (short) ticksSinceOperation);
	}

	@Override
	public void progressFromNBT(
			CJTileEntityMachineBase machineEntity, int index,
			NBTTagCompound progressBarsTag) {

		ticksSinceOperation = progressBarsTag.getShort("Ticks");
	}
}
