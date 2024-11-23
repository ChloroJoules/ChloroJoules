package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJTileEntityMachineBase;
import net.minecraft.src.game.level.World;

public class CJMachineToolStation implements CJIMachine {
	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		// TODO: Implement tool station.
	}

	@Override
	public void onNeighbourChange(
			World world, int x, int y, int z) {}
}
