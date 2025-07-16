package io.github.chlorojoules.machine;

public enum CJMachineTier {
	PRIMITIVE,
	INDUSTRIAL;

	public static CJMachineTier fromString(String id) {
		return switch(id.toLowerCase()) {
			case "primitive" -> PRIMITIVE;
			case "industrial" -> INDUSTRIAL;

			default -> INDUSTRIAL;
		};
	}
}
