package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJTank;
import io.github.chlorojoules.client.CJTankVolume;
import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.src.game.level.World;

import static io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase.machineEntity;

public class CJMachineSlooper implements CJIMachine {
	// TODO: Generic API for machines which maintain a set of adjacent
	//       Machines.
	private CJTileEntityMachineBase[] adjacentMachines = null;

	public CJMachineFlooper linked = null;

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
	}

	@Override
	public void onNeighbourChange(World world, int x, int y, int z) {
	}
}
