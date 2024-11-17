package io.github.chlorojoules.client;

import net.minecraft.src.client.gui.GuiContainer;
import net.minecraft.src.client.gui.Slot;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

public class CJGuiMachineBase extends GuiContainer {
	public static final int MACHINE_TEXT_COLOR = 4210752;

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

	private final CJTileEntityMachineBase machine;

	public CJGuiMachineBase(
			InventoryPlayer inventoryPlayer, CJTileEntityMachineBase machine) {

		super(new CJContainerMachineBase(inventoryPlayer, machine));

		this.machine = machine;
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		StringTranslate translate = StringTranslate.getInstance();

		String name = translate.translateKey(machine.getInvName());
		String inventory = translate.translateKey("inventory.generic");

		fontRenderer.drawString(
				name,
				/* X, Y */
				xSize / 2 - fontRenderer.getStringWidth(name) / 2, 6,
				MACHINE_TEXT_COLOR);

		fontRenderer.drawString(
				inventory,
				/* X, Y */
				8, ySize - 96 + 2,
				MACHINE_TEXT_COLOR);
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

		// TODO: Fluid tank/slots interface.
	}
}
