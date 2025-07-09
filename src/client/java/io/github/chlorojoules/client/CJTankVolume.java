package io.github.chlorojoules.client;

// This is to a fluid as an `ItemStack` is to an item.
public class CJTankVolume {
	// Maximum fluid volume.
	public int max = 8 * CJTank.BUCKET;

	// Current fluid volume.
	public int current = 0;

	// Fluid type.
	public int fluidID = 0;
	// Whether fluid type is fixed -- i.e. only `fluid` accepted, don't clear
	// On empty.
	public boolean lockFluid = false;

	public int addFluid(int blockFluid, int amount, boolean all) {
		if(fluidID != 0 && fluidID != blockFluid) return 0;

		if((current + amount) > max) {
			if(all) return 0;

			if(fluidID == 0) fluidID = blockFluid;

			int diff = max - current;
			current += diff;
			return diff;
		}

		if(fluidID == 0) fluidID = blockFluid;
		current += amount;

		return amount;
	}

	public int removeFluid(int blockFluid, int amount, boolean all) {
		if(blockFluid != 0) {
			if(fluidID != blockFluid) return 0;
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

	// NOTE: Transfers up to count into tank from `in` -- may transfer less.
	public boolean transferFrom(CJTankVolume in, int count) {
		if(in.current == 0) return false;

		int amount = Math.min(in.current, count);
		if(amount == 0) return false;

		int added = addFluid(in.fluidID, amount, false);
		if(added == 0) return false;

		in.removeFluid(0, added, true);

		return true;
	}
}
