package io.github.chlorojoules.machine;

import com.fox2code.foxloader.energy.FoxPowerInterfaceSimple;
import com.fox2code.foxloader.energy.FoxPowerType;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import org.jetbrains.annotations.NotNull;

public class CJMachineFoxPowerInterface extends FoxPowerInterfaceSimple {
	private final CJTileEntityMachineBase machineEntity;

	public CJMachineFoxPowerInterface(CJTileEntityMachineBase machineEntity) {
		this.machineEntity = machineEntity;
	}

	@Override
	public long getMaxFoxPowerStorage() {
		CJMachineBuilder machineBuilder = machineEntity.getBuilder();

		if(machineBuilder.hasNamedTank("fuel")) {
			return machineBuilder.getNamedTankVolume(
					machineEntity, "fuel").max;
		}

		return 0;
	}

	@Override
	public long getStoredFoxPower() {
		CJMachineBuilder machineBuilder = machineEntity.getBuilder();

		if(machineBuilder.hasNamedTank("fuel")) {
			return machineBuilder.getNamedTankVolume(
					machineEntity, "fuel").current;
		}

		return 0;
	}

	@Override
	public void setStoredFoxPower(long foxPower) {
		CJMachineBuilder machineBuilder = machineEntity.getBuilder();

		if(machineBuilder.hasNamedTank("fuel")) {
			machineBuilder.getNamedTankVolume(
					machineEntity, "fuel").current = (int) foxPower;
		}
	}

	@Override
	public void emitFoxPowerSinkUpdated() {}

	@Override
	public @NotNull FoxPowerType getFoxPowerType() {
		return machineEntity.getBuilder().powerType;
	}
}
