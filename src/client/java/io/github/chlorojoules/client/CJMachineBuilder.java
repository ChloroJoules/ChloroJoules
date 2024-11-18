package io.github.chlorojoules.client;

import net.minecraft.src.client.gui.Slot;

import java.util.ArrayList;

class CJMachineSlotInfo {
	public int x;
	public int y;
	public boolean output;

	public CJMachineSlotInfo(int x, int y, boolean output) {
		this.x = x;
		this.y = y;
		this.output = output;
	}
}

public class CJMachineBuilder {
	public String name = null;
	public CJRarity rarity = CJRarity.MANUFACTURED;
	public CJIMachine machineImpl = null;

	public ArrayList<CJMachineSlotInfo> slots = new ArrayList<>();
	public ArrayList<CJTank> tanks = new ArrayList<>();
	public ArrayList<CJTankVolume> tankVolumes = new ArrayList<>();

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
	public CJMachineBuilder addSlot(int x, int y, boolean output) {
		slots.add(new CJMachineSlotInfo(x, y, output));
		return this;
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
}
