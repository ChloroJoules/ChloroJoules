package io.github.chlorojoules.machine;

import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;

public class CJMachineBugBlock implements CJIMachine {
	public static final int INPUT_SLOT = 0;
	public static final int OUTPUT_SLOT = 1;

	public static final int INPUT_TANK = 0;

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		ItemStack inputStack = machineEntity.stacks.get(INPUT_SLOT);
		ItemStack outputStack = machineEntity.stacks.get(OUTPUT_SLOT);

		CJTankVolume tank = machineEntity.tanks.get(INPUT_TANK);

		if(inputStack != null) {
			if(inputStack.stackSize == 1) {
				machineEntity.stacks.set(INPUT_SLOT, null);
			}
			else {
				inputStack.stackSize--;
			}

			tank.addFluid(Blocks.LAVA_MOVING.blockID, 1, true);
		}

		if(outputStack == null) {
			if(tank.removeFluid(0, 10, true) == 10) {
				machineEntity.stacks.set(
						OUTPUT_SLOT, new ItemStack(Items.SNOWBALL));

				machineEntity.onInventoryChanged();
			}
		}
		else if(outputStack.stackSize < outputStack.getMaxStackSize()) {
			if(tank.removeFluid(0, 10, true) == 10) {
				outputStack.stackSize++;
				machineEntity.onInventoryChanged();
			}
		}
	}
}
