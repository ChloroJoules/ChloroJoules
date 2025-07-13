package io.github.chlorojoules.gui;

import com.indigo3d.util.RenderSystem;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.container.CJContainerMachineBase;
import io.github.chlorojoules.machine.CJMachineBuilder;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.client.gui.GuiContainer;
import net.minecraft.client.renderer.world.RenderHelper;
import net.minecraft.client.renderer.world.Tessellator;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.icon.Icon;
import net.minecraft.common.block.texture.Face;
import net.minecraft.common.entity.player.InventoryPlayer;

import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJGuiMachineBase extends GuiContainer<CJContainerMachineBase> {
	public static final int MACHINE_TEXT = Color.DARK_GRAY.getRGB();
	public static final int MACHINE_OK = Color.GREEN.getRGB();
	public static final int MACHINE_WARNING = Color.ORANGE.getRGB();
	public static final int MACHINE_ERROR = Color.RED.getRGB();
	public static final int MACHINE_INFO = Color.BLUE.getRGB();
	public static final int TOOLTIP_BACKGROUND = -1073741824;

	private final CJTileEntityMachineBase machineEntity;
	private final CJMachineBuilder machineBuilder;

	private boolean wasMousePressed = false;

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

	private void drawTooltip(
			String name, String description, int x, int y, int titleColor) {

		// TODO: Loads of magic numbers in here.
		int nameWidth = fontRenderer.getStringWidth(name);
		int boxHeight = 12;

		int descriptionWidth = 0;
		if(description != null) {
			descriptionWidth = fontRenderer.getStringWidth(description);
			boxHeight += 14;
		}

		int textWidth = Math.max(nameWidth, descriptionWidth);

		int xSlot = x + 12;
		int ySlot = y - 12;

		RenderSystem.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();

		this.drawGradientRect(
				xSlot - 3, ySlot - 3,
				xSlot + textWidth + 3, ySlot + boxHeight,
				TOOLTIP_BACKGROUND, TOOLTIP_BACKGROUND);

		if(description != null) {
			fontRenderer.drawStringWithShadow(
					description, xSlot, ySlot + 15, Color.GRAY.getRGB());
		}

		fontRenderer.drawStringWithShadow(name, xSlot, ySlot, titleColor);

		RenderSystem.enableLighting();
		RenderSystem.enableDepthTest();
	}

	@Override
	public void drawScreen(float mouseXf, float mouseYf, float deltaTicks) {
		super.drawScreen(mouseXf, mouseYf, deltaTicks);

		int mouseX = (int) mouseXf;
		int mouseY = (int) mouseYf;

		StringTranslate translate = StringTranslate.getInstance();

		CJContainerMachineBase machine = inventorySlots;

		for(int i = 0; i < machine.tanks.size(); i++) {
			CJTank tank = machine.tanks.get(i);
			CJTankVolume tankVolume = machineEntity.tanks.get(i);

			if(getIsMouseOverTank(tank, mouseX, mouseY)) {
				String name = translate.translateKey("message.cj_empty_fluid");

				if(tankVolume.fluidID != 0) {
					Block fluid = Blocks.BLOCKS_LIST[tankVolume.fluidID];
					name = fluid.translateBlockName();
				}

				drawTooltip(
						name,
						tankVolume.current + "/" + tankVolume.max + "mB",
						mouseX, mouseY, -1);

				break;
			}
		}

		for(int i = 0; i < machineBuilder.buttons.size(); ++i) {
			CJGuiButton button = machineBuilder.buttons.get(i);

			if(getIsMouseOverRect(
					mouseX, mouseY,
					button.xDisplayPosition, button.yDisplayPosition,
					BUTTON_WIDTH, BUTTON_HEIGHT)) {

				drawTooltip(
						translate.translateKey(button.tooltip),
						null,
						mouseX, mouseY,
						Color.WHITE.getRGB());

				if(Mouse.isButtonDown(0)) {
					if(!wasMousePressed) {
						this.mc.sndManager.playSoundFX(
								"random.click", 1.0F, 1.0F);

						machineEntity.buttonStates.set(
								i, !machineEntity.buttonStates.get(i));
					}

					wasMousePressed = true;
				}
				else wasMousePressed = false;
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

	public void renderIconClipped(
			int x, int y, Icon icon, int width, int height) {

		Tessellator t = Tessellator.instance;
		Tessellator.instance.startDrawingQuads();

		float w = icon.getMaxU() - icon.getMinU();
		float h = icon.getMaxV() - icon.getMinV();
		float maxU = icon.getMinU() + (w * (width / 16F));
		float maxV = icon.getMinV() + (h * (height / 16F));

		t.addVertexWithUV(
				x, y + height, this.zLevel,
				icon.getMinU(), maxV);

		t.addVertexWithUV(x + width, y + height, this.zLevel, maxU, maxV);
		t.addVertexWithUV(x + width, y, this.zLevel, maxU, icon.getMinV());

		t.addVertexWithUV(
				x, y, this.zLevel,
				icon.getMinU(), icon.getMinV());

		t.draw();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float deltaTicks) {
		CJContainerMachineBase machine = inventorySlots;

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
				int inX = SLOT_IN_X;
				int inY = SLOT_IN_Y;

				if(slot.isGem() && machineEntity.stacks.get(i) == null) {
					inX = SLOT_JEWEL_X;
					inY = SLOT_JEWEL_Y;
				}

				drawTexturedModalRect(
						slotX - SLOT_IN_OFFSET_X, slotY - SLOT_IN_OFFSET_Y,
						inX, inY,
						SLOT_IN_WIDTH, SLOT_IN_HEIGHT);
			}
		}

		// Draw tanks.
		for(int i = 0; i < machine.tanks.size(); i++) {
			CJTank tank = machine.tanks.get(i);
			CJTankVolume tankVolume = machineEntity.tanks.get(i);
			int tankX = baseX + tank.xDisplayPosition;
			int tankY = baseY + tank.yDisplayPosition;

			drawTexturedModalRect(
					tankX, tankY,
					FLUID_FULL_X, FLUID_FULL_Y,
					FLUID_WIDTH, FLUID_HEIGHT);

			if(tankVolume.fluidID != CJMod.fuelFluid) {
				Icon icon = Blocks.BLOCKS_LIST[tankVolume.fluidID].getIcon(
						Face.TOP.direction(), 0);

				int fluidTexture = mc.renderEngine.getTexture("/terrain.png");
				mc.renderEngine.bindTexture(fluidTexture);

				int j;
				for(j = 0; j < FLUID_HEIGHT - 2; j += 16) {
					renderIconClipped(
							tankX + 1, tankY + 1 + j,
							icon,
							FLUID_WIDTH - 2,
							Math.min(16, FLUID_HEIGHT - 2 - j));
				}

				mc.renderEngine.bindTexture(texture);
			}

			// Overlay the full tank graphic with an amount of the empty one.
			int tankFill =
					(tankVolume.current * FLUID_HEIGHT) / tankVolume.max;

			int tankEmptyDrawHeight = FLUID_HEIGHT - tankFill;

			drawTexturedModalRect(
					tankX, tankY,
					FLUID_EMPTY_X, FLUID_EMPTY_Y,
					FLUID_WIDTH, tankEmptyDrawHeight);
		}

		// Draw coordinates.
		for(int i = 0; i < machineBuilder.linkCoordinates.size(); i++) {
			CJGuiCoordinate coordinate = machineBuilder.linkCoordinates.get(i);
			int x = baseX + coordinate.xDisplayPosition;
			int y = baseY + coordinate.yDisplayPosition;

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

			drawCenteredString(fontRenderer, string, x, y, 0);
		}

		// Draw progress bar.
		if(machineBuilder.progressBar != null) {
			int x = baseX + machineBuilder.progressBar.xDisplayPosition;
			int y = baseY + machineBuilder.progressBar.yDisplayPosition;

			drawTexturedModalRect(
					x, y,
					PROGRESS_EMPTY_X, PROGRESS_EMPTY_Y,
					PROGRESS_WIDTH, PROGRESS_HEIGHT);

			int width = machineEntity.operationTicks * PROGRESS_WIDTH;
			drawTexturedModalRect(
					x, y,
					PROGRESS_FULL_X, PROGRESS_FULL_Y,
					width / machineEntity.operationLength, PROGRESS_HEIGHT);
		}

		// Draw buttons.
		for(int i = 0; i < machineBuilder.buttons.size(); ++i) {
			CJGuiButton button = machineBuilder.buttons.get(i);
			int x = baseX + button.xDisplayPosition;
			int y = baseY + button.yDisplayPosition;

			boolean state = machineEntity.buttonStates.get(i);
			int srcX = state ? BUTTON_ACTIVE_X : BUTTON_INACTIVE_X;
			int srcY = state ? BUTTON_ACTIVE_Y : BUTTON_INACTIVE_Y;

			drawTexturedModalRect(
					x, y,
					srcX, srcY,
					BUTTON_WIDTH, BUTTON_HEIGHT);

			x += BUTTON_LABEL_INSET;
			y += BUTTON_LABEL_INSET;

			ItemStack stack = new ItemStack(button.label, 1);
			itemRenderer.renderItemIntoGUI(
					this.fontRenderer, this.mc.renderEngine, stack, x, y);

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(texture);
			RenderSystem.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			RenderSystem.disableLighting();
			RenderSystem.disableDepthTest();
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
