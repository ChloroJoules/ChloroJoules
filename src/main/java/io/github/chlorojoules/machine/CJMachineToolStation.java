package io.github.chlorojoules.machine;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.item.CJIItemSocket;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;

public class CJMachineToolStation implements CJIMachine {
	public static final int INPUT_SLOT = 0;
	public static final int TOOL_SLOT = 1;

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		StringTranslate translate = StringTranslate.getInstance();

		ItemStack inputStack = machineEntity.stacks.get(INPUT_SLOT);
		ItemStack toolStack = machineEntity.stacks.get(TOOL_SLOT);

		machineEntity.errorMessage = null;
		machineEntity.isWarning = false;
		machineEntity.isPassive = false;
		machineEntity.operationLength = 100;

		if(inputStack == null) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_bad_recipe");

			return;
		}

		if(toolStack == null) {
			if(!(inputStack.getItem() instanceof CJIItemSocket)) {
				machineEntity.operationTicks = 0;
				machineEntity.errorMessage =
						translate.translateKey("message.cj_bad_recipe");

				return;
			}

			int damage = inputStack.itemDamage;
			if(damage == CJRarityInfo.MAX_DAMAGE) {
				machineEntity.operationTicks = 0;
				machineEntity.errorMessage =
						translate.translateKey("message.cj_no_jewel");

				return;
			}

			++machineEntity.operationTicks;
			if(machineEntity.operationTicks >= machineEntity.operationLength) {
				machineEntity.operationTicks = 0;

				inputStack.setItemDamage(CJRarityInfo.MAX_DAMAGE);

				CJRarity rarity = CJRarityInfo.getDamageRarity(damage);
				int id = CJRarityInfo.getRarityJewel(rarity);
				machineEntity.stacks.set(TOOL_SLOT, new ItemStack(id, 1));
			}

			return;
		}

		if(!(toolStack.getItem() instanceof CJIItemSocket)) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_bad_tool");

			return;
		}

		if(toolStack.itemDamage != CJRarityInfo.MAX_DAMAGE) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_full_tool");

			return;
		}

		CJRarity rarity = CJRarityInfo.getJewelRarity(
				inputStack.getItem().itemID);

		if(rarity == CJRarity.INVALID ||
				inputStack.getItem() == CJMod.fauxJewel) {

			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_bad_jewel");

			return;
		}

		if(++machineEntity.operationTicks >= machineEntity.operationLength) {
			machineEntity.operationTicks = 0;

			toolStack.setItemDamage(CJRarityInfo.getRarityDamage(rarity));
			machineEntity.stacks.set(INPUT_SLOT, null);
		}
	}
}
