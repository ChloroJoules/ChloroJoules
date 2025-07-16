package io.github.chlorojoules.machine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.util.JsonUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class CJMachineRecipe {
	public ArrayList<CJMachineRecipeComponent> inputs = new ArrayList<>();
	public ArrayList<CJMachineRecipeComponent> outputs = new ArrayList<>();

	public int processTime = 20;
	public int fuelIndex = -1;

	public CJRarity requiredRarity = CJRarity.PRIMAL;
	boolean allowPassive = false;
	public int requiredButton = -1;

	// Fuel + In -> Out.
	public CJMachineRecipe(
			CJMachineBuilder builder, int fuelVolume,
			CJMachineRecipeComponent in, CJMachineRecipeComponent out,
			int ticks, boolean allowPassive) {

		processTime = ticks;
		this.allowPassive = allowPassive;

		if(builder.fuelTankIndex == -1) {
			throw new RuntimeException("No fuel tank in machine");
		}

		fuelIndex = 0;
		CJMachineRecipeComponent fuelComponent = new CJMachineRecipeComponent(
				builder.fuelTankIndex,
				new CJTankVolume(CJMod.fluidChlorojoules, fuelVolume));

		inputs.add(fuelComponent);
		inputs.add(in);

		outputs.add(out);
	}

	// Fuel + In[] -> Out[].
	public CJMachineRecipe(
			CJMachineBuilder builder, int fuelVolume,
			CJMachineRecipeComponent[] in, CJMachineRecipeComponent[] out,
			int ticks, boolean allowPassive) {

		processTime = ticks;
		this.allowPassive = allowPassive;

		if(builder.fuelTankIndex == -1) {
			throw new RuntimeException("No fuel tank in machine");
		}

		fuelIndex = 0;
		CJMachineRecipeComponent fuelComponent = new CJMachineRecipeComponent(
				builder.fuelTankIndex,
				new CJTankVolume(CJMod.fluidChlorojoules, fuelVolume));

		inputs.add(fuelComponent);
		inputs.addAll(Arrays.asList(in));

		outputs.addAll(Arrays.asList(out));
	}

	// In + In -> Out.
	public CJMachineRecipe(
			CJMachineRecipeComponent in, CJMachineRecipeComponent out,
			int ticks) {

		allowPassive = true;
		processTime = ticks;

		inputs.add(in);
		outputs.add(out);
	}

	public CJMachineRecipe() {}

	// TODO: Remove builder from here and use string IDs.
	public CJMachineRecipe(CJMachineBuilder builder, JsonObject jsonObject) {
		if(jsonObject.has("passive")) {
			allowPassive = JsonUtils.getBoolean(jsonObject, "passive");
		}

		processTime = JsonUtils.getInt(jsonObject, "ticks");

		if(jsonObject.has("fuel")) {
			fuelIndex = inputs.size();

			inputs.add(new CJMachineRecipeComponent(
					builder.fuelTankIndex,
					new CJTankVolume(
							CJMod.fluidChlorojoules,
							JsonUtils.getInt(jsonObject, "fuel"))));
		}

		if(jsonObject.has("button")) {
			requiredButton = builder.getButtonIndex(
					JsonUtils.getString(jsonObject, "button"));
		}

		for(JsonElement input : JsonUtils.getJsonArray(jsonObject, "inputs")) {
			inputs.add(new CJMachineRecipeComponent(
					builder, input.getAsJsonObject()));
		}

		for(JsonElement output :
				JsonUtils.getJsonArray(jsonObject, "outputs")) {

			outputs.add(new CJMachineRecipeComponent(
					builder, output.getAsJsonObject()));
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

	public CJMachineRecipe setProcessTime(int value) {
		processTime = value;
		return this;
	}

	public CJMachineRecipe setRequiredRarity(CJRarity value) {
		requiredRarity = value;
		return this;
	}

	public CJMachineRecipe setAllowPassive(boolean value) {
		allowPassive = value;
		return this;
	}

	public CJMachineRecipe setRequiredButton(int value) {
		requiredButton = value;
		return this;
	}

	public CJMachineRecipeComponent getFuelComponent() {
		if(fuelIndex == -1) return null;

		return inputs.get(fuelIndex);
	}
}
