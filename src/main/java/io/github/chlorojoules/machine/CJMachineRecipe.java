package io.github.chlorojoules.machine;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;

import java.util.ArrayList;
import java.util.Arrays;

public class CJMachineRecipe {
	public ArrayList<CJMachineRecipeComponent> inputs = new ArrayList<>();
	public ArrayList<CJMachineRecipeComponent> outputs = new ArrayList<>();

	public CJRarity requiredRarity = CJRarity.PRIMAL;

	public int processTime = 20;

	public int fuelIndex = -1;

	boolean allowPassive = true;

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
				builder.fuelTankIndex, CJMod.fuelFluid, fuelVolume)
				.setTarget(CJMachineRecipeTarget.TANK);

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
				builder.fuelTankIndex, CJMod.fuelFluid, fuelVolume)
				.setTarget(CJMachineRecipeTarget.TANK);

		inputs.add(fuelComponent);
		inputs.addAll(Arrays.asList(in));

		outputs.addAll(Arrays.asList(out));
	}

	// In + In -> Out.
	public CJMachineRecipe(
			CJMachineRecipeComponent in, CJMachineRecipeComponent out,
			int ticks) {

		processTime = ticks;

		inputs.add(in);
		outputs.add(out);
	}

	public CJMachineRecipeComponent getFuelComponent() {
		if(fuelIndex == -1) return null;

		return inputs.get(fuelIndex);
	}
}
