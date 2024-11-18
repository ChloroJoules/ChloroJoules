package io.github.chlorojoules.client;

import net.minecraft.src.client.gui.GuiContainer;
import net.minecraft.src.client.gui.Slot;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.client.inventory.IInventory;
import net.minecraft.src.game.block.Block;
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
		// TODO: This currently fires for inventory pickup aswell.
		//item.onCrafting(entityPlayer.worldObj, entityPlayer);
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
	public static final int MACHINE_TEXT = 4210752;
	public static final int TOOLTIP_BACKGROUND = -1073741824;

	// TODO: Switch this to ChloroJoules.
	public static final int fuelFluid = Block.waterMoving.blockID;

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

	private static final int FLUID_OFFSET_X = 1;
	private static final int FLUID_OFFSET_Y = 1;

	private final CJTileEntityMachineBase machineEntity;
	private final CJMachineBuilder machineBuilder;

	public CJGuiMachineBase(
			InventoryPlayer inventoryPlayer, CJTileEntityMachineBase entity,
			CJMachineBuilder builder) {

		super(new CJContainerMachineBase(inventoryPlayer, entity, builder));

		machineBuilder = builder;
		machineEntity = entity;
	}

	private boolean getIsMouseOverTank(CJTank tank, int x, int y) {
		int widthScaled = (width - xSize) / 2;
		int heightScaled = (height - ySize) / 2;

		x -= widthScaled;
		y -= heightScaled;

		return x >= tank.xDisplayPosition - FLUID_OFFSET_X
				&& x < tank.xDisplayPosition + FLUID_WIDTH + FLUID_OFFSET_X
				&& y >= tank.yDisplayPosition - FLUID_OFFSET_Y
				&& y < tank.yDisplayPosition + FLUID_HEIGHT + FLUID_OFFSET_Y;
	}

	// TODO: This floats tooltips what appears to be in absolute rather than
	//		 Relative space.
	private void drawTooltip(String name, String description, int x, int y) {
		int widthScaled = (width - xSize) / 2;
		int heightScaled = (height - ySize) / 2;

		int nameWidth = fontRenderer.getStringWidth(name);
		int descriptionWidth = fontRenderer.getStringWidth(description);
		int textWidth = Math.max(nameWidth, descriptionWidth);

		// TODO: Loads of magic numbers down here.
		int xSlot = x - widthScaled + 12;
		int ySlot = y - heightScaled - 12;

		this.drawGradientRect(
				xSlot - 3, ySlot - 3,
				xSlot + textWidth + 3, ySlot + 26,
				TOOLTIP_BACKGROUND, TOOLTIP_BACKGROUND
		);

		fontRenderer.drawStringWithShadow(description, xSlot, ySlot + 15, -1);
		fontRenderer.drawStringWithShadow(name, xSlot, ySlot, -1);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float deltaTicks) {
		super.drawScreen(mouseX, mouseY, deltaTicks);

		CJContainerMachineBase machine =
				(CJContainerMachineBase) inventorySlots;

		for(int i = 0; i < machine.tanks.size(); i++) {
			CJTank tank = machine.tanks.get(i);
			CJTankVolume tankVolume = machineEntity.tanks.get(i);

			if(getIsMouseOverTank(tank, mouseX, mouseY)) {
				String name = "Nothing";

				if(tankVolume.fluidID != 0) {
					Block fluid = Block.blocksList[tankVolume.fluidID];
					name = fluid.translateBlockName();
				}

				drawTooltip(
						name,
						tankVolume.current + "/" + tankVolume.max + "mB",
						mouseX, mouseY);

				return;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		CJContainerMachineBase machine =
				(CJContainerMachineBase) inventorySlots;

		StringTranslate translate = StringTranslate.getInstance();

		// TODO: Source name from container/block.
		String name = translate.translateKey(machineEntity.getInvName());
		String inventory = translate.translateKey("inventory.generic");

		// TODO: De-magic these placements.
		// Machine label.
		fontRenderer.drawString(
				name,
				xSize / 2 - fontRenderer.getStringWidth(name) / 2, 6,
				MACHINE_TEXT);

		// "Inventory" label.
		fontRenderer.drawString(
				inventory, 8, ySize - 96 + 2, MACHINE_TEXT);

		// TODO: Debug only.
		for(int i = 0; i < machineEntity.tanks.size(); i++) {
			CJTankVolume tankVolume = machineEntity.tanks.get(i);

			if(tankVolume.fluidID != 0) {
				fontRenderer.drawString(
						Block.blocksList[tankVolume.fluidID].getBlockName(),
						2, 2 + (i * 8),
						MACHINE_TEXT);
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
			tankVolume = machineEntity.tanks.get(i);
			tankX = baseX + tank.xDisplayPosition;
			tankY = baseY + tank.yDisplayPosition;

			drawTexturedModalRect(
					tankX, tankY,
					FLUID_FULL_X, FLUID_FULL_Y,
					FLUID_WIDTH, FLUID_HEIGHT);

			// TODO: Fix this.
			/*
			if(tankVolume.fluidID != fuelFluid) {
				Block fluid = Block.blocksList[tankVolume.fluidID];
				TextureStitched fluidIcon =
						(TextureStitched) fluid.getBlockTextureFromSide(0);

				int fluidTexture =
						mc.renderEngine.getTexture("/terrain.png");

				mc.renderEngine.bindTexture(fluidTexture);

				drawTexturedModalRect(
						tankX + FLUID_OFFSET_X, tankY + FLUID_OFFSET_Y,
						fluidIcon.getOriginX(), fluidIcon.getOriginY(),
						FLUID_WIDTH - (FLUID_OFFSET_X * 2),
						FLUID_HEIGHT - (FLUID_OFFSET_Y * 2));

				mc.renderEngine.bindTexture(texture);
			}*/

			// Overlay the full tank graphic with an amount of the empty one.
			// TODO: This doesn't account for the margins of the tank which it
			//		 Probably should do.
			int tankFill =
					(tankVolume.current * FLUID_HEIGHT) / tankVolume.max;

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
