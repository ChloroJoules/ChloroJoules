package io.github.chlorojoules.gui;

import com.google.gson.JsonObject;

import io.github.chlorojoules.CJMod;

import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.JsonUtils;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJGuiButton extends CJGuiElement {
	String tooltip;
	ItemStack label;

	public CJGuiButton(JsonObject jsonObject) {
		super(jsonObject);

		setSize(BUTTON_WIDTH, BUTTON_HEIGHT);

		tooltip = JsonUtils.getString(jsonObject, "tooltip");
		label = (ItemStack) CJMod.ingredientFromJson(
				JsonUtils.getJsonObject(jsonObject, "label"));
	}
}
