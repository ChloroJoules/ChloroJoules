package io.github.chlorojoules.client.item;

import io.github.chlorojoules.client.CJRarity;
import io.github.chlorojoules.client.CJRarityInfo;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.item.description.ItemDesc;

import java.util.List;

public class CJItemDescriptionSocket extends ItemDesc {
	@Override
	public void runDesc(List<String> desc, ItemStack item) {
		if(!(item.getItem() instanceof CJIItemSocket)) return;

		CJRarity rarity = CJRarityInfo.getDamageRarity(item.itemDamage);

		String description =
				CJRarityInfo.getRarityDescription(rarity).description;

		desc.add(StringTranslate.getInstance().translateKey(description));
	}
}
