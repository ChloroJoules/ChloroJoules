package io.github.chlorojoules.client;

import io.github.chlorojoules.CJInstance;

import io.github.chlorojoules.client.machine.CJMachineBugBlock;
import io.github.chlorojoules.client.machine.CJMachineLiquefier;
import net.minecraft.src.game.block.Block;

import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.registry.*;
import net.minecraft.src.game.item.Item;

import static io.github.chlorojoules.client.CJGuiMachineBaseLayout.FLUID_HEIGHT;
import static io.github.chlorojoules.client.CJGuiMachineBaseLayout.WORKING_HEIGHT;
import static io.github.chlorojoules.client.CJRarityInfo.*;

public class CJClient extends CJInstance implements ClientMod {
	// TODO: Switch this to ChloroJoules.
	public static final int fuelFluid = Block.waterMoving.blockID;

	public static RegisteredBlock bugBlock;
	public static RegisteredBlock liquefier;

	public static RegisteredItem paste;

	public static RegisteredItem primalJewel;
	public static RegisteredItem manufacturedJewel;
	public static RegisteredItem refinedJewel;
	public static RegisteredItem awakenedJewel;

	public RegisteredBlock registerNewMachine(
			String name, CJMachineBuilder builder) {

		return registerNewBlock(name, new BlockBuilder()
				.setBlockName(name)
				.setGameBlockProvider(
						((id, build, ext) ->
								new CJBlockMachineBase(id, builder) {}))
				.setTooltipColor(CJRarityInfo.getRarityColor(builder.rarity)));
	}

	@Override
	public void onInit() {
		bugBlock = registerNewMachine("cj_bugblock", new CJMachineBuilder()
				.setMachineName("cj_bugblock")
				.setRarity(CJRarity.AWAKENED)
				.addTank(15, 15, false, 4 * CJTank.BUCKET, 0)
				.addSlot(50, 35, false)
				.addSlot(75, 35, true)
				.setImpl(new CJMachineBugBlock()));

		liquefier = registerNewMachine("cj_liquefier", new CJMachineBuilder()
				.setMachineName("cj_liquefier")
				.addTankGravityVCentre(
						CJGuiGravity.TOP_LEFT, 10, false, 4 * CJTank.BUCKET, fuelFluid)
				.addTankGravityVCentre(
						CJGuiGravity.TOP_RIGHT, 10, true, 8 * CJTank.BUCKET, 0)
				.addSlotCentre(false)
				.addSlotGravity(
						CJGuiGravity.BOTTOM_LEFT, 35,
						/* Align bottom of Jewel slot with input tank. */
						(WORKING_HEIGHT - FLUID_HEIGHT) / 2, false)
				.setImpl(new CJMachineLiquefier()));

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
