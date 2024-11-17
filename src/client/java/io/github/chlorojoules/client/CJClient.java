package io.github.chlorojoules.client;

import io.github.chlorojoules.CJInstance;

import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.registry.BlockBuilder;

public class CJClient extends CJInstance implements ClientMod {
	@Override
	public void onInit() {
		BlockBuilder builder;

		builder = new BlockBuilder()
				.setBlockName("cj_bugblock")
				.setGameBlockProvider(
						((id, build, ext) -> new CJBlockMachineBase(id) {}));

		bugBlock = registerNewBlock("cj_bugblock", builder);
	}
}
