package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJTank;
import io.github.chlorojoules.client.CJTankVolume;
import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;

import net.minecraft.src.game.level.World;

import static
		io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase.machineEntity;

public class CJMachineFlooper implements CJIMachine {
	// TODO: Generic API for machines which maintain a set of adjacent
	//       Machines.
	private CJTileEntityMachineBase[] adjacentMachines = null;

	// NOTE: Transfers up to count -- may transfer less.
	public static boolean tankTransfer(
			CJTankVolume in, CJTankVolume out, int count) {

		if(in.current == 0) return false;

		int amount = Math.min(in.current, count);
		if(amount == 0) return false;

		int added = out.addFluid(in.fluidID, amount, false);
		if(added == 0) return false;

		in.removeFluid(0, added, true);

		return true;
	}

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		// TODO: Add init method which takes the `TileEntity`.
		if(adjacentMachines == null) {
			adjacentMachines = new CJTileEntityMachineBase[6];
			onNeighbourChange(
					machineEntity.worldObj,
					machineEntity.getRegisteredX(),
					machineEntity.getRegisteredY(),
					machineEntity.getRegisteredZ());
		}

		// TODO: Upgrade Flooper with ChloroJewel.
		// TODO: Avoid looping back on the same flooper which was just received
		//       From -- make creating flooper chains easier.

		CJTankVolume volume = machineEntity.tanks.get(0);

		// TODO: De-duplicate once we need this for pipes/auto-push/pull etc.
		// Machine to Flooper.
		boolean didTransfer = false;
		int inTank = -1;
		for(int i = 0; i < adjacentMachines.length; ++i) {
			CJTileEntityMachineBase adjacent = adjacentMachines[i];
			if(adjacent == null) continue;

			for(int j = 0; j < adjacent.tanks.size(); j++) {
				CJTankVolume adjacentVolume = adjacent.tanks.get(j);
				CJTank adjacentTank = adjacent.machineBuilder.tanks.get(j);

				if(!adjacentTank.output) continue;

				if(tankTransfer(adjacentVolume, volume, 10)) {
					inTank = i;
					didTransfer = true;
					break;
				}
			}

			if(didTransfer) break;
		}

		// Flooper to machine.
		didTransfer = false;
		for(int i = 0; i < adjacentMachines.length; ++i) {
			CJTileEntityMachineBase adjacent = adjacentMachines[i];
			if(adjacent == null) continue;
			if(i == inTank) continue;

			for(int j = 0; j < adjacent.tanks.size(); j++) {
				CJTankVolume adjacentVolume = adjacent.tanks.get(j);
				CJTank adjacentTank = adjacent.machineBuilder.tanks.get(j);

				if(adjacentTank.output) continue;

				if(tankTransfer(volume, adjacentVolume, 10)) {
					didTransfer = true;
					break;
				}
			}

			if(didTransfer) break;
		}
	}

	@Override
	public void onNeighbourChange(World world, int x, int y, int z) {
		if(adjacentMachines == null) return;

		adjacentMachines[0] = machineEntity(world, x + 1, y, z);
		adjacentMachines[1] = machineEntity(world, x - 1, y, z);
		adjacentMachines[2] = machineEntity(world, x, y + 1, z);
		adjacentMachines[3] = machineEntity(world, x, y - 1, z);
		adjacentMachines[4] = machineEntity(world, x, y, z + 1);
		adjacentMachines[5] = machineEntity(world, x, y, z - 1);
	}
}
