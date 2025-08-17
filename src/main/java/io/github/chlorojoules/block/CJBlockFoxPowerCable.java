package io.github.chlorojoules.block;

import com.fox2code.foxloader.energy.FoxPowerCableBlock;
import com.fox2code.foxloader.energy.FoxPowerCableTileEntity;
import io.github.chlorojoules.block.tileentity.CJTileEntityFoxPowerCable;
import net.minecraft.common.block.data.Materials;

public class CJBlockFoxPowerCable extends FoxPowerCableBlock {
	public CJBlockFoxPowerCable(String name) {
		super(name, Materials.ROCK);
	}

	@Override
	public FoxPowerCableTileEntity getBlockEntity() {
		return new CJTileEntityFoxPowerCable(this);
	}

	@Override
	public long getCableThroughput() {
		return 10;
	}

	@Override
	public long getCableStorage() {
		return 10;
	}
}
