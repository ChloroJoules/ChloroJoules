package io.github.chlorojoules.client.gui;

import io.github.chlorojoules.client.*;
import io.github.chlorojoules.client.machine.CJMachineBuilder;
import net.minecraft.src.client.gui.GuiContainer;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import static io.github.chlorojoules.client.gui.CJGuiMachineBaseLayout.*;

public class CJGuiMachineBase extends GuiContainer {
	public static final int MACHINE_TEXT = 4210752;
	public static final int MACHINE_ERROR = Block.COLOR_RED;
	public static final int TOOLTIP_BACKGROUND = -1073741824;

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

		// Error message.
		String errorMessage = machineEntity.errorMessage;
		if(errorMessage != null) {
			fontRenderer.drawString(
					errorMessage,
					xSize / 2 - fontRenderer.getStringWidth(errorMessage) / 2,
					14,
					MACHINE_ERROR);
		}

		// "Inventory" label.
		fontRenderer.drawString(
				inventory, 8, ySize - 96 + 2, MACHINE_TEXT);
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
		CJGuiMachineBaseSlot slot;
		int slotX;
		int slotY;
		for(int i = 0; i < machine.slots.size(); i++) {
			slot = (CJGuiMachineBaseSlot) machine.slots.get(i);
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

		// Draw progress bars.
		CJGuiElement element;
		int elementX;
		int elementY;
		for(int i = 0; i < machineBuilder.progressBars.size(); i++) {
			element = machineBuilder.progressBars.get(i);
			elementX = baseX + element.xDisplayPosition;
			elementY = baseY + element.yDisplayPosition;

			drawTexturedModalRect(
					elementX, elementY,
					PROGRESS_EMPTY_X, PROGRESS_EMPTY_Y,
					PROGRESS_WIDTH, PROGRESS_HEIGHT);

			int width = machineEntity.operationTicks * PROGRESS_WIDTH;
			drawTexturedModalRect(
					elementX, elementY,
					PROGRESS_FULL_X, PROGRESS_FULL_Y,
					width / machineEntity.operationLength, PROGRESS_HEIGHT);
		}
	}
}
