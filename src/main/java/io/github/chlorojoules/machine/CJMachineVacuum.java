package io.github.chlorojoules.machine;

import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.entity.other.EntityItem;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.math.AxisAlignedBB;

import java.util.List;

@SuppressWarnings("unused")
public class CJMachineVacuum implements CJIMachine {
	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		if(machineEntity.isPaused) return;

		AxisAlignedBB aabb = new AxisAlignedBB(
				machineEntity.xCoord - 1, machineEntity.yCoord - 1,
				machineEntity.zCoord - 1,
				machineEntity.xCoord + 2, machineEntity.yCoord + 2,
				machineEntity.zCoord + 2);

		List<Entity> entities = machineEntity.worldObj.getEntitiesWithinAABB(
				EntityItem.class, aabb);

		for(Entity entity : entities) {
			ItemStack worldStack = ((EntityItem) entity).item;
			ItemStack buffer = machineEntity.getStackInSlot(0);

			if(buffer == null) {
				machineEntity.setInventorySlotContents(0, worldStack);
				entity.setEntityDead();
				continue;
			}

			int space = buffer.getMaxStackSize() - buffer.stackSize;
			if(space <= 0) break;
			if(worldStack.getItemID() != buffer.getItemID()) continue;
			if(worldStack.itemDamage != buffer.itemDamage) continue;
			if(worldStack.hasTagCompound()) continue;

			int consume = Math.min(space, worldStack.stackSize);
			buffer.stackSize += consume;
			worldStack.stackSize -= consume;

			if(worldStack.stackSize <= 0) {
				entity.setEntityDead();
			}
		}
	}

	@Override
	public boolean canPause(CJTileEntityMachineBase machineEntity) {
		return true;
	}
}
