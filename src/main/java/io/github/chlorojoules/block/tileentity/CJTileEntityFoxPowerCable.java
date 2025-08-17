package io.github.chlorojoules.block.tileentity;

import com.fox2code.foxloader.energy.FoxPowerCableBlock;
import com.fox2code.foxloader.energy.FoxPowerCableTileEntity;
import io.github.chlorojoules.CJMod;

public class CJTileEntityFoxPowerCable extends FoxPowerCableTileEntity {
	public CJTileEntityFoxPowerCable(FoxPowerCableBlock cableBlock) {
		super(cableBlock);
	}

	public CJTileEntityFoxPowerCable() {
		super(CJMod.foxPowerCable);
	}
}
