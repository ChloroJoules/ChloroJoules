package io.github.chlorojoules.chlorojoules;

import com.fox2code.foxloader.config.ConfigEntry;
import com.fox2code.foxloader.loader.Mod;
import com.fox2code.foxloader.registry.*;

import java.awt.*;

public class CJInstance extends Mod {
    public static final CJConfig CONFIG = new CJConfig();

    public static RegisteredBlock bugBlock;

    @Override
    public void onPreInit() {
        setConfigObject(CONFIG);

        bugBlock = registerNewBlock("cj_bugblock", new BlockBuilder()
                .setBlockName("cj_bugblock")
                .setTooltipColor(Color.GRAY)
                .setEffectiveTool(RegisteredToolType.PICKAXE));

        RegisteredItem coal = GameRegistry.getInstance().getRegisteredItem(263);
        RegisteredItem wool = GameRegistry.getInstance().getRegisteredItem(35);
        RegisteredItemStack grayWool = wool.newRegisteredItemStack();
        grayWool.setRegisteredDamage(1); // This is for light gray wool.
        registerShapelessRecipe(bugBlock.newRegisteredItemStack(), grayWool, coal);
    }

    public static class CJConfig {
        @ConfigEntry(configName = "Tomfoolery?")
        public boolean tomfoolery = true;
    }
}
