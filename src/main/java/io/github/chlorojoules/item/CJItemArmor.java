package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import net.minecraft.common.item.children.ItemArmor;

public class CJItemArmor extends ItemArmor {
	public CJItemArmor(String id, int type) {
		super(id, type, CJMod.armorMaterial);

		this.setMaxDamage(99999);
	}
}
