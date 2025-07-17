package io.github.chlorojoules.machine;

import net.minecraft.common.block.data.Material;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.block.sound.StepSound;
import net.minecraft.common.block.sound.StepSounds;
import net.minecraft.common.item.data.EnumTools;

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

	public Material getMaterial() {
		return switch(this) {
			case PRIMITIVE -> Materials.WOOD;
			case INDUSTRIAL -> Materials.ROCK;
		};
	}

	public StepSound getStepSound() {
		return switch(this) {
			case PRIMITIVE -> StepSounds.SOUND_WOOD;
			case INDUSTRIAL -> StepSounds.SOUND_STONE;
		};
	}

	public EnumTools getEffectiveTool() {
		return switch(this) {
			case PRIMITIVE -> EnumTools.AXE;
			case INDUSTRIAL -> EnumTools.PICKAXE;
		};
	}

	public float getHardness() {
		return switch(this) {
			case PRIMITIVE -> 0.75F;
			case INDUSTRIAL -> 1.5F;
		};
	}

	public float getResistance() {
		return switch(this) {
			case PRIMITIVE -> 7.5F;
			case INDUSTRIAL -> 10.0F;
		};
	}
}
