package io.github.chlorojoules.client;

public interface CJIMachine {
	void updateMachine(CJTileEntityMachineBase machineEntity);

	// NOTE: Should be returned in the range [ 0, PROGRESS_WIDTH ].
	int getProgress(CJTileEntityMachineBase machineEntity, int index);
}
