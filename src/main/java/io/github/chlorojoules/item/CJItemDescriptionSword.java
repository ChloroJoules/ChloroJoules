package io.github.chlorojoules.item;

import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.description.ItemDesc;
import net.minecraft.common.util.ChatColors;
import net.minecraft.common.world.World;

import java.util.List;

public class CJItemDescriptionSword implements ItemDesc {
	@Override
	public void runDesc(World world, List<String> desc, ItemStack item) {
		if(!(item.getItem() instanceof CJItemToolSoulSword)) return;

		CJRarity rarity = CJRarityInfo.getDamageRarity(item.itemDamage);
		String description =
				ChatColors.BLUE + "+" +
				(CJItemToolSoulSword.getRarityDamage(rarity) + 1) + " " +
				st.translateKey("item.tool.attackdamage");

		desc.add(description);
	}
}
