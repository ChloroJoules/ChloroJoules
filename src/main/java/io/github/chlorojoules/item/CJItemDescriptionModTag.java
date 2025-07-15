package io.github.chlorojoules.item;

import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.description.ItemDesc;
import net.minecraft.common.util.ChatColors;
import net.minecraft.common.world.World;

import java.util.List;

public class CJItemDescriptionModTag implements ItemDesc {
	public CJItemDescriptionModTag() {}

	@Override
	public void runDesc(World world, List<String> desc, ItemStack item) {
		desc.add(ChatColors.BLUE + "Chlorojoules");
	}
}
