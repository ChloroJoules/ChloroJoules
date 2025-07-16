package io.github.chlorojoules.machine;

public enum CJMachineRecipeTarget {
	SLOT,
	TANK;

	public static CJMachineRecipeTarget fromString(String id) {
		return switch(id.toLowerCase()) {
			case "item" -> SLOT;
			case "tank" -> TANK;
			default -> SLOT;
		};
	}
}
