package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJTileEntityMachineBase;
import net.minecraft.src.game.nbt.NBTTagCompound;

public interface CJIMachine {
	void updateMachine(CJTileEntityMachineBase machineEntity);

	// NOTE: Should be returned in the range [ 0, PROGRESS_WIDTH ].
	int getProgress(CJTileEntityMachineBase machineEntity, int index);

	void progressToNBT(
			CJTileEntityMachineBase machineEntity, int index,
			NBTTagCompound progressBarsTag);

	void progressFromNBT(
			CJTileEntityMachineBase machineEntity, int index,
			NBTTagCompound progressBarsTag);
}
