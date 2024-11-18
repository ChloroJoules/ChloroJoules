package io.github.chlorojoules.client;

import io.github.chlorojoules.CJInstance;

import net.minecraft.src.game.block.Block;

import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.registry.*;
import net.minecraft.src.game.item.Item;

import java.awt.*;

public class CJClient extends CJInstance implements ClientMod {
	public static final int PRIMAL_COLOR = Color.GRAY.getRGB();
	public static final int MANUFACTURED_COLOR = 0;
	public static final int REFINED_COLOR = -100999924;
	public static final int AWAKENED_COLOR = Block.COLOR_CYAN;

	public static RegisteredBlock bugBlock;

	public static RegisteredItem paste;

	public static RegisteredItem primalJewel;
	public static RegisteredItem manufacturedJewel;
	public static RegisteredItem refinedJewel;
	public static RegisteredItem awakenedJewel;

	@Override
	public void onInit() {
		bugBlock = registerNewBlock("cj_bugblock", new BlockBuilder()
				.setBlockName("cj_bugblock")
				.setGameBlockProvider(
						((id, build, ext) -> new CJBlockMachineBase(id) {})));

		paste = registerNewItem("cj_paste", new ItemBuilder()
				.setItemName("cj_paste")
				.setTooltipColor(PRIMAL_COLOR));

		primalJewel = registerNewItem(
				"cj_jewel_primal", new ItemBuilder()
						.setItemName("cj_jewel_primal")
						.setTooltipColor(PRIMAL_COLOR));

		manufacturedJewel = registerNewItem(
				"cj_jewel_manufactured", new ItemBuilder()
						.setItemName("cj_jewel_manufactured")
						.setTooltipColor(MANUFACTURED_COLOR));

		refinedJewel = registerNewItem(
				"cj_jewel_refined", new ItemBuilder()
						.setItemName("cj_jewel_refined")
						.setTooltipColor(REFINED_COLOR));

		awakenedJewel = registerNewItem(
				"cj_jewel_awakened", new ItemBuilder()
						.setItemName("cj_jewel_awakened")
						.setTooltipColor(AWAKENED_COLOR));

		RegisteredItemStack pasteStack = paste.newRegisteredItemStack();
		RegisteredItemStack primalJewelStack =
				primalJewel.newRegisteredItemStack();

		RegisteredItemStack diamondStack = Item.diamond.newRegisteredItemStack();

		registerFurnaceRecipe(Block.leaves.asRegisteredItem(), pasteStack);

		registerRecipe(
				primalJewelStack,
				" # ",
				"#@#",
				" # ",
				'#', pasteStack,
				'@', diamondStack);
	}
}
