package io.github.chlorojoules.client;

import net.minecraft.src.client.gui.GuiContainer;
import net.minecraft.src.client.gui.Slot;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.block.BlockFluid;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.entity.player.InventoryPlayer;

import net.minecraft.src.game.item.ItemStack;
import org.lwjgl.opengl.GL11;

// TODO: Ghosts in the class here? Extracting this to its own `.java` file
//		 Causes symbol resolution to fail at compile time.
class CJSlotMachineBase extends Slot {
	private final EntityPlayer entityPlayer;

	private boolean output = false;

	public CJSlotMachineBase(
			EntityPlayer entityPlayer, IInventory inventory,
			int index, int x, int y) {

		super(inventory, index, x, y);

		this.entityPlayer = entityPlayer;
	}

	@Override
	public boolean isItemValid(ItemStack item) {
		return !output;
	}

	@Override
	public void onPickupFromSlot(ItemStack item) {
		item.onCrafting(entityPlayer.worldObj, entityPlayer);
		super.onPickupFromSlot(item);
	}

	CJSlotMachineBase setOutput(boolean value) {
		output = value;
		return this;
	}

	boolean isOutput() {
		return output;
	}
}

public class CJGuiMachineBase extends GuiContainer {
	public static final int MACHINE_TEXT_COLOR = 4210752;

	// TODO: Switch this to ChloroJoules.
	public static final BlockFluid fuelFluid = (BlockFluid) Block.waterMoving;

	// Game slot hitbox size.
	private static final int SLOT_WIDTH = 16;
	private static final int SLOT_HEIGHT = 16;

	// NOTE: Positional information from `resources/cj_machinebase.xcf` image
	// Source split by layer.
	private static final int PROGRESS_FULL_X = 176;
	private static final int PROGRESS_FULL_Y = 0;

	private static final int PROGRESS_EMPTY_X = 176;
	private static final int PROGRESS_EMPTY_Y = 17;

	private static final int PROGRESS_WIDTH = 24;
	private static final int PROGRESS_HEIGHT = 17;

	private static final int SLOT_IN_X = 176;
	private static final int SLOT_IN_Y = 34;

	private static final int SLOT_IN_WIDTH = 18;
	private static final int SLOT_IN_HEIGHT = 18;

	private static final int SLOT_IN_OFFSET_X =
			(SLOT_IN_WIDTH - SLOT_WIDTH) / 2;

	private static final int SLOT_IN_OFFSET_Y =
			(SLOT_IN_HEIGHT - SLOT_HEIGHT) / 2;

	private static final int SLOT_OUT_X = 176;
	private static final int SLOT_OUT_Y = 52;

	private static final int SLOT_OUT_WIDTH = 26;
	private static final int SLOT_OUT_HEIGHT = 26;

	private static final int SLOT_OUT_OFFSET_X =
			(SLOT_OUT_WIDTH - SLOT_WIDTH) / 2;

	private static final int SLOT_OUT_OFFSET_Y =
			(SLOT_OUT_HEIGHT - SLOT_HEIGHT) / 2;

	private static final int FLUID_FULL_X = 176;
	private static final int FLUID_FULL_Y = 78;

	private static final int FLUID_EMPTY_X = 188;
	private static final int FLUID_EMPTY_Y = 78;

	private static final int FLUID_WIDTH = 12;
	private static final int FLUID_HEIGHT = 53;

	private final CJTileEntityMachineBase machineEntity;

	public CJGuiMachineBase(
			InventoryPlayer inventoryPlayer, CJTileEntityMachineBase machine) {

		super(new CJContainerMachineBase(inventoryPlayer, machine));

		this.machineEntity = machine;
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		CJContainerMachineBase machine =
				(CJContainerMachineBase) inventorySlots;

		StringTranslate translate = StringTranslate.getInstance();

		String name = translate.translateKey(machineEntity.getInvName());
		String inventory = translate.translateKey("inventory.generic");

		// TODO: De-magic these placements.
		// Machine label.
		fontRenderer.drawString(
				name,
				xSize / 2 - fontRenderer.getStringWidth(name) / 2, 6,
				MACHINE_TEXT_COLOR);

		// "Inventory" label.
		fontRenderer.drawString(
				inventory, 8, ySize - 96 + 2, MACHINE_TEXT_COLOR);

		// TODO: Debug only.
		for(int i = 0; i < machineEntity.tanks.length; i++) {
			CJTankVolume tankVolume = machineEntity.tanks[i];

			if(tankVolume.fluidID != 0) {
				fontRenderer.drawString(
						Block.blocksList[tankVolume.fluidID].getBlockName(),
						2, 2 + (i * 8),
						MACHINE_TEXT_COLOR);
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float deltaTicks) {
		CJContainerMachineBase machine =
				(CJContainerMachineBase) inventorySlots;

		int texture = mc.renderEngine.getTexture("/gui/cj_machinebase.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);

		int baseX = (width - xSize) / 2;
		int baseY = (height - ySize) / 2;

		// Draw UI base.
		drawTexturedModalRect(baseX, baseY, 0, 0, xSize, ySize);

		// Draw slots.
		CJSlotMachineBase slot;
		int slotX;
		int slotY;
		for(int i = 0; i < machine.slots.size(); i++) {
			slot = (CJSlotMachineBase) machine.slots.get(i);
			slotX = baseX + slot.xDisplayPosition;
			slotY = baseY + slot.yDisplayPosition;

			if(slot.isOutput()) {
				drawTexturedModalRect(
						slotX - SLOT_OUT_OFFSET_X, slotY - SLOT_OUT_OFFSET_Y,
						SLOT_OUT_X, SLOT_OUT_Y,
						SLOT_OUT_WIDTH, SLOT_OUT_HEIGHT);
			}
			else {
				drawTexturedModalRect(
						slotX - SLOT_IN_OFFSET_X, slotY - SLOT_IN_OFFSET_Y,
						SLOT_IN_X, SLOT_IN_Y,
						SLOT_IN_WIDTH, SLOT_IN_HEIGHT);
			}
		}

		// Draw tanks.
		CJTank tank;
		CJTankVolume tankVolume;
		int tankX;
		int tankY;
		for(int i = 0; i < machine.tanks.size(); i++) {
			tank = machine.tanks.get(i);
			tankVolume = machineEntity.tanks[i];
			tankX = baseX + tank.xDisplayPosition;
			tankY = baseY + tank.yDisplayPosition;

			drawTexturedModalRect(
					tankX, tankY,
					FLUID_FULL_X, FLUID_FULL_Y,
					FLUID_WIDTH, FLUID_HEIGHT);

			// Overlay the full tank graphic with an amount of the empty one.
			// TODO: This doesn't account for the margins of the tank which it
			//		 Probably should do.
			int tankFill = (tankVolume.current * FLUID_HEIGHT) / CJTank.MAX;
			int tankEmptyDrawHeight = FLUID_HEIGHT - tankFill;

			drawTexturedModalRect(
					tankX, tankY,
					FLUID_EMPTY_X, FLUID_EMPTY_Y,
					FLUID_WIDTH, tankEmptyDrawHeight);
		}

		// TODO: Draw tanks as filling with fluid texture rather than just a
		//  	 Green bar.
	}
}
