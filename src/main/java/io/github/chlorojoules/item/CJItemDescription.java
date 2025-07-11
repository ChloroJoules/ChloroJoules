package io.github.chlorojoules.item;

import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.description.ItemDesc;
import net.minecraft.common.world.World;

import java.util.List;

public class CJItemDescription implements ItemDesc {
	public String description;

	// TODO: Allow to take color as well -- translate to escape code.
	public CJItemDescription(String value) {
		description = value;
	}

	@Override
	public void runDesc(World world, List<String> desc, ItemStack item) {
		desc.add(description);
	}
}
