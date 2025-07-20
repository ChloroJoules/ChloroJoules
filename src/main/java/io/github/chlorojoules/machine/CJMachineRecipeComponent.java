package io.github.chlorojoules.machine;

import com.google.gson.JsonObject;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.JsonUtils;

public class CJMachineRecipeComponent {
	public CJMachineRecipeTarget target;
	public int index;

	public ItemStack stack;

	public CJTankVolume volume;

	public String tag;
	public int tagCount;
	public boolean isTag = false;

	public float chance = 1.0f;
	public boolean optional = false;

	public CJMachineRecipeComponent(int index, ItemStack stack) {
		this.target = CJMachineRecipeTarget.SLOT;
		this.index = index;
		this.stack = stack;
	}

	public CJMachineRecipeComponent(int index, CJTankVolume volume) {
		this.target = CJMachineRecipeTarget.TANK;
		this.index = index;
		this.volume = volume;
	}

	// TODO: Remove builder param and use string slot IDs.
	public CJMachineRecipeComponent(
			CJMachineBuilder builder, JsonObject jsonObject) {

		if(jsonObject.has("kind")) {
			target = CJMachineRecipeTarget.fromString(
					JsonUtils.getString(jsonObject, "kind"));
		}

		if(jsonObject.has("fluid")) {
			target = CJMachineRecipeTarget.TANK;
			volume = new CJTankVolume(jsonObject);
		}
		else if(jsonObject.has("item")) {
			target = CJMachineRecipeTarget.SLOT;
			stack = CJMod.stackFromJson(jsonObject);
		}
		else {
			isTag = true;
			tag = JsonUtils.getString(jsonObject, "tag");

			if(jsonObject.has("amount")) {
				tagCount = JsonUtils.getInt(jsonObject, "amount");
			}
			else tagCount = 1;
		}

		String id = JsonUtils.getString(jsonObject, "target");
		if(target == CJMachineRecipeTarget.SLOT) {
			index = builder.getSlotIndex(id);
		}
		else index = builder.getTankIndex(id);

		if(jsonObject.has("chance")) {
			chance = JsonUtils.getFloat(jsonObject, "chance");
		}

		if(jsonObject.has("optional")) {
			optional = JsonUtils.getBoolean(jsonObject, "optional");
		}
	}

	public int getStackSize() {
		if(isTag) return tagCount;
		return stack.stackSize;
	}
}
