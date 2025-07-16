package io.github.chlorojoules.gui;

// Just used for progress bar arrows right now. Can be used for any element
// Which doesn't have any real metadata associated with it.

import com.google.gson.JsonObject;
import net.minecraft.common.util.JsonUtils;

public class CJGuiElement {
	public String id;
	public CJGuiGravity gravity = CJGuiGravity.TOP_LEFT;
	public int xDisplayPosition = 0;
	public int yDisplayPosition = 0;
	public int width = 0;
	public int height = 0;
	public int[] damageExclusive = null;

	public CJGuiElement(int x, int y) {
		xDisplayPosition = x;
		yDisplayPosition = y;
	}

	public CJGuiElement(JsonObject jsonObject) {
		if(jsonObject.has("name")) {
			id = JsonUtils.getString(jsonObject, "name");
		}

		if(jsonObject.has("anchor")) {
			gravity = CJGuiGravity.fromString(
					JsonUtils.getString(jsonObject, "anchor"));
		}

		if(jsonObject.has("x")) {
			xDisplayPosition = JsonUtils.getInt(jsonObject, "x");
		}
		if(jsonObject.has("y")) {
			yDisplayPosition = JsonUtils.getInt(jsonObject, "y");
		}
	}

	public int getXPlacement() {
		if(gravity == null) return xDisplayPosition;
		return gravity.alignX(xDisplayPosition) - (width / 2);
	}

	public int getYPlacement() {
		if(gravity == null) return yDisplayPosition;
		return gravity.alignY(yDisplayPosition) - (height / 2);
	}

	public CJGuiElement setGravity(CJGuiGravity value) {
		this.gravity = value;
		return this;
	}

	public CJGuiElement setSize(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}

	public CJGuiElement setDamageExclusive(int[] damageExclusive) {
		this.damageExclusive = damageExclusive;
		return this;
	}
}
