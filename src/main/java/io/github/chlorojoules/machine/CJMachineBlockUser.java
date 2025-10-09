package io.github.chlorojoules.machine;

import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.item.block.ItemBlock;
import net.minecraft.common.item.children.ItemCharge;
import net.minecraft.common.world.World;

@SuppressWarnings("unused")
public class CJMachineBlockUser implements CJIMachine {
	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		if(machineEntity.isPaused) return;

		CJMachineBuilder machineBuilder = machineEntity.getBuilder();
		World worldObj = machineEntity.worldObj;

		int x = machineEntity.xCoord;
		int y = machineEntity.yCoord;
		int z = machineEntity.zCoord;

		switch(machineEntity.getWorldBlockMetadata()) {
			case 0 -> z++;
			case 1 -> x--;
			case 2 -> z--;
			case 3 -> x++;
		}

		if(machineBuilder.getNamedButtonState(machineEntity, "place_break")) {
			ItemStack stack = machineBuilder.getNamedStack(
					machineEntity, "buffer");

			if(stack == null) return;
			if(!worldObj.isAirBlock(x, y, z)) return;

			if(stack.getItem() instanceof ItemBlock block) {
				worldObj.setBlockAndMetadataWithNotify(
						x, y, z, block.blockID,
						block.getPlacedBlockMetadata(stack.getItemDamage()));
			}
			else if(stack.getItem() == Items.FIRE_CHARGE) {
				worldObj.setBlockWithNotify(x, y, z, Blocks.FIRE.blockID);
			}

			stack.stackSize--;

			if(stack.stackSize <= 0) {
				machineBuilder.setNamedStack(machineEntity, "buffer", null);
			}
		}
		else {
			int blockID = worldObj.getBlockId(x, y, z);
			Block block = Blocks.BLOCKS_LIST[blockID];

			if(block.isIndestructible()) return;

			block.dropBlockAsItem(
					worldObj, x, y, z, worldObj.getBlockMetadata(x, y, z));

			worldObj.setBlockWithNotify(x, y, z, Blocks.AIR.blockID);
		}
	}

	@Override
	public boolean canPause(CJTileEntityMachineBase machineEntity) {
		return true;
	}
}
