package io.github.chlorojoules;

import com.fox2code.foxloader.config.ConfigEntry;
import com.fox2code.foxloader.loader.Mod;
import com.fox2code.foxloader.registry.*;

public class CJInstance extends Mod {
	public static final CJConfig CONFIG = new CJConfig();

	public static RegisteredBlock bugBlock;

	@Override
	public void onPreInit() {
		setConfigObject(CONFIG);
	}

	public static class CJConfig {
		@ConfigEntry(configName = "Shenanigans?")
		public boolean shenanigans = true;
	}
}
