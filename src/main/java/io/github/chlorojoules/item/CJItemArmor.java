package io.github.chlorojoules.item;

import net.minecraft.common.item.children.ItemArmor;
import net.minecraft.common.item.data.ArmorMaterials;

public class CJItemArmor extends ItemArmor {
	public CJItemArmor(String id, int type) {
		super(id, type, ArmorMaterials.DIAMOND);

		this.setMaxDamage(99999);
	}
}
