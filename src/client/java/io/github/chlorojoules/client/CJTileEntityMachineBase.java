package io.github.chlorojoules.client;

import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.level.World;

// TODO: Client stuff.
/*
private MachineContainer container;
container = new MachineContainer();
public class MachineContainer extends Container {

}
 */

class TileEntityMachineBase extends TileEntity {
	public static TileEntityMachineBase machineEntity(
			World world, int x, int y, int z) {

		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		return (TileEntityMachineBase) tileEntity;
	}

	public void onBreak(World world, int x, int y, int z) {
		// TODO: Drop item stacks.
		// TODO: Preserve fluid tanks etc. Should we make machines retain
		//  	 Inventory on break?
		/*
		dropStack = new ItemStack(item.itemID, size, item.getItemDamage());
		EntityItem entity = new EntityItem(world, x, y, z, dropStack);

		world.entityJoinedWorld(entity);
		if(entity.hasTagCompound()) {
			entity.item.setTagCompound(stack.getTagCompound());
		}
		 */
	}
}
