package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.src.game.level.World;

public class CJMachineRecipeConsumer implements CJIMachine {
	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		CJMachineBuilder builder = machineEntity.machineBuilder;
		CJMachineRecipe recipe = builder.getMatchingRecipe(machineEntity);

		if(recipe == null) return;

		builder.runRecipe(recipe, machineEntity);
	}

	@Override
	public void onNeighbourChange(World world, int x, int y, int z) {}
}
