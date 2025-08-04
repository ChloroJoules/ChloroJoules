package io.github.chlorojoules.machine;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.common.world.World;

public interface CJIMachine {
	void updateMachine(CJTileEntityMachineBase machineEntity);

	default void onNeighbourChange(World world, int x, int y, int z) {}
	default void onBreak(World world, int x, int y, int z) {}

	default void writeToNBT(
			CJTileEntityMachineBase machineEntity, CompoundTag tagCompound) {}

	default void readFromNBT(
			CJTileEntityMachineBase machineEntity, CompoundTag tagCompound) {}

	default void renderTileEntityAt(
			CJTileEntityMachineBase machineEntity,
			double x, double y, double z, float deltaTicks, int progress) {}
}
