package io.github.chlorojoules.gui;

// Just used for progress bar arrows right now. Can be used for any element
// Which doesn't have any real metadata associated with it.

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.machine.CJMachineBuilder;
import net.minecraft.common.util.JsonUtils;

import java.util.Arrays;

public class CJGuiElement {
	public String id;
	public CJGuiGravity gravity = CJGuiGravity.TOP_LEFT;
	public int xDisplayPosition = 0;
	public int yDisplayPosition = 0;
	public int width = 0;
	public int height = 0;
	public String[] damageExclusive = null;

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

		if(jsonObject.has("damages")) {
			JsonArray damages = JsonUtils.getJsonArray(jsonObject, "damages");
			damageExclusive = new String[damages.size()];
			for(int i = 0; i < damages.size(); ++i) {
				damageExclusive[i] = damages.get(i).getAsString();
			}
		}
	}

	public boolean matchesDamageExclusive(
			CJTileEntityMachineBase machineEntity) {

		if(damageExclusive == null) return true;

		int meta = machineEntity.getWorldBlockMetadata();
		CJMachineBuilder machineBuilder = machineEntity.machine.machineBuilder;

		return Arrays.stream(damageExclusive).anyMatch(
						x -> machineBuilder.getNamedDamage(x) == meta);
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

	public CJGuiElement setID(String value) {
		this.id = value;
		return this;
	}

	public CJGuiElement setDamageExclusive(String[] damageExclusive) {
		this.damageExclusive = damageExclusive;
		return this;
	}
}
