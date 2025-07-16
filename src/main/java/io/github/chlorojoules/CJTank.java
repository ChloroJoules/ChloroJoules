package io.github.chlorojoules;

import com.google.gson.JsonObject;
import io.github.chlorojoules.gui.CJGuiElement;
import io.github.chlorojoules.gui.CJGuiGravity;
import net.minecraft.common.util.JsonUtils;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

// NOTE: All units are in millibuckets.

// This is to a fluid as a `Slot` is to an item.
public class CJTank extends CJGuiElement {
	// Millibuckets per bucket.
	public static final int BUCKET = 1000;

	public String id;
	public boolean output = false;
	public boolean bidirectional = false;

	public CJTank(int x, int y) {
		super(x, y);

		setSize(FLUID_WIDTH, FLUID_HEIGHT);
	}

	public CJTank(CJGuiGravity gravity, int x, int y) {
		super(x, y);

		setSize(FLUID_WIDTH, FLUID_HEIGHT);

		this.gravity = gravity;
	}

	public CJTank(JsonObject jsonObject) {
		super(jsonObject);

		setSize(FLUID_WIDTH, FLUID_HEIGHT);

		id = JsonUtils.getString(jsonObject, "name");

		if(jsonObject.has("output")) {
			output = JsonUtils.getBoolean(jsonObject, "output");
		}

		if(jsonObject.has("bidirectional")) {
			bidirectional = JsonUtils.getBoolean(jsonObject, "bidirectional");
		}
	}

	public CJTank setOutput(boolean value) {
		output = value;
		return this;
	}

	public CJTank setBidirectional(boolean value) {
		bidirectional = value;
		return this;
	}
}
