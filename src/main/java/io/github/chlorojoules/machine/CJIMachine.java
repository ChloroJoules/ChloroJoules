package io.github.chlorojoules.machine;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.common.world.World;

public interface CJIMachine {
	void updateMachine(CJTileEntityMachineBase machineEntity);

	default void onBreak(World world, int x, int y, int z) {}

	// TODO: Since machine storage now belongs to the tile entity these should
	//		 Probably be moved out until we have a better storage solution.
	default void writeToNBT(
			CJTileEntityMachineBase machineEntity, CompoundTag tagCompound) {}

	default void readFromNBT(
			CJTileEntityMachineBase machineEntity, CompoundTag tagCompound) {}

	default void renderTileEntityAt(
			CJTileEntityMachineBase machineEntity,
			double x, double y, double z, float deltaTicks, int progress) {}

	default boolean canPause(CJTileEntityMachineBase machineEntity) {
		return false;
	}
}
