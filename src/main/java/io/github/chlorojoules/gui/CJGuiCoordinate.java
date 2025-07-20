package io.github.chlorojoules.gui;

import com.google.gson.JsonObject;
import net.minecraft.common.util.JsonUtils;

public class CJGuiCoordinate extends CJGuiElement {
	public String label;

	public CJGuiCoordinate(JsonObject jsonObject) {
		super(jsonObject);

		label = JsonUtils.getString(jsonObject, "label");
	}
}
