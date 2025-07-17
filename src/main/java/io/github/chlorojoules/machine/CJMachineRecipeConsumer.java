package io.github.chlorojoules.machine;

import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;

public class CJMachineRecipeConsumer implements CJIMachine {
	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		CJMachineBuilder builder = machineEntity.machine.machineBuilder;
		CJMachineRecipe recipe = builder.getMatchingRecipe(machineEntity);

		if(recipe == null) {
			machineEntity.operationTicks = 0;
			return;
		}

		builder.runRecipe(recipe, machineEntity);
	}
}
