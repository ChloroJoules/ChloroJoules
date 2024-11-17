package io.github.chlorojoules.client;

import net.minecraft.src.client.gui.GuiContainer;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

public class CJGuiMachineBase extends GuiContainer {
	public static final int MACHINE_TEXT_COLOR = 4210752;

	private CJTileEntityMachineBase machine;

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
		// TODO: Draw background texture -- procedural GUI texture generation?
		int texture = mc.renderEngine.getTexture("/gui/furnace.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);

		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;

		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
}
