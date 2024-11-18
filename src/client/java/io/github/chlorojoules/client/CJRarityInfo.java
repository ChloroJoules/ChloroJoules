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

	public static CJRarity getJewelRarity(int itemID) {
		if(itemID == CJClient.primalJewel.getRegisteredItemId()) {
			return CJRarity.PRIMAL;
		}
		else if(itemID == CJClient.manufacturedJewel.getRegisteredItemId()) {
			return CJRarity.MANUFACTURED;
		}
		else if(itemID == CJClient.refinedJewel.getRegisteredItemId()) {
			return CJRarity.REFINED;
		}
		else if(itemID == CJClient.awakenedJewel.getRegisteredItemId()) {
			return CJRarity.AWAKENED;
		}

		return CJRarity.INVALID;
	}

	public static int getRarityPowerScale(CJRarity rarity) {
		switch(rarity) {
			case PRIMAL: return 1;
			case MANUFACTURED: return 2;
			case REFINED: return 3;
			case AWAKENED: return 4;
		}

		return -1;
	}

	public static int getRarityTimeScale(CJRarity rarity) {
		switch(rarity) {
			case PRIMAL: return 1;
			case MANUFACTURED: return 2;
			case REFINED: return 4;
			case AWAKENED: return 8;
		}

		return -1;
	}

	public static boolean raritySufficient(CJRarity test, CJRarity required) {
		return test.compareTo(required) >= 0;
	}
}
