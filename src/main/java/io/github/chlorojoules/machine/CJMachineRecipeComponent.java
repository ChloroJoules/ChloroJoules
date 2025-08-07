package io.github.chlorojoules.machine;

import com.google.gson.JsonObject;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.Ingredient;
import net.minecraft.common.util.JsonUtils;

public class CJMachineRecipeComponent {
	public CJMachineRecipeTarget target;
	public String targetID;

	public Ingredient item;
	public CJTankVolume volume;

	public int tagStackSize = 1;

	public float chance = 1.0f;
	public boolean optional = false;

	public CJMachineRecipeComponent(String targetID, CJTankVolume volume) {
		this.target = CJMachineRecipeTarget.TANK;
		this.targetID = targetID;
		this.volume = volume;
	}

	public CJMachineRecipeComponent(JsonObject jsonObject) {
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
			item = CJMod.ingredientFromJson(jsonObject);

			if(!(item instanceof ItemStack) && jsonObject.has("amount")) {
				tagStackSize = JsonUtils.getInt(jsonObject, "amount");
			}
		}

		targetID = JsonUtils.getString(jsonObject, "target");

		if(jsonObject.has("chance")) {
			chance = JsonUtils.getFloat(jsonObject, "chance");
		}

		if(jsonObject.has("optional")) {
			optional = JsonUtils.getBoolean(jsonObject, "optional");
		}
	}

	public int getStackSize() {
		if(item instanceof ItemStack stack) return stack.stackSize;
		return tagStackSize;
	}
}
