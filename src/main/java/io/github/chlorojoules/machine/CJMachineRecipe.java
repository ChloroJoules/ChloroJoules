package io.github.chlorojoules.machine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.util.JsonUtils;

import java.util.ArrayList;
import java.util.Objects;

public class CJMachineRecipe {
	public ArrayList<CJMachineRecipeComponent> inputs = new ArrayList<>();
	public ArrayList<CJMachineRecipeComponent> outputs = new ArrayList<>();

	public int processTime = 20;
	public int fuelIndex = -1;

	public CJRarity requiredRarity = CJRarity.PRIMAL;
	boolean allowPassive = false;
	public String requiredButton;

	public CJMachineRecipe() {}

	public CJMachineRecipe(JsonObject jsonObject) {
		if(jsonObject.has("passive")) {
			allowPassive = JsonUtils.getBoolean(jsonObject, "passive");
		}

		if(jsonObject.has("rarity")) {
			requiredRarity = CJRarityInfo.fromString(
					JsonUtils.getString(jsonObject, "rarity"));
		}

		processTime = JsonUtils.getInt(jsonObject, "ticks");

		if(jsonObject.has("fuel")) {
			fuelIndex = inputs.size();

			inputs.add(new CJMachineRecipeComponent(
					"fuel",
					new CJTankVolume(
							CJMod.fluidChlorojoules,
							JsonUtils.getInt(jsonObject, "fuel"))));
		}

		if(jsonObject.has("button")) {
			requiredButton = JsonUtils.getString(jsonObject, "button");
		}

		for(JsonElement input : JsonUtils.getJsonArray(jsonObject, "inputs")) {
			inputs.add(new CJMachineRecipeComponent(input.getAsJsonObject()));
		}

		for(JsonElement output :
				JsonUtils.getJsonArray(jsonObject, "outputs")) {

			outputs.add(
					new CJMachineRecipeComponent(output.getAsJsonObject()));
		}
	}

	public CJMachineRecipe addInput(CJMachineRecipeComponent component) {
		inputs.add(component);
		return this;
	}

	public CJMachineRecipe addOutput(CJMachineRecipeComponent component) {
		outputs.add(component);
		return this;
	}

	public CJMachineRecipeComponent getInputByTarget(String target) {
		for(CJMachineRecipeComponent component : inputs) {
			if(component.targetID.equals(target)) return component;
		}

		throw new RuntimeException("No input with target '" + target + "'");
	}

	public CJMachineRecipeComponent getOutputByTarget(String target) {
		for(CJMachineRecipeComponent component : outputs) {
			if(component.targetID.equals(target)) return component;
		}

		throw new RuntimeException("No output with target '" + target + "'");
	}

	public CJMachineRecipe setProcessTime(int value) {
		processTime = value;
		return this;
	}

	public CJMachineRecipeComponent getFuelComponent() {
		if(fuelIndex == -1) return null;

		return inputs.get(fuelIndex);
	}
}
