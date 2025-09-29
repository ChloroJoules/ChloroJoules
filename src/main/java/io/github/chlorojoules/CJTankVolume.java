package io.github.chlorojoules;

import com.google.gson.JsonObject;
import com.mojang.nbt.CompoundTag;
import net.minecraft.common.block.Block;
import net.minecraft.common.util.JsonUtils;

// This is to a fluid as an `ItemStack` is to an item.
public class CJTankVolume {
	public static final int DEFAULT_MAX = CJTank.BUCKET;

	// Maximum fluid volume.
	public int max = DEFAULT_MAX;

	// Current fluid volume.
	public int current = 0;

	// Fluid type.
	public int fluidID = 0;
	// Whether fluid type is fixed -- i.e. only `fluid` accepted, don't clear
	// On empty.
	public boolean lockFluid = false;

	public CJTankVolume() {}

	public CJTankVolume(Block fluid, int volume) {
		this.fluidID = fluid.blockID;
		this.current = volume;
	}

	public CJTankVolume(JsonObject jsonObject) {
		if(jsonObject.has("volume")) {
			max = JsonUtils.getInt(jsonObject, "volume");
		}

		if(jsonObject.has("amount")) {
			current = JsonUtils.getInt(jsonObject, "amount");
		}

		if(jsonObject.has("fluid")) {
			fluidID = CJMod.fluidFromName(
					JsonUtils.getString(jsonObject, "fluid")).blockID;
		}

		if(jsonObject.has("lock")) {
			lockFluid = JsonUtils.getBoolean(jsonObject, "lock");
		}
	}

	public void writeToNBT(CompoundTag tagCompound) {
		// NOTE: `max` and `lockFluid` are expected to be set statically
		//       Per-machine so we don't need to serialize them.
		tagCompound.setInteger("current", current);
		tagCompound.setInteger("fluid", fluidID);
	}

	public void readFromNBT(CompoundTag tagCompound) {
		current = tagCompound.getInteger("current");
		fluidID = tagCompound.getInteger("fluid");
	}

	public CJTankVolume setMax(int value) {
		max = value;
		return this;
	}

	public CJTankVolume setLockFluid(int value) {
		fluidID = value;
		lockFluid = true;
		return this;
	}

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
		if(!lockFluid && current == 0) fluidID = 0;

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
