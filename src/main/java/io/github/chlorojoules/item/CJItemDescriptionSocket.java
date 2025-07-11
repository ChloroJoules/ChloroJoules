package io.github.chlorojoules.item;

import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.description.ItemDesc;
import net.minecraft.common.util.i18n.StringTranslate;
import net.minecraft.common.world.World;

import java.util.List;

public class CJItemDescriptionSocket implements ItemDesc {
	@Override
	public void runDesc(World world, List<String> desc, ItemStack item) {
		if(!(item.getItem() instanceof CJIItemSocket)) return;

		CJRarity rarity = CJRarityInfo.getDamageRarity(item.itemDamage);

		String description =
				CJRarityInfo.getRarityDescription(rarity).description;

		desc.add(StringTranslate.getInstance().translateKey(description));
	}
}
