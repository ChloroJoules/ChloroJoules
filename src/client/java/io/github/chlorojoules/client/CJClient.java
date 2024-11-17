package io.github.chlorojoules.client;

import io.github.chlorojoules.CJInstance;

import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.registry.BlockBuilder;

public class CJClient extends CJInstance implements ClientMod {
	@Override
	public void onInit() {
		bugBlock = registerNewBlock("cj_bugblock", new BlockBuilder()
				.setBlockName("cj_bugblock")
				.setGameBlockProvider(
						((id, builder, ext) -> new BlockMachineBase(id) {})));
	}
}
