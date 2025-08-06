package io.github.chlorojoules.item;

import net.minecraft.common.block.Block;
import net.minecraft.common.block.children.BlockLeavesBase;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.children.ItemTool;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.item.data.ToolMaterials;

public class CJItemToolPruningShears extends ItemTool {
	public CJItemToolPruningShears(String id) {
		super(id, 1, ToolMaterials.GOLD, EnumTools.HOE);

		setMaxDamage(75);
	}

	@Override
	public float getStrVsBlock(ItemStack stack, Block block) {
		return block instanceof BlockLeavesBase ? 10.0F : 0.0F;
	}

	@Override
	public boolean canHarvestBlock(Block block) {
		return block instanceof BlockLeavesBase;
	}
}
