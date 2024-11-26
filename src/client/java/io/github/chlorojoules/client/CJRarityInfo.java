package io.github.chlorojoules.client;

import io.github.chlorojoules.client.item.CJItemDescription;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.item.description.ItemDesc;

import java.awt.*;

import static io.github.chlorojoules.client.CJRarity.*;

public class CJRarityInfo {
	public static final int PRIMAL_COLOR = Color.GRAY.getRGB();
	public static final int MANUFACTURED_COLOR = 0;
	public static final int REFINED_COLOR = -100999924;
	public static final int AWAKENED_COLOR = Block.COLOR_CYAN;

	public static final int MAX_DAMAGE = 4;

	private static final CJItemDescription noneDescription =
			new CJItemDescription(StringTranslate.getInstance().translateKey(
					"message.cj_tool_none"));

	private static final CJItemDescription primalDescription =
			new CJItemDescription(StringTranslate.getInstance().translateKey(
					"message.cj_tool_primal"));

	private static final CJItemDescription manufacturedDescription =
			new CJItemDescription(StringTranslate.getInstance().translateKey(
					"message.cj_tool_manufacturer"));

	private static final CJItemDescription refinedDescription =
			new CJItemDescription(StringTranslate.getInstance().translateKey(
					"message.cj_tool_refined"));

	private static final CJItemDescription awakenedDescription =
			new CJItemDescription(StringTranslate.getInstance().translateKey(
					"message.cj_tool_awakened"));

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
		if(itemID == CJClient.primalJewel.getRegisteredItemId()) {
			return PRIMAL;
		}
		else if(itemID == CJClient.manufacturedJewel.getRegisteredItemId()) {
			return MANUFACTURED;
		}
		else if(itemID == CJClient.refinedJewel.getRegisteredItemId()) {
			return CJRarity.REFINED;
		}
		else if(itemID == CJClient.awakenedJewel.getRegisteredItemId()) {
			return CJRarity.AWAKENED;
		}

		return CJRarity.INVALID;
	}

	public static int getRarityJewel(CJRarity rarity) {
		switch(rarity) {
			case PRIMAL: {
				return CJClient.primalJewel.getRegisteredItemId();
			}
			case MANUFACTURED: {
				return CJClient.manufacturedJewel.getRegisteredItemId();
			}
			case REFINED: {
				return CJClient.refinedJewel.getRegisteredItemId();
			}
			case AWAKENED: {
				return CJClient.awakenedJewel.getRegisteredItemId();
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
