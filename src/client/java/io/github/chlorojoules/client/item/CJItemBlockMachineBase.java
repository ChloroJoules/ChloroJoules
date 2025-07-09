package io.github.chlorojoules.client.item;

import com.fox2code.foxloader.registry.RegisteredBlock;
import io.github.chlorojoules.client.block.CJBlockMachineBase;
import net.minecraft.src.client.renderer.block.icon.Icon;
import net.minecraft.src.game.item.ItemBlock;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.item.ItemStack;

public class CJItemBlockMachineBase extends ItemBlock {
	private RegisteredBlock block;

	public CJItemBlockMachineBase(
			int id, RegisteredBlock block, int maxMetadata) {

		super(id - 256, block.getRegisteredBlockId());

		this.block = block;

		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public Icon getIconFromDamage(int metadata) {
		// TODO: Is this icon face correct?
		return Block.blocksList[block.getRegisteredBlockId()]
				.getIcon(2, metadata);
	}

	@Override
	public int getPlacedBlockMetadata(int metadata) {
		return metadata;
	}

	@Override
	public String getItemNameIS(ItemStack itemstack) {
		CJBlockMachineBase machine =
				(CJBlockMachineBase) Block.blocksList[
						block.getRegisteredBlockId()];

		return machine.getIconName(
				super.getItemName(), itemstack.getItemDamage());
	}
}
