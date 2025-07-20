package io.github.chlorojoules.machine;

public enum CJMachineBlockSideMode {
	FRONT_FACE,
	ALL_SIDES,
	ALL_FACES;

	public static CJMachineBlockSideMode fromString(String id) {
		return switch(id.toLowerCase()) {
			//case "front" -> FRONT_FACE;
			case "sides" -> ALL_SIDES;
			case "all" -> ALL_FACES;

			default -> FRONT_FACE;
		};
	}
}
