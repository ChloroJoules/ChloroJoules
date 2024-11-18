package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJClient;
import io.github.chlorojoules.client.CJRarity;

import java.util.ArrayList;

public class CJMachineRecipe {
	public ArrayList<CJMachineRecipeComponent> inputs = new ArrayList<>();
	public ArrayList<CJMachineRecipeComponent> outputs = new ArrayList<>();

	// TODO: Add flag for allowing passive operation.
	public CJRarity requiredRarity = CJRarity.PRIMAL;

	public int processTime = 20;

	public int fuelIndex = -1;

	// Fuel + In -> Out.
	public CJMachineRecipe(
			CJMachineBuilder builder, int fuelVolume,
			CJMachineRecipeComponent in, CJMachineRecipeComponent out,
			int ticks) {

		processTime = ticks;

		if(builder.fuelTankIndex == -1) {
			throw new RuntimeException("No fuel tank in machine");
		}

		fuelIndex = 0;
		CJMachineRecipeComponent fuelComponent = new CJMachineRecipeComponent(
				builder.fuelTankIndex, CJClient.fuelFluid, fuelVolume)
				.setTarget(CJMachineRecipeTarget.TANK);

		inputs.add(fuelComponent);
		inputs.add(in);

		outputs.add(out);
	}

	// TODO: This will need a post-process if fuel is set non-standardly.
	public CJMachineRecipeComponent getFuelComponent() {
		if(fuelIndex == -1) return null;

		return inputs.get(fuelIndex);
	}
}
