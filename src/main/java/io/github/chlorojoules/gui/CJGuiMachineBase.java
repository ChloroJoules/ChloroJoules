package io.github.chlorojoules.gui;

import com.indigo3d.util.RenderSystem;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.container.CJContainerMachineBase;
import io.github.chlorojoules.machine.CJMachineBuilder;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.machine.CJMachineSlotInfo;
import net.minecraft.client.gui.GuiContainer;
import net.minecraft.client.renderer.world.RenderHelper;
import net.minecraft.client.renderer.world.Tessellator;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.icon.Icon;
import net.minecraft.common.block.texture.Face;
import net.minecraft.common.entity.player.InventoryPlayer;

import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJGuiMachineBase extends GuiContainer<CJContainerMachineBase> {
	public static final int MACHINE_TEXT = Color.DARK_GRAY.getRGB();
	public static final int MACHINE_OK = Color.GREEN.getRGB();
	public static final int MACHINE_PAUSED = Color.DARK_GRAY.getRGB();
	public static final int MACHINE_WARNING = Color.ORANGE.getRGB();
	public static final int MACHINE_ERROR = Color.RED.getRGB();
	public static final int MACHINE_INFO = Color.BLUE.getRGB();
	public static final int TOOLTIP_BACKGROUND = -1073741824;

	private final CJTileEntityMachineBase machineEntity;

	private boolean wasMousePressed = false;

	public CJGuiMachineBase(
			InventoryPlayer inventoryPlayer, CJTileEntityMachineBase entity) {

		super(new CJContainerMachineBase(inventoryPlayer, entity));

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
				tank.getXPlacement(), tank.getYPlacement(),
				FLUID_WIDTH - FLUID_OFFSET_X, FLUID_HEIGHT - FLUID_OFFSET_Y);
	}

	private void drawTooltip(
			String name, String description, int x, int y, int titleColor) {

		int nameWidth = fontRenderer.getStringWidth(name);
		int boxHeight = TOOLTIP_HEIGHT;

		int descriptionWidth = 0;
		if(description != null) {
			descriptionWidth = fontRenderer.getStringWidth(description);
			boxHeight += TOOLTIP_DESCRIPTION_HEIGHT;
		}

		int textWidth = Math.max(nameWidth, descriptionWidth);

		int xSlot = x + TOOLTIP_OFFSET_X;
		int ySlot = y - TOOLTIP_OFFSET_Y;

		RenderSystem.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();

		this.drawGradientRect(
				xSlot - TOOLTIP_BORDER, ySlot - TOOLTIP_BORDER,
				xSlot + textWidth + TOOLTIP_BORDER, ySlot + boxHeight,
				TOOLTIP_BACKGROUND, TOOLTIP_BACKGROUND);

		if(description != null) {
			fontRenderer.drawStringWithShadow(
					description, xSlot, ySlot + TOOLTIP_DESCRIPTION_OFFSET,
					Color.GRAY.getRGB());
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

		CJMachineBuilder machineBuilder = machineEntity.getBuilder();
		for(int i = 0; i < machineBuilder.buttons.size(); ++i) {
			CJGuiButton button = machineBuilder.buttons.get(i);
			if(button.checkDamageExclusive(machineEntity)) continue;

			if(getIsMouseOverRect(
					mouseX, mouseY,
					button.getXPlacement(), button.getYPlacement(),
					BUTTON_WIDTH, BUTTON_HEIGHT)) {

				String tooltip = button.tooltip;
				boolean state = machineEntity.buttonStates.get(i);

				if(button.filter) {
					tooltip =
							state ? "message.cj_blacklist" :
									"message.cj_whitelist";
				}

				drawTooltip(
						translate.translateKey(tooltip),
						null,
						mouseX, mouseY,
						Color.WHITE.getRGB());

				if(Mouse.isButtonDown(0)) {
					if(!wasMousePressed) {
						this.mc.sndManager.playSoundFX(
								"random.click", 1.0F, 1.0F);

						machineEntity.operationTicks = 0;
						machineEntity.buttonStates.set(i, !state);
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
				if(machineEntity.isPaused) {
					title = "message.cj_paused";
					color = MACHINE_PAUSED;
					name = "message.cj_paused_by_gear";
				}
				else {
					title = "message.cj_working";
					color = MACHINE_OK;
					name = "message.cj_ok";
				}
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
		CJContainerMachineBase machine = inventorySlots;
		CJMachineBuilder machineBuilder = machineEntity.getBuilder();

		int baseX = (width - xSize) / 2;
		int baseY = (height - ySize) / 2;

		machineBuilder.drawGui(this, baseX, baseY, xSize, ySize);

		for(CJMachineSlotInfo slot : machineBuilder.slots) {
			if(slot.checkDamageExclusive(machineEntity)) continue;

			machineBuilder.drawSlot(this, baseX, baseY, slot);
		}

		for(int i = 0; i < machineEntity.tanks.size(); i++) {
			CJTank tank = machineBuilder.tanks.get(i);
			if(tank.checkDamageExclusive(machineEntity)) continue;

			CJTankVolume volume = machineEntity.tanks.get(i);
			machineBuilder.drawTankWithVolume(
					this, baseX, baseY, tank, volume, zLevel);
		}

		machineBuilder.drawProgressBar(
				this, baseX, baseY, machineEntity.operationTicks,
				machineEntity.operationLength);

		// Draw player inventory.
		CJGuiMachineBaseSlot slot;
		int slotX;
		int slotY;
		for(int i = 0; i < machine.slots.size(); i++) {
			slot = (CJGuiMachineBaseSlot) machine.slots.get(i);
			if(!(slot.inventory instanceof InventoryPlayer)) continue;

			// Placement is pre-applied to slots during container population.
			slotX = baseX + slot.xDisplayPosition;
			slotY = baseY + slot.yDisplayPosition;

			drawTexturedModalRect(
					slotX - SLOT_IN_OFFSET_X, slotY - SLOT_IN_OFFSET_Y,
					SLOT_IN_X, SLOT_IN_Y,
					SLOT_IN_WIDTH, SLOT_IN_HEIGHT);
		}

		// Draw buttons.
		for(int i = 0; i < machineBuilder.buttons.size(); ++i) {
			CJGuiButton button = machineBuilder.buttons.get(i);
			if(button.checkDamageExclusive(machineEntity)) continue;

			int x = baseX + button.getXPlacement();
			int y = baseY + button.getYPlacement();

			boolean state = machineEntity.buttonStates.get(i);
			int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

			if(button.filter) {
				int srcX = state ? BUTTON_BLACKLIST_X : BUTTON_WHITELIST_X;
				int srcY = state ? BUTTON_BLACKLIST_Y : BUTTON_WHITELIST_Y;

				drawTexturedModalRect(
						x, y,
						srcX, srcY,
						BUTTON_WIDTH, BUTTON_HEIGHT);
			}
			else {
				int srcX = state ? BUTTON_ACTIVE_X : BUTTON_INACTIVE_X;
				int srcY = state ? BUTTON_ACTIVE_Y : BUTTON_INACTIVE_Y;

				drawTexturedModalRect(
						x, y,
						srcX, srcY,
						BUTTON_WIDTH, BUTTON_HEIGHT);

				x += BUTTON_LABEL_INSET;
				y += BUTTON_LABEL_INSET;

				itemRenderer.renderItemIntoGUI(
						this.fontRenderer, mc.renderEngine, button.label,
						x, y);
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(texture);
			RenderSystem.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			RenderSystem.disableLighting();
			RenderSystem.disableDepthTest();
		}

		// Draw rift view.
		if(machineBuilder.hasRiftView) {
			int riftX = baseX + (WORKING_WIDTH / 2) - (RIFT_VIEW_WIDTH / 2);
			int riftY = baseY + 16;

			drawTexturedModalRect(
					riftX, riftY, RIFT_VIEW_X, RIFT_VIEW_Y,
					RIFT_VIEW_WIDTH, RIFT_VIEW_HEIGHT);

			GL11.glLineWidth(4.0f);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glBegin(GL11.GL_LINES);

			float dt = mc.ticksRan / 4.0f;

			for(int i = 0; i < machineEntity.riftDensity; ++i) {
				float dti = dt / i + i * 16;
				float sdt = (float) Math.sin(dti) * 0.5f + 0.5f;
				float cdt = (float) Math.cos(dti) * 0.5f + 0.5f;
				float n_sdt = (float) Math.sin(-dti) * 0.5f + 0.5f;
				float n_cdt = (float) Math.cos(-dti) * 0.5f + 0.5f;

				GL11.glColor3f(cdt, sdt, 1.0f);

				GL11.glVertex2i(
						riftX + (int) (RIFT_VIEW_WIDTH * sdt),
						riftY + (int) (RIFT_VIEW_WIDTH * cdt));

				GL11.glVertex2i(
						riftX + (int) (RIFT_VIEW_WIDTH * n_sdt),
						riftY + (int) (RIFT_VIEW_WIDTH * n_cdt));
			}

			GL11.glEnd();
			GL11.glColor3f(1.0f, 1.0f, 1.0f);

			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		// Draw status badge.
		String message = machineEntity.errorMessage;
		if(message == null) {
			if(machineEntity.isPaused) {
				drawTexturedModalRect(
						STATUS_X + baseX, STATUS_Y + baseY,
						STATUS_PAUSED_X, STATUS_PAUSED_Y,
						STATUS_WIDTH, STATUS_HEIGHT);
			}
			else drawTexturedModalRect(
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

		// Draw coordinates.
		for(int i = 0; i < machineBuilder.linkCoordinates.size(); i++) {
			CJGuiCoordinate coordinate = machineBuilder.linkCoordinates.get(i);
			if(coordinate.checkDamageExclusive(machineEntity)) continue;

			int x = baseX + coordinate.getXPlacement();
			int y = baseY + coordinate.getYPlacement();

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
	}
}
