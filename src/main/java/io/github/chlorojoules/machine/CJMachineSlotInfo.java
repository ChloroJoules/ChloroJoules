package io.github.chlorojoules.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.gui.CJGuiElement;
import io.github.chlorojoules.gui.CJGuiGravity;
import io.github.chlorojoules.gui.CJMachineSlotRenderType;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.Ingredient;
import net.minecraft.common.util.JsonUtils;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJMachineSlotInfo extends CJGuiElement {
	public boolean output = false;
	public Ingredient[] allowedItems = null;
	public CJMachineSlotRenderType renderType =
			CJMachineSlotRenderType.DEFAULT;

	public CJMachineSlotInfo(int x, int y) {
		super(x, y);
	}

	public CJMachineSlotInfo(JsonObject jsonObject) {
		super(jsonObject);

		if(jsonObject.has("output")) {
			output = JsonUtils.getBoolean(jsonObject, "output");
		}

		if(output) setSize(SLOT_OUT_WIDTH, SLOT_OUT_HEIGHT);
		else setSize(SLOT_IN_WIDTH, SLOT_IN_HEIGHT);

		id = JsonUtils.getString(jsonObject, "name");

		if(jsonObject.has("allowed")) {
			JsonArray allowed = JsonUtils.getJsonArray(jsonObject, "allowed");

			allowedItems = new ItemStack[allowed.size()];
			for(int i = 0; i < allowed.size(); ++i) {
				allowedItems[i] =
						CJMod.ingredientFromJson(
								allowed.get(i).getAsJsonObject());
			}
		}

		if(jsonObject.has("render")) {
			String render = JsonUtils.getString(jsonObject, "render");
			renderType = CJMachineSlotRenderType.fromString(render);
		}
	}

	public CJMachineSlotInfo setGravity(CJGuiGravity value) {
		super.setGravity(value);
		return this;
	}

	public CJMachineSlotInfo setRenderType(CJMachineSlotRenderType value) {
		renderType = value;
		return this;
	}

	public CJMachineSlotInfo setAllowedItems(ItemStack[] value) {
		allowedItems = value;
		return this;
	}

	public boolean isAllowedItem(ItemStack stack) {
		if(allowedItems == null) return true;

		for(Ingredient allowed : allowedItems) {
			if(CJMod.matchIngredientLenient(stack, allowed)) {
				return true;
			}
		}

		return false;
	}
}
