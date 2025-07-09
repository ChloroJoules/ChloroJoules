package io.github.chlorojoules.client.gui;

import io.github.chlorojoules.client.*;
import io.github.chlorojoules.client.container.CJContainerMachineBase;
import io.github.chlorojoules.client.machine.CJMachineBuilder;
import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.client.machine.CJMachineSlotInfo;
import net.minecraft.src.client.gui.GuiContainer;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.entity.player.InventoryPlayer;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static io.github.chlorojoules.client.gui.CJGuiMachineBaseLayout.*;

public class CJGuiMachineBase extends GuiContainer {
	public static final int MACHINE_TEXT = Color.DARK_GRAY.getRGB();
	public static final int MACHINE_OK = Block.COLOR_GREEN;
	public static final int MACHINE_WARNING = Block.COLOR_ORANGE;
	public static final int MACHINE_ERROR = Block.COLOR_RED;
	public static final int MACHINE_INFO = Block.COLOR_BLUE;
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

	private boolean getIsMouseOverRect(
			int mouseX, int mouseY, int x, int y, int width, int height) {

		int widthScaled = (this.width - xSize) / 2;
		int heightScaled = (this.height - ySize) / 2;

		mouseX -= widthScaled;
		mouseY -= heightScaled;

		return mouseX >= x
				&& mouseX < x + width
				&& mouseY >= y
				&& mouseY < y + height;
	}

	private boolean getIsMouseOverTank(CJTank tank, int x, int y) {
		return getIsMouseOverRect(
				x + FLUID_OFFSET_X, y + FLUID_OFFSET_Y,
				tank.xDisplayPosition, tank.yDisplayPosition,
				FLUID_WIDTH - FLUID_OFFSET_X, FLUID_HEIGHT - FLUID_OFFSET_Y);
	}

	// TODO: This floats tooltips what appears to be in absolute rather than
	//		 Relative space.
	private void drawTooltip(
			String name, String description, int x, int y, int titleColor) {

		int nameWidth = fontRenderer.getStringWidth(name);

		int descriptionWidth = 0;
		if(description != null) {
			descriptionWidth = fontRenderer.getStringWidth(description);
		}

		int textWidth = Math.max(nameWidth, descriptionWidth);

		// TODO: Loads of magic numbers down here.
		int xSlot = x + 12;
		int ySlot = y - 12;

		// TODO: Smaller when no description.
		this.drawGradientRect(
				xSlot - 3, ySlot - 3,
				xSlot + textWidth + 3, ySlot + 26,
				TOOLTIP_BACKGROUND, TOOLTIP_BACKGROUND);

		if(description != null) {
			fontRenderer.drawStringWithShadow(
					description, xSlot, ySlot + 15, Color.GRAY.getRGB());
		}

		fontRenderer.drawStringWithShadow(name, xSlot, ySlot, titleColor);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float deltaTicks) {
		super.drawScreen(mouseX, mouseY, deltaTicks);

		StringTranslate translate = StringTranslate.getInstance();

		CJContainerMachineBase machine =
				(CJContainerMachineBase) inventorySlots;

		for(int i = 0; i < machine.tanks.size(); i++) {
			CJTank tank = machine.tanks.get(i);
			CJTankVolume tankVolume = machineEntity.tanks.get(i);

			if(getIsMouseOverTank(tank, mouseX, mouseY)) {
				String name = translate.translateKey("message.cj_empty_fluid");

				if(tankVolume.fluidID != 0) {
					Block fluid = Block.blocksList[tankVolume.fluidID];
					name = fluid.translateBlockName();
				}

				drawTooltip(
						name,
						tankVolume.current + "/" + tankVolume.max + "mB",
						mouseX, mouseY, -1);

				break;
			}
		}

		if(getIsMouseOverRect(
				mouseX, mouseY,
				STATUS_X, STATUS_Y,
				STATUS_WIDTH, STATUS_HEIGHT)) {

			String title = "message.cj_stopped";
			int color = MACHINE_ERROR;
			String name = machineEntity.errorMessage;

			if(machineEntity.errorMessage == null) {
				title = "message.cj_working";
				color = MACHINE_OK;
				name = "message.cj_ok";
			}
			else if(machineEntity.isWarning) {
				title = "message.cj_warning";
				color = MACHINE_WARNING;
			}

			drawTooltip(
					translate.translateKey(title),
					translate.translateKey(name), mouseX, mouseY,
					color);
		}

		if(getIsMouseOverRect(
				mouseX, mouseY, INFO_X, INFO_Y, STATUS_WIDTH, STATUS_HEIGHT)) {

			drawTooltip(
					translate.translateKey(
							"message." + machineBuilder.name + ".help"),
					translate.translateKey(
							"message." + machineBuilder.name + ".flavor"),
					mouseX, mouseY,
					MACHINE_INFO);
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

		// "Inventory" label.
		fontRenderer.drawString(
				inventory, INVENTORY_LABEL_X, INVENTORY_LABEL_Y, MACHINE_TEXT);
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

		// Draw coordinates.
		CJGuiCoordinate coordinate;
		int coordinateX;
		int coordinateY;
		for(int i = 0; i < machineBuilder.linkCoordinates.size(); i++) {
			coordinate = machineBuilder.linkCoordinates.get(i);
			coordinateX = baseX + coordinate.xDisplayPosition;
			coordinateY = baseY + coordinate.yDisplayPosition;

			CJGuiCoordinateDisplay coordinateDisplay =
					machineEntity.coordinateDisplays.get(i);

			String string;
			if(coordinateDisplay != null) {
				string = StringTranslate.getInstance().translateKeyFormat(
						coordinate.label,
						coordinateDisplay.value[0],
						coordinateDisplay.value[1],
						coordinateDisplay.value[2]);
			}
			else {
				string = StringTranslate.getInstance().translateKey(
						"message.cj_coordinate_unset");
			}

			drawCenteredString(
					fontRenderer, string, coordinateX, coordinateY, 0);
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

		// Draw status badge.
		String message = machineEntity.errorMessage;
		if(message == null) {
			drawTexturedModalRect(
					STATUS_X + baseX, STATUS_Y + baseY,
					STATUS_OK_X, STATUS_OK_Y,
					STATUS_WIDTH, STATUS_HEIGHT);
		}
		else {
			if(machineEntity.isWarning) {
				drawTexturedModalRect(
						STATUS_X + baseX, STATUS_Y + baseY,
						STATUS_WARNING_X, STATUS_WARNING_Y,
						STATUS_WIDTH, STATUS_HEIGHT);
			}
			else {
				drawTexturedModalRect(
						STATUS_X + baseX, STATUS_Y + baseY,
						STATUS_ERROR_X, STATUS_ERROR_Y,
						STATUS_WIDTH, STATUS_HEIGHT);
			}
		}

		drawTexturedModalRect(
				INFO_X + baseX, INFO_Y + baseY,
				STATUS_INFO_X, STATUS_INFO_Y,
				STATUS_WIDTH, STATUS_HEIGHT);
	}
}
