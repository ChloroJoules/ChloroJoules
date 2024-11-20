package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJTileEntityMachineBase;
import net.minecraft.src.game.level.World;

public interface CJIMachine {
	void updateMachine(CJTileEntityMachineBase machineEntity);
	void onNeighbourChange(
			World world, int x, int y, int z);
}
