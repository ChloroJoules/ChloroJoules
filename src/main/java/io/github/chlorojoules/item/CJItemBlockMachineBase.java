package io.github.chlorojoules.item;

import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.icon.Icon;
import net.minecraft.common.block.texture.Face;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.block.ItemBlock;

public class CJItemBlockMachineBase extends ItemBlock {
	public CJItemBlockMachineBase(Block block, boolean hasSubtypes) {
		super(block);

		this.setMaxDamage(0);
		this.setHasSubtypes(hasSubtypes);
	}

	private Block getBlock() {
		return Blocks.BLOCKS_LIST[blockID];
	}

	@Override
	public Icon getIconFromDamage(int metadata) {
		return getBlock().getIcon(Face.EAST.direction(), metadata);
	}

	@Override
	public int getPlacedBlockMetadata(int metadata) {
		return metadata;
	}

	@Override
	public String getItemNameIS(ItemStack stack) {
		return super.getItemName();
	}
}
