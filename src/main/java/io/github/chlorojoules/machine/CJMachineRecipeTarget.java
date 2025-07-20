package io.github.chlorojoules.machine;

public enum CJMachineRecipeTarget {
	SLOT,
	TANK;

	public static CJMachineRecipeTarget fromString(String id) {
		if(id.equalsIgnoreCase("tank")) return TANK;

		return SLOT;
	}
}
