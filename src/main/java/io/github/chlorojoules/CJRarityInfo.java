package io.github.chlorojoules;

import io.github.chlorojoules.item.CJItemDescription;
import net.minecraft.common.block.Block;

import java.awt.*;

import static io.github.chlorojoules.CJRarity.*;

public class CJRarityInfo {
	public static final int PRIMAL_COLOR = Color.GRAY.getRGB();
	public static final int MANUFACTURED_COLOR = Color.GREEN.getRGB();
	public static final int REFINED_COLOR = Color.YELLOW.getRGB();
	public static final int AWAKENED_COLOR = Color.CYAN.getRGB();

	public static final int MAX_DAMAGE = 4;

	private static final CJItemDescription noneDescription =
			new CJItemDescription("message.cj_tool_none");

	private static final CJItemDescription primalDescription =
			new CJItemDescription("message.cj_tool_primal");

	private static final CJItemDescription manufacturedDescription =
			new CJItemDescription("message.cj_tool_manufactured");

	private static final CJItemDescription refinedDescription =
			new CJItemDescription("message.cj_tool_refined");

	private static final CJItemDescription awakenedDescription =
			new CJItemDescription("message.cj_tool_awakened");

	public static int getRarityColor(CJRarity rarity) {
		switch(rarity) {
			case PRIMAL: return PRIMAL_COLOR;
			case MANUFACTURED: return MANUFACTURED_COLOR;
			case REFINED: return REFINED_COLOR;
			case AWAKENED: return AWAKENED_COLOR;
		}

		return -1;
	}

	public static int getRarityDamage(CJRarity rarity) {
		switch(rarity) {
			case PRIMAL: return 3;
			case MANUFACTURED: return 2;
			case REFINED: return 1;
			case AWAKENED: return 0;
		}

		return MAX_DAMAGE;
	}

	public static CJRarity getDamageRarity(int damage) {
		switch(damage) {
			case 3: return PRIMAL;
			case 2: return MANUFACTURED;
			case 1: return REFINED;
			case 0: return AWAKENED;
		}

		return INVALID;
	}

	public static CJItemDescription getRarityDescription(CJRarity rarity) {
		switch(rarity) {
			case PRIMAL: return primalDescription;
			case MANUFACTURED: return manufacturedDescription;
			case REFINED: return refinedDescription;
			case AWAKENED: return awakenedDescription;
		}

		return noneDescription;
	}

	public static CJRarity getJewelRarity(int itemID) {
		if(itemID == CJMod.fauxJewel.itemID) {
			return PRIMAL;
		}
		else if(itemID == CJMod.primalJewel.itemID) {
			return PRIMAL;
		}
		else if(itemID == CJMod.manufacturedJewel.itemID) {
			return MANUFACTURED;
		}
		else if(itemID == CJMod.refinedJewel.itemID) {
			return CJRarity.REFINED;
		}
		else if(itemID == CJMod.awakenedJewel.itemID) {
			return CJRarity.AWAKENED;
		}

		return CJRarity.INVALID;
	}

	public static int getRarityJewel(CJRarity rarity) {
		switch(rarity) {
			case PRIMAL: {
				return CJMod.primalJewel.itemID;
			}
			case MANUFACTURED: {
				return CJMod.manufacturedJewel.itemID;
			}
			case REFINED: {
				return CJMod.refinedJewel.itemID;
			}
			case AWAKENED: {
				return CJMod.awakenedJewel.itemID;
			}
		}

		return -1;
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
