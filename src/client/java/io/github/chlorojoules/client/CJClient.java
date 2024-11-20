package io.github.chlorojoules.client;

import io.github.chlorojoules.CJInstance;

import io.github.chlorojoules.client.gui.CJGuiGravity;
import io.github.chlorojoules.client.machine.*;

import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.item.Item;

import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.registry.*;
import net.minecraft.src.game.item.ItemBucket;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static io.github.chlorojoules.client.gui.CJGuiMachineBaseLayout.*;
import static io.github.chlorojoules.client.CJRarityInfo.*;
import static io.github.chlorojoules.client.machine.CJMachineBuilder.*;

public class CJClient extends CJInstance implements ClientMod {
	public static List<ItemBucket> buckets = new ArrayList<>();
	public static List<Integer> bucketFluids = new ArrayList<>();

	public static RegisteredBlock fluidChlorojoules;
	public static RegisteredItem bucketFluidChlorojoules;
	public static RegisteredBlock fluidPaste;
	public static RegisteredItem bucketFluidPaste;

	public static RegisteredBlock bugBlock;
	public static RegisteredBlock liquefier;
	public static RegisteredBlock solidifier;
	public static RegisteredBlock flooper;

	public static RegisteredItem paste;

	public static RegisteredItem primalJewel;
	public static RegisteredItem manufacturedJewel;
	public static RegisteredItem refinedJewel;
	public static RegisteredItem awakenedJewel;

	public static int fuelFluid;

	public RegisteredBlock registerNewMachine(
			String name, CJMachineBuilder builder) {

		return registerNewBlock(name, new BlockBuilder()
				.setBlockName(name)
				.setGameBlockProvider(
						((id, build, ext) ->
								new CJBlockMachineBase(id, builder)))
				.setTooltipColor(CJRarityInfo.getRarityColor(builder.rarity)));
	}

	private RegisteredBlock registerNewFluid(String name) {
		return registerNewBlock(
				name, new BlockBuilder()
						.setBlockMaterial(GameRegistry.BuiltInMaterial.WATER)
						.setBlockName(name)
						.setBlockHardness(100.0F));

		// TODO: Implement `BlockFluid` wrapper/add a mixin to
		//       Expose its constructor for provider.
		//.setGameBlockProvider((id, build, ext) ->
		// new Block(id, Material.water) {}));
	}

	private RegisteredItem registerFluidBucket(
			String name, RegisteredBlock tile) {

		return registerNewItem(
				name, new ItemBuilder()
						.setItemName(name)
						.setGameItemProvider(((id, builder, ext) ->
								new ItemBucket(
										// TODO: Re-using Vanilla providers
										//       Seems to cause this offset
										//       Issue.
										id - 256, tile.getRegisteredBlockId())
										.setContainerItem(Item.bucketEmpty))));
	}

	@Override
	public void onInit() {
		// Fluids.
		{
			fluidChlorojoules = registerNewFluid("cj_fluid_chlorojoules");
			bucketFluidChlorojoules = registerFluidBucket(
					"cj_fluid_chlorojoules_bucket", fluidChlorojoules);

			fluidPaste = registerNewFluid("cj_fluid_paste");
			bucketFluidPaste = registerFluidBucket(
					"cj_fluid_paste_bucket", fluidPaste);
		}
		fuelFluid = fluidChlorojoules.getRegisteredBlockId();

		// Items.
		{
			paste = registerNewItem(
					"cj_paste", new ItemBuilder()
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
		}

		// TODO: Feels like machines could be declared in JSON or smth (so too
		//       For all our registry here -- make our lives easier?)
		bugBlock = registerNewMachine(
				"cj_bugblock", new CJMachineBuilder()
						.setMachineName("cj_bugblock")
						.setRarity(CJRarity.AWAKENED)
						.addTank(15, 15, false, 4 * CJTank.BUCKET, 0)
						.addSlot(50, 35, false)
						.addSlot(75, 35, true)
						.setImpl(new CJMachineBugBlock()));

		liquefier = registerNewMachine(
				"cj_liquefier", new CJMachineBuilder()
						.setMachineName("cj_liquefier")
						.addFuelTank()
						.addTankGravityVCenter(
								CJGuiGravity.TOP_RIGHT, 10, true,
								8 * CJTank.BUCKET, 0)
						.addSlotGravity(CJGuiGravity.CENTER, 0, 0, false)
						/* TODO: Make Jewel slot maximum stack size 1. */
						/*
						 * TODO: Make Jewel slot visually distinct and reject
						 *       Non-Jewel items.
						 */
						.addJewelSlot()
						.addProgressBarGravityVCenter(
								/*
								 * TODO: Find a better way to center progress
								 *       Bars between two elements.
								 */
								CJGuiGravity.CENTER, (SLOT_IN_WIDTH * 4) / 3)
						.addRecipe(
								10,
								new CJMachineRecipeComponent(0, Block.leaves, 1),
								new CJMachineRecipeComponent(1, fluidPaste, 50)
										.setTarget(CJMachineRecipeTarget.TANK),
								50)
						.setImpl(new CJMachineLiquefier()));

		solidifier = registerNewMachine(
				"cj_solidifier", new CJMachineBuilder()
					.setMachineName("cj_solidifier")
					.addFuelTank()
					.addTankGravityVCenter(
							CJGuiGravity.TOP_LEFT,
							JEWEL_SLOT_INSET + SLOT_OUT_WIDTH,
							false, 8 * CJTank.BUCKET, 0)
					.addSlotGravityVCenter(
							CJGuiGravity.CENTER, SLOT_IN_WIDTH * 4, true)
					.addJewelSlot()
					.addProgressBarGravityVCenter(CJGuiGravity.CENTER, 0)
					.addRecipe(
							20,
							new CJMachineRecipeComponent(1, fluidPaste, 30)
									.setTarget(CJMachineRecipeTarget.TANK),
							new CJMachineRecipeComponent(0, paste, 2),
							150)
					.setImpl(new CJMachineLiquefier()));

		// TODO: UI to allow floopers to be filtered on one fluid kind.
		flooper = registerNewMachine(
				"cj_flooper", new CJMachineBuilder()
						.setMachineName("cj_flooper")
						.addTankGravity(
								CJGuiGravity.CENTER, 0, 0, false,
								2 * CJTank.BUCKET, 0)
						.setImpl(new CJMachineFlooper()));

		RegisteredItemStack pasteStack = paste.newRegisteredItemStack();
		RegisteredItemStack primalJewelStack =
				primalJewel.newRegisteredItemStack();

		RegisteredItemStack diamondStack =
				Item.diamond.newRegisteredItemStack();

		registerFurnaceRecipe(Block.leaves.asRegisteredItem(), pasteStack);

		registerRecipe(
				primalJewelStack,
				" # ",
				"#@#",
				" # ",
				'#', pasteStack,
				'@', diamondStack);
	}

	@Override
	public void onPostInit() {
		// TODO: A two way mapping between bucket/fluid IDs would probably be
		//       More efficient for lookup by `CJBlockMachineBase`.
		for(int i = 0; i < Item.itemsList.length; i++) {
			Item item = Item.itemsList[i];

			if(item == null) continue;
			if(!(item instanceof ItemBucket)) continue;
			if(item == Item.bucketEmpty) continue;

			buckets.add((ItemBucket) item);

			// TODO: Replace with a Mixin once possible.
			try {
				Field field = ItemBucket.class.getDeclaredField("heldLiquid");
				field.setAccessible(true);
				int fluidID = (int) field.get(item);
				bucketFluids.add(fluidID);

				Logger.getLogger("ChloroJoules").info("Added bucket \"" +
						item.getItemName() + "\" (" + item.itemID + ") for " +
						"fluid \"" + Block.blocksList[fluidID].getBlockName() +
						"\" (" + fluidID + ")");
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
