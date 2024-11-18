package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.*;

public class CJMachineLiquefier implements CJIMachine {
	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		// TODO: Basic machines like this don't need their own `CJIMachine`
		//       Implementation.

		CJMachineBuilder builder = machineEntity.machineBuilder;
		CJMachineRecipe recipe = builder.getMatchingRecipe(machineEntity);

		if(recipe == null) return;

		builder.runRecipe(recipe, machineEntity);
	}
}
