package io.github.chlorojoules.gui;

import com.google.gson.JsonObject;

import io.github.chlorojoules.CJMod;

import net.minecraft.common.block.Block;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.JsonUtils;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJGuiButton extends CJGuiElement {
	public boolean filter = false;
	public String tooltip;
	public ItemStack label;

	public CJGuiButton(String tooltip, Block block, int x, int y) {
		super(x, y);

		this.tooltip = tooltip;
		label = new ItemStack(block.getItemID(), 1);
	}

	public CJGuiButton(JsonObject jsonObject) {
		super(jsonObject);

		setSize(BUTTON_WIDTH, BUTTON_HEIGHT);

		if(jsonObject.has("filter")) {
			filter = JsonUtils.getBoolean(jsonObject, "filter");
		}

		if(!filter) {
			tooltip = JsonUtils.getString(jsonObject, "tooltip");
			label = (ItemStack) CJMod.ingredientFromJson(
					JsonUtils.getJsonObject(jsonObject, "label"));
		}
	}
}
