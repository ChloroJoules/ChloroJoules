package io.github.chlorojoules.client.container;

import io.github.chlorojoules.client.CJTank;
import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.client.gui.CJGuiMachineBaseSlot;
import io.github.chlorojoules.client.machine.CJMachineBuilder;
import io.github.chlorojoules.client.machine.CJMachineSlotInfo;
import net.minecraft.src.client.gui.Container;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.entity.player.InventoryPlayer;
import net.minecraft.src.game.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

// TODO: Mixin on `ItemBucket.onItemRightClick' to auto-register non-Vanilla
//		 Fluids to bucket items.

public class CJContainerMachineBase extends Container {
	private final CJTileEntityMachineBase machineEntity;
	private final CJMachineBuilder machineBuilder;

	public ArrayList<CJTank> tanks = new ArrayList<>();

	void addPlayerInventory(InventoryPlayer inventory) {
		EntityPlayer player = inventory.player;

		// TODO: Can we de-magic this a little bit?
		for(int row = 0; row < 3; row++) {
			for(int column = 0; column < 9; column++) {
				addSlot(new CJGuiMachineBaseSlot(
						player, inventory, column + (row * 9) + 9,
						8 + (column * 18), 84 + (row * 18)));
			}
		}

		for(int i = 0; i < 9; i++) {
			addSlot(new CJGuiMachineBaseSlot(
					player, inventory, i, 8 + i * 18, 142));
		}
	}

	public CJContainerMachineBase(
			InventoryPlayer inventoryPlayer, CJTileEntityMachineBase entity,
			CJMachineBuilder builder) {

		machineEntity = entity;
		machineBuilder = builder;

		EntityPlayer player = inventoryPlayer.player;

		for(int i = 0; i < machineBuilder.slots.size(); i++) {
			CJMachineSlotInfo info = machineBuilder.slots.get(i);

			if(info.damageExclusive != null) {
				boolean matchesExclusiveMetadata =
						ArrayUtils.contains(
								info.damageExclusive,
								machineEntity.getWorldBlockMetadata());

				if(!matchesExclusiveMetadata) continue;
			}

			CJGuiMachineBaseSlot slot =
					new CJGuiMachineBaseSlot(
							player, machineEntity, i,
							info.xDisplayPosition, info.yDisplayPosition)
					.setOutput(info.output);

			addSlot(slot);
		}

		for(int i = 0; i < machineBuilder.tanks.size(); ++i) {
			CJTank tank = machineBuilder.tanks.get(i);

			if(tank.damageExclusive != null) {
				boolean matchesExclusiveMetadata =
						ArrayUtils.contains(
								tank.damageExclusive,
								machineEntity.getWorldBlockMetadata());

				if(!matchesExclusiveMetadata) continue;
			}

			tanks.add(tank);
		}

		addPlayerInventory(inventoryPlayer);
	}

	// TODO: Update machine interface in these two (?).
	@Override
	public void updateInventory() {
		super.updateInventory();
	}

	@Override
	public void func_20112_a(int slot, int arg2) {
		// This is for updating the progress bar (apparently)?
		// arg2 might be the progress amount/delta, but it's unclear.
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityPlayer) {
		return machineEntity.canInteractWith(entityPlayer);
	}

	// TODO: Investigate how quick moves work.
	@Override
	public ItemStack quickMove(int index) {
		return null;
	}
}
