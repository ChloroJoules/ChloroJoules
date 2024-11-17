package io.github.chlorojoules.client;

import net.minecraft.src.game.block.BlockFluid;

// This is to a fluid as an `ItemStack` is to an item.
public class CJTankVolume {
	// Current fluid volume.
	public int current = 0;

	// Fluid type.
	public int fluidID = 0;
	// Whether fluid type is fixed -- i.e. only `fluid` accepted, don't clear
	// On empty.
	public boolean lockFluid = false;

	public int addFluid(BlockFluid blockFluid, int amount, boolean all) {
		if(fluidID != 0 && fluidID != blockFluid.getBlockID()) return 0;

		if((current + amount) > CJTank.MAX) {
			if(all) return 0;

			if(fluidID == 0) fluidID = blockFluid.getBlockID();

			int diff = CJTank.MAX - current;
			current += diff;
			return diff;
		}

		if(fluidID == 0) fluidID = blockFluid.getBlockID();
		current += amount;

		return amount;
	}

	public int removeFluid(BlockFluid blockFluid, int amount, boolean all) {
		if(blockFluid != null) {
			if(fluidID != 0) return 0;
			if(fluidID != blockFluid.getBlockID()) return 0;
		}

		if(amount > current) {
			if(all) return 0;

			int removed = current;
			current = 0;

			if(!lockFluid) fluidID = 0;

			return removed;
		}

		current -= amount;

		return amount;
	}
}
