package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJTankVolume;
import io.github.chlorojoules.client.CJTileEntityMachineBase;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.nbt.NBTTagCompound;

public class CJMachineBugBlock implements CJIMachine {
	public static final int INPUT_SLOT = 0;
	public static final int OUTPUT_SLOT = 1;

	public static final int INPUT_TANK = 0;

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		ItemStack inputStack = machineEntity.stacks.get(INPUT_SLOT);
		ItemStack outputStack = machineEntity.stacks.get(OUTPUT_SLOT);

		CJTankVolume tank = machineEntity.tanks.get(INPUT_TANK);

		// TODO: Helper for decreasing stack size in machine entity.
		if(inputStack != null) {
			if(inputStack.stackSize == 1) {
				machineEntity.stacks.set(INPUT_SLOT, null);
			}
			else {
				inputStack.stackSize--;
			}

			tank.addFluid(Block.lavaMoving.getBlockID(), 1, true);
		}

		if(outputStack == null) {
			if(tank.removeFluid(0, 10, true) == 10) {
				machineEntity.stacks.set(
						OUTPUT_SLOT, new ItemStack(Item.snowball));

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
