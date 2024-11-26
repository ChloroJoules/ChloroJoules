package io.github.chlorojoules.client.item;

import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.item.description.ItemDesc;

import java.util.List;

public class CJItemDescription extends ItemDesc {
	public String description;

	// TODO: Allow to take color as well -- translate to escape code.
	public CJItemDescription(String value) {
		description = value;
	}

	@Override
	public void runDesc(List<String> desc, ItemStack item) {
		desc.add(description);
	}
}
