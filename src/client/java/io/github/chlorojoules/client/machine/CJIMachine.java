package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.nbt.NBTTagCompound;

public interface CJIMachine {
	void updateMachine(CJTileEntityMachineBase machineEntity);

	default void onNeighbourChange(World world, int x, int y, int z) {}
	default void onBreak(World world, int x, int y, int z) {}

	default void writeToNBT(NBTTagCompound tagCompound) {}
	default void readFromNBT(NBTTagCompound tagCompound) {}
}
