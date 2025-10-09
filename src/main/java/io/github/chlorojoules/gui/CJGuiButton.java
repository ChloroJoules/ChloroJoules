package io.github.chlorojoules.gui;

import com.google.gson.JsonObject;

import io.github.chlorojoules.CJMod;

import net.minecraft.common.block.Block;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.JsonUtils;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJGuiButton extends CJGuiElement {
	public CJGuiButtonLabelKind labelKind = CJGuiButtonLabelKind.ITEM;
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

		if(jsonObject.has("kind")) {
			labelKind = CJGuiButtonLabelKind.fromString(
					JsonUtils.getString(jsonObject, "kind"));
		}

		if(labelKind == CJGuiButtonLabelKind.ITEM) {
			tooltip = JsonUtils.getString(jsonObject, "tooltip");
			label = (ItemStack) CJMod.ingredientFromJson(
					JsonUtils.getJsonObject(jsonObject, "label"));
		}
	}
}
