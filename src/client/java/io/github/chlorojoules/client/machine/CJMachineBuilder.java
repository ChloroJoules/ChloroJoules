package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.CJRarity;
import io.github.chlorojoules.client.CJTank;
import io.github.chlorojoules.client.CJTankVolume;
import io.github.chlorojoules.client.gui.CJGuiElement;
import io.github.chlorojoules.client.gui.CJGuiGravity;
import io.github.chlorojoules.client.gui.CJGuiGravityInfo;

import java.util.ArrayList;

import static io.github.chlorojoules.client.gui.CJGuiGravity.*;

public class CJMachineBuilder {
	public String name = null;
	public CJRarity rarity = CJRarity.MANUFACTURED;
	public CJIMachine machineImpl = null;

	public ArrayList<CJMachineSlotInfo> slots = new ArrayList<>();
	public ArrayList<CJTank> tanks = new ArrayList<>();
	public ArrayList<CJTankVolume> tankVolumes = new ArrayList<>();
	public ArrayList<CJGuiElement> progressBars = new ArrayList<>();

	public CJMachineBuilder setMachineName(String value) {
		name = value;
		return this;
	}

	public CJMachineBuilder setRarity(CJRarity value) {
		rarity = value;
		return this;
	}

	public CJMachineBuilder setImpl(CJIMachine value) {
		machineImpl = value;
		return this;
	}

	// TODO: Mechanism for anchoring/centering slots/tanks in builder
	//       Interface.
	// TODO: Helper for ChloroJewel slots.
	public CJMachineBuilder addSlot(int x, int y, boolean output) {
		slots.add(new CJMachineSlotInfo(x, y, output));

		return this;
	}

	public CJMachineBuilder addSlotGravity(
			CJGuiGravity anchor, int x, int y, boolean output) {

		return addSlot(
				CJGuiGravityInfo.getSlotAnchoredX(anchor, x, output),
				CJGuiGravityInfo.getSlotAnchoredY(anchor, y, output),
				output);
	}

	public CJMachineBuilder addSlotGravityVCenter(
			CJGuiGravity anchor, int x, boolean output) {

		return addSlot(
				CJGuiGravityInfo.getSlotAnchoredX(anchor, x, output),
				CJGuiGravityInfo.getSlotAnchoredY(CENTER, 0, output),
				output);
	}

	// TODO: Add helper for adding specifically fuel tanks.
	// NOTE: Pass the block ID of the flowing variant of the fluid you want
	//       To set as locked to `lockFluid`, otherwise `0`.
	public CJMachineBuilder addTank(
			int x, int y, boolean output, int max, int lockFluid) {

		CJTank tank = new CJTank(x, y);
		tank.output = output;
		tanks.add(tank);

		CJTankVolume tankVolume = new CJTankVolume();
		tankVolume.max = max;
		tankVolume.fluidID = lockFluid;
		tankVolume.lockFluid = (lockFluid != 0);
		tankVolumes.add(tankVolume);

		return this;
	}

	public CJMachineBuilder addTankGravity(
			CJGuiGravity anchor, int x, int y, boolean output, int max,
			int lockFluid) {

		return addTank(
				CJGuiGravityInfo.getTankAnchoredX(anchor, x),
				CJGuiGravityInfo.getTankAnchoredY(anchor, y),
				output, max, lockFluid);
	}

	public CJMachineBuilder addTankGravityVCenter(
			CJGuiGravity anchor, int x, boolean output, int max,
			int lockFluid) {

		return addTank(
				CJGuiGravityInfo.getTankAnchoredX(anchor, x),
				CJGuiGravityInfo.getTankAnchoredY(CENTER, 0),
				output, max, lockFluid);
	}

	public CJMachineBuilder addProgressBar(int x, int y) {
		progressBars.add(new CJGuiElement(x, y));

		return this;
	}

	public CJMachineBuilder addProgressBarGravity(
			CJGuiGravity anchor, int x, int y) {

		return addProgressBar(
				CJGuiGravityInfo.getProgressBarAnchoredX(anchor, x),
				CJGuiGravityInfo.getProgressBarAnchoredY(anchor, y));
	}

	public CJMachineBuilder addProgressBarGravityVCenter(
			CJGuiGravity anchor, int x) {

		return addProgressBar(
				CJGuiGravityInfo.getProgressBarAnchoredX(anchor, x),
				CJGuiGravityInfo.getProgressBarAnchoredY(CENTER, 0));
	}
}
