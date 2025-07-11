package io.github.chlorojoules.item;

import io.github.chlorojoules.block.CJBlockMachineBase;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.icon.Icon;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.block.ItemBlock;

public class CJItemBlockMachineBase extends ItemBlock {
	public CJItemBlockMachineBase(Block block) {
		super(block);

		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	private Block getBlock() {
		return Blocks.BLOCKS_LIST[blockID];
	}

	@Override
	public Icon getIconFromDamage(int metadata) {
		// TODO: Is this icon face correct?
		return getBlock().getIcon(2, metadata);
	}

	@Override
	public int getPlacedBlockMetadata(int metadata) {
		return metadata;
	}

	@Override
	public String getItemNameIS(ItemStack itemstack) {
		CJBlockMachineBase machine = (CJBlockMachineBase) getBlock();

		return machine.getIconName(
				super.getItemName(), itemstack.getItemDamage());
	}
}
