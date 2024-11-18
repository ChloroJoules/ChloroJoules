package io.github.chlorojoules.client;

import net.minecraft.src.game.block.Block;

import java.awt.*;

public class CJRarityInfo {
	public static final int PRIMAL_COLOR = Color.GRAY.getRGB();
	public static final int MANUFACTURED_COLOR = 0;
	public static final int REFINED_COLOR = -100999924;
	public static final int AWAKENED_COLOR = Block.COLOR_CYAN;

	public static int getRarityColor(CJRarity rarity) {
		switch(rarity) {
			case PRIMAL: return PRIMAL_COLOR;
			case MANUFACTURED: return MANUFACTURED_COLOR;
			case REFINED: return REFINED_COLOR;
			case AWAKENED: return AWAKENED_COLOR;
		}

		return -1;
	}
}
