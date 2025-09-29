package io.github.chlorojoules.gui;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.container.CJContainerGuide;
import io.github.chlorojoules.machine.CJMachineBuilder;
import io.github.chlorojoules.machine.CJMachineSlotInfo;
import net.minecraft.client.gui.GuiContainer;

public class CJGuiGuide extends GuiContainer<CJContainerGuide> {
	public CJGuiGuide() {
		super(new CJContainerGuide());
	}

	@Override
	public void drawScreen(float mouseXf, float mouseYf, float deltaTicks) {
		super.drawScreen(mouseXf, mouseYf, deltaTicks);

		/*int mouseX = (int) mouseXf;
		int mouseY = (int) mouseYf;

		StringTranslate translate = StringTranslate.getInstance();

		CJContainerGuideBook machine = inventorySlots;

		for(int i = 0; i < machine.tanks.size(); i++) {
			CJTank tank = machine.tanks.get(i);
			CJTankVolume tankVolume = volume;

			if(CJMachineBuilder.getIsMouseOverTank(
					this, tank, mouseX, mouseY)) {

				String name = translate.translateKey("message.cj_empty_fluid");

				if(tankVolume.fluidID != 0) {
					Block fluid = Blocks.BLOCKS_LIST[tankVolume.fluidID];
					name = fluid.translateBlockName();
				}

				CJMachineBuilder.drawTooltip(
						this, name,
						tankVolume.current + "/" + tankVolume.max + "mB",
						mouseX, mouseY, -1);

				break;
			}
		}*/
	}

	//@Override
	//protected void drawGuiContainerForegroundLayer() {}

	@Override
	protected void drawGuiContainerBackgroundLayer(float deltaTicks) {
		CJMachineBuilder machineBuilder =
				CJMod.machines.get("pulverizer").machineBuilder;

		int baseX = (width - xSize) / 2;
		int baseY = (height - ySize) / 2;

		machineBuilder.drawGui(this, baseX, baseY, xSize, ySize);

		for(CJMachineSlotInfo slot : machineBuilder.slots) {
			CJMachineBuilder.drawSlot(this, baseX, baseY, slot);
		}

		for(int i = 0; i < machineBuilder.tanks.size(); i++) {
			CJTank tank = machineBuilder.tanks.get(i);
			CJTankVolume volume = new CJTankVolume();

			CJMachineBuilder.drawTankWithVolume(
					this, baseX, baseY, tank, volume, zLevel);
		}

		machineBuilder.drawProgressBar(this, baseX, baseY, 0, 100);
	}
}
