package io.github.chlorojoules.client;

import com.fox2code.foxloader.client.CreativeItems;
import io.github.chlorojoules.CJInstance;
import io.github.chlorojoules.client.block.CJBlockMachineBase;
import io.github.chlorojoules.client.gui.CJGuiButton;
import io.github.chlorojoules.client.item.*;
import io.github.chlorojoules.client.machine.*;

import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.src.game.block.*;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.item.ItemBucket;

import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.registry.*;
import net.minecraft.src.game.item.ItemStack;
import net.minecraft.src.game.recipe.FurnaceRecipes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import static io.github.chlorojoules.client.gui.CJGuiGravity.*;
import static io.github.chlorojoules.client.gui.CJGuiMachineBaseLayout.*;
import static io.github.chlorojoules.client.CJRarityInfo.*;
import static io.github.chlorojoules.client.machine.CJMachineBuilder.*;
import static io.github.chlorojoules.client.machine.CJMachineRecipeTarget.*;

public class CJClient extends CJInstance implements ClientMod {
	public static Map<String, CJMachineBuilder> machines = new HashMap<>();

	public static List<ItemBucket> buckets = new ArrayList<>();
	public static List<Integer> bucketFluids = new ArrayList<>();

	public static RegisteredBlock fluidChlorojoules;
	public static RegisteredItem bucketFluidChlorojoules;
	public static RegisteredBlock fluidPaste;
	public static RegisteredItem bucketFluidPaste;
	public static RegisteredBlock fluidSouls;
	public static RegisteredItem bucketFluidSouls;

	public static RegisteredBlock machineFrame;
	public static RegisteredBlock compactedJewelDust;

	public static RegisteredBlock bugBlock;
	public static RegisteredBlock liquefier;
	public static RegisteredBlock solidifier;
	public static RegisteredBlock refinery;
	public static RegisteredBlock pulverizer;
	public static RegisteredBlock press;
	public static RegisteredBlock furnace;
	public static RegisteredBlock toolStation;
	public static RegisteredBlock transferor;

	public static RegisteredItem paste;

	public static RegisteredItem primalJewel;
	public static RegisteredItem manufacturedJewel;
	public static RegisteredItem refinedJewel;
	public static RegisteredItem awakenedJewel;

	public static RegisteredItem jewelDust;
	public static RegisteredItem soulDust;
	public static RegisteredItem soulEssence;
	public static RegisteredItem ironDust;
	public static RegisteredItem goldDust;
	public static RegisteredItem soulCore;

	public static RegisteredItem soulExtractor;
	public static RegisteredItem soulSword;
	public static RegisteredItem linker;

	public static RegisteredItem ironRod;

	public static int fuelFluid;

	public RegisteredBlock registerNewMachine(
			String name, CJMachineBuilder builder,
			String[] iconNames, int maxMetadata) {

		builder.setMachineName(name);
		machines.put(name, builder);

		BlockBuilder blockBuilder = new BlockBuilder()
				.setBlockName(name)
				.setGameBlockProvider(
						((id, build, ext) ->
								new CJBlockMachineBase(
										id, builder, iconNames, maxMetadata)))
				.setTooltipColor(getRarityColor(builder.rarity))
				.setItemBlock(new ItemBuilder()
						.setGameItemProvider(
								((id, build, block) ->
										new CJItemBlockMachineBase(
												id, block, maxMetadata)))
						.hideFromCreativeInventory());

		RegisteredBlock block = registerNewBlock(name, blockBuilder);

		addItemDamagesToCreative(block.asRegisteredItem(), maxMetadata);

		return block;
	}

	private void addItemDamagesToCreative(RegisteredItem item, int maxDamage) {
		for(int i = 0; i <= maxDamage; ++i) {
			ItemStack stack = new ItemStack(item.getRegisteredItemId(), 1, i);
			CreativeItems.addToCreativeInventory(stack);
		}
	}

	private RegisteredBlock registerNewFluid(String name) {
		Constructor<?> constructor =
				BlockFluidStationary.class.getDeclaredConstructors()[0];

		constructor.setAccessible(true);

		return registerNewBlock(
				name, new BlockBuilder()
						.setBlockMaterial(GameRegistry.BuiltInMaterial.WATER)
						.setBlockName(name)
						.setBlockHardness(100.0F)
						.setGameBlockProvider(
								(id, build, ext) ->
										(BlockFluidStationary)
												constructor.newInstance(
														id, Material.water)));

		// TODO: Implement `BlockFluid` wrapper/add a mixin to
		//       Expose its constructor for provider.
	}

	private RegisteredItem registerFluidBucket(
			String name, RegisteredBlock tile) {

		return registerNewItem(
				name, new ItemBuilder()
						.setItemName(name)
						.setMaxStackSize(1)
						.setGameItemProvider(((id, builder, ext) ->
								new ItemBucket(
										id - 256, tile.getRegisteredBlockId())
										.setContainerItem(Item.bucketEmpty))));
	}

	public static void callStaticMethod(
			Class<?> target, String name, Object... params)
			throws InvocationTargetException, IllegalAccessException {

		Method[] methods = target.getDeclaredMethods();

		for(Method method : methods) {
			if(!name.equals(method.getName())) continue;

			method.setAccessible(true);
			method.invoke(null, params);
			return;
		}

		throw new RuntimeException(
				"Method \"" + name + "\" could not be found in class \"" +
				target.getName() + "\"");
	}

	@Override
	public void onInit() {
		// TODO: Allow all balancing to be controlled from config -- we may
		//		 Need to request some upstream changes or provide our own
		//		 Config UI.

		try {
			callStaticMethod(
					TileEntity.class, "addMapping",
					CJTileEntityMachineBase.class, "cj_machine_base");
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}

		// TODO: Hack in our own mod-wise tooltips to mark things as being from
		//       Chlorojoules (Or just make a PR).

		// TODO: Hide fluids from creative inventory.

		// Fluids.
		{
			fluidChlorojoules = registerNewFluid("cj_fluid_chlorojoules");
			bucketFluidChlorojoules = registerFluidBucket(
					"cj_fluid_chlorojoules_bucket", fluidChlorojoules);

			fluidPaste = registerNewFluid("cj_fluid_paste");
			bucketFluidPaste = registerFluidBucket(
					"cj_fluid_paste_bucket", fluidPaste);

			fluidSouls = registerNewFluid("cj_fluid_souls");
			bucketFluidSouls = registerFluidBucket(
					"cj_fluid_souls_bucket", fluidSouls);
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
							.setMaxStackSize(1)
							.setItemName("cj_jewel_primal")
							.setTooltipColor(PRIMAL_COLOR));

			manufacturedJewel = registerNewItem(
					"cj_jewel_manufactured", new ItemBuilder()
							.setMaxStackSize(1)
							.setItemName("cj_jewel_manufactured")
							.setTooltipColor(MANUFACTURED_COLOR));

			refinedJewel = registerNewItem(
					"cj_jewel_refined", new ItemBuilder()
							.setMaxStackSize(1)
							.setItemName("cj_jewel_refined")
							.setTooltipColor(REFINED_COLOR));

			awakenedJewel = registerNewItem(
					"cj_jewel_awakened", new ItemBuilder()
							.setMaxStackSize(1)
							.setItemName("cj_jewel_awakened")
							.setTooltipColor(AWAKENED_COLOR));

			jewelDust = registerNewItem(
					"cj_jewel_dust", new ItemBuilder()
							.setItemName("cj_jewel_dust")
							.setTooltipColor(REFINED_COLOR));

			soulDust = registerNewItem(
					"cj_soul_dust", new ItemBuilder()
							.setItemName("cj_soul_dust")
							.setTooltipColor(REFINED_COLOR));

			soulEssence = registerNewItem(
					"cj_soul_essence", new ItemBuilder()
							.setItemName("cj_soul_essence")
							.setTooltipColor(REFINED_COLOR));

			ironDust = registerNewItem(
					"cj_iron_dust", new ItemBuilder()
							.setItemName("cj_iron_dust"));

			goldDust = registerNewItem(
					"cj_gold_dust", new ItemBuilder()
							.setItemName("cj_gold_dust"));

			soulExtractor = registerNewItem(
					"cj_soul_extractor", new ItemBuilder()
							.setMaxStackSize(1)
							.setItemName("cj_soul_extractor")
							.setTooltipColor(REFINED_COLOR)
							.setGameItemProvider(((id, build, ext) ->
									new CJItemSoulExtractor(id))));

			soulSword = registerNewItem(
					"cj_soul_sword", new ItemBuilder()
							.setMaxStackSize(1)
							.setItemName("cj_soul_sword")
							.setTooltipColor(REFINED_COLOR)
							.setGameItemProvider(((id, build, ext) ->
									new CJItemToolSoulSword(id))));

			linker = registerNewItem(
					"cj_linker", new ItemBuilder()
							.setMaxStackSize(1)
							.setItemName("cj_linker")
							.setGameItemProvider(((id, build, ext) ->
									new CJItemLinker(
											id, new String[] {
													"cj_link_item_empty",
													"cj_link_item_full",
													"cj_link_fluid_empty",
													"cj_link_fluid_full",
													"cj_multi_item_empty",
													"cj_multi_item_full",
													"cj_multi_fluid_empty",
													"cj_multi_fluid_full" })))
							.setTooltipColor(MANUFACTURED_COLOR));

			soulCore = registerNewItem(
					"cj_soul_core", new ItemBuilder()
							.setMaxStackSize(1)
							.setItemName("cj_soul_core")
							.setTooltipColor(AWAKENED_COLOR));

			ironRod = registerNewItem(
					"cj_iron_rod", new ItemBuilder()
							.setItemName("cj_iron_rod"));
		}

		// TODO: Feature request for registering ores.
		// TODO: Feature request for registering loot table additions.

		// Blocks.
		{
			machineFrame = registerNewBlock(
					"cj_machine_frame", new BlockBuilder()
							.setBlockMaterial(
									GameRegistry.BuiltInMaterial.ROCK)
							.setBlockHardness(1.5F)
							.setBlockResistance(10.0F)
							.setBlockStepSounds(
									GameRegistry.BuiltInStepSounds.STONE)
							.setEffectiveTool(RegisteredToolType.PICKAXE));

			compactedJewelDust = registerNewBlock(
					"cj_block_jewel_dust", new BlockBuilder()
							.setBlockMaterial(
									GameRegistry.BuiltInMaterial.SAND)
							.setBlockHardness(0.5F)
							.setBlockStepSounds(
									GameRegistry.BuiltInStepSounds.SAND)
							.setEffectiveTool(RegisteredToolType.SHOVEL));
		}

		// TODO: Feels like machines could be declared in JSON or smth (so too
		//       For all our registry here -- make our lives easier?)

		// TODO: Need a big machine UI fixup to make them more distinct and
		//       Improve alignment.

		// TODO: Make achievements to get ready for when they start working!

		// Machines.
		{
			bugBlock = registerNewMachine(
					"cj_bugblock", new CJMachineBuilder()
							.setRarity(CJRarity.AWAKENED)
							.addTank(15, 15, false, 4 * CJTank.BUCKET, 0)
							.addSlot(50, 35, false)
							.addSlot(75, 35, true)
							.setImpl(CJMachineBugBlock.class), null, 0);

			liquefier = registerNewMachine(
					"cj_liquefier", new CJMachineBuilder()
							.setRarity(CJRarity.PRIMAL)
							.addFuelTank()
							.addTankGravityVCenter(
									TOP_RIGHT, 10, true, 8 * CJTank.BUCKET, 0)
							.addSlotGravity(CENTER, 0, 0, false)
							/* TODO: Make Jewel slot maximum stack size 1. */
							/*
							 * TODO: Make Jewel slot visually distinct and
							 *       Reject Non-Jewel items.
							 */
							.addJewelSlot()
							/*
							 * TODO: Find a better way to center progress Bars
							 *       Between two elements.
							 */
							.addProgressBarGravityVCenter(
									CENTER, (SLOT_IN_WIDTH * 4) / 3)
							.addRecipe(
									10,
									new CJMachineRecipeComponent(
											0, Block.leaves, 1, false),
									new CJMachineRecipeComponent(
											1, fluidPaste, 50, true)
											.setTarget(TANK),
									50, true, -1)
							.setImpl(CJMachineRecipeConsumer.class), null, 0);

			refinery = registerNewMachine(
					"cj_refinery", new CJMachineBuilder()
							.setRarity(CJRarity.PRIMAL)
							.addFuelTank()
							.addTankGravityVCenter(
									TOP_LEFT,
									JEWEL_SLOT_INSET + SLOT_OUT_WIDTH,
									false, 8 * CJTank.BUCKET, 0)
							.addTankGravityVCenter(
									CENTER, SLOT_IN_WIDTH * 4, true,
									8 * CJTank.BUCKET, 0)
							.addJewelSlot()
							.addProgressBarGravityVCenter(CENTER, 0)
							.addRecipe(
									1,
									new CJMachineRecipeComponent(
											1, fluidPaste, 10, true)
											.setTarget(TANK),
									new CJMachineRecipeComponent(
											2, fluidChlorojoules, 5, true)
											.setTarget(TANK),
									10, true, -1)
							.setImpl(CJMachineRecipeConsumer.class), null, 0);

			solidifier = registerNewMachine(
					"cj_solidifier", new CJMachineBuilder()
							.setRarity(CJRarity.PRIMAL)
							.addFuelTank()
							.addTankGravityVCenter(
									TOP_LEFT,
									JEWEL_SLOT_INSET + SLOT_OUT_WIDTH,
									false, 8 * CJTank.BUCKET, 0)
							.addSlotGravityVCenter(
									CENTER, SLOT_IN_WIDTH * 4, true)
							.addJewelSlot()
							.addProgressBarGravityVCenter(CENTER, 0)
							.addButtonGravity(
									BOTTOM_RIGHT, JEWEL_SLOT_INSET,
									(WORKING_HEIGHT - FLUID_HEIGHT) / 2,
									"message.cj_enable_refine_fuel",
									CJGuiButton.JEWEL)
							.addRecipe(
									20,
									new CJMachineRecipeComponent(
											1, fluidPaste, 30, true)
											.setTarget(TANK),
									new CJMachineRecipeComponent(0, paste, 2),
									150, true, -1)
							.addRecipe(
									100,
									new CJMachineRecipeComponent(
											0, fluidChlorojoules, 250, true)
											.setTarget(TANK),
									new CJMachineRecipeComponent(
											0, manufacturedJewel, 1),
									500, false, 0)
							.setImpl(CJMachineRecipeConsumer.class), null, 0);

			// TODO: Secondary output.
			pulverizer = registerNewMachine(
					"cj_pulverizer", new CJMachineBuilder()
							.addFuelTank()
							.addSlotGravityVCenter(
									TOP_LEFT,
									JEWEL_SLOT_INSET + SLOT_OUT_WIDTH,
									false)
							.addSlotGravityVCenter(
									CENTER, SLOT_IN_WIDTH * 4, true)
							.addJewelSlot()
							.addProgressBarGravityVCenter(CENTER, 0)
							.addRecipe(
									10,
									new CJMachineRecipeComponent(
											0, primalJewel, 1),
									new CJMachineRecipeComponent(
											1, jewelDust, 2),
									100, false, -1)
							.addRecipe(
									10,
									new CJMachineRecipeComponent(
											0, manufacturedJewel, 1),
									new CJMachineRecipeComponent(
											1, jewelDust, 4),
									200, false, -1)
							.addRecipe(
									10,
									new CJMachineRecipeComponent(
											0, refinedJewel, 1),
									new CJMachineRecipeComponent(
											1, jewelDust, 16),
									250, false, -1)
							.addRecipe(
									10,
									new CJMachineRecipeComponent(
											0, awakenedJewel, 1),
									new CJMachineRecipeComponent(
											1, jewelDust, 64),
									350, false, -1)
							.addRecipe(
									30,
									new CJMachineRecipeComponent(
											0, Block.oreIron, 1, false),
									new CJMachineRecipeComponent(
											1, ironDust, 2),
									150, false, -1)
							.addRecipe(
									30,
									new CJMachineRecipeComponent(
											0, Block.oreIronNether, 1, false),
									new CJMachineRecipeComponent(
											1, ironDust, 3),
									150, false, -1)
							.addRecipe(
									30,
									new CJMachineRecipeComponent(
											0, Block.oreGold, 1, false),
									new CJMachineRecipeComponent(
											1, goldDust, 2),
									150, false, -1)
							.addRecipe(
									30,
									new CJMachineRecipeComponent(
											0, Block.oreGoldNether, 1, false),
									new CJMachineRecipeComponent(
											1, goldDust, 3),
									150, false, -1)
							.addRecipe(
									10,
									new CJMachineRecipeComponent(
											0, Block.sugarCane, 1, false),
									new CJMachineRecipeComponent(
											1, Item.sugar, 4),
									150, false, -1)
							// TODO: Add dyes when we have damage values.
							/*.addRecipe(
									10,
									new CJMachineRecipeComponent(
											0, Item.bone, 1),
									new CJMachineRecipeComponent(
											1, Item.dyePowder, 3),
									150, false)*/
							.setImpl(CJMachineRecipeConsumer.class), null, 0);

			press = registerNewMachine(
					"cj_press", new CJMachineBuilder()
							.addFuelTank()
							.addSlotGravityVCenter(
									TOP_LEFT,
									JEWEL_SLOT_INSET + SLOT_OUT_WIDTH,
									false)
							.addSlotGravityVCenter(
									CENTER, SLOT_IN_WIDTH * 4, true)
							.addJewelSlot()
							.addProgressBarGravityVCenter(CENTER, 0)
							.addRecipe(
									60,
									new CJMachineRecipeComponent(
											0, jewelDust, 4),
									new CJMachineRecipeComponent(
											1, compactedJewelDust, 1, false),
									150, false, -1)
							.addRecipe(
									15,
									new CJMachineRecipeComponent(
											0, Item.ingotIron, 1),
									new CJMachineRecipeComponent(
											1, Block.gear, 5, false),
									75, false, -1)
							.setImpl(CJMachineRecipeConsumer.class), null, 0);

			furnace = registerNewMachine(
					"cj_furnace", new CJMachineBuilder()
							.addFuelTank()
							.addSlotGravityVCenter(
									TOP_LEFT,
									JEWEL_SLOT_INSET + SLOT_OUT_WIDTH,
									false)
							.addSlotGravityVCenter(
									CENTER, SLOT_IN_WIDTH * 4, true)
							.addJewelSlot()
							.addProgressBarGravityVCenter(CENTER, 0)
							.addRecipeRarity(
									30,
									new CJMachineRecipeComponent(
											0, compactedJewelDust, 1, false),
									new CJMachineRecipeComponent(
											1, refinedJewel, 1),
									300, CJRarity.MANUFACTURED, -1)
							.setImpl(CJMachineRecipeConsumer.class), null, 0);

			toolStation = registerNewMachine(
					"cj_tool_station", new CJMachineBuilder()
							// TODO: These can just be centre-offset on either
							//       Side.
							.addSlotGravityVCenter(
									TOP_LEFT,
									JEWEL_SLOT_INSET,
									false)
							.addSlotGravityVCenter(
									CENTER,
									JEWEL_SLOT_INSET + SLOT_OUT_WIDTH,
									false)
							.addProgressBarGravityVCenter(CENTER, 0)
							.setImpl(CJMachineToolStation.class), null, 0);

			// TODO: For `Soul Extractor` -- make base tool then socket a
			//       `Refined ChloroJewel` to use; allows player to reclaim
			//       The jewel once they don't need the tool anymore.

			// TODO: Figure out how to make Gear controls.
			// TODO: UI to allow floopers to be filtered on one fluid kind.
			transferor = registerNewMachine(
					"cj_transferor", new CJMachineBuilder()
							.addCoordinateGravityHCenter(
									CENTER, 12, "message.cj_link_coordinate")
							.addTankGravity(
									CENTER, 0, 0, false, 2 * CJTank.BUCKET, 0)
							.addSlotGravity(CENTER, 0, 0, false)
							.setSlotDamageExclusive(0, new int[] {
									CJMachineTransferor.TRANSMIT_ITEMS,
									CJMachineTransferor.MULTI_TRANSMIT_ITEMS })
							.setTankDamageExclusive(0, new int[] {
									CJMachineTransferor.TRANSMIT_FLUIDS,
									CJMachineTransferor.MULTI_TRANSMIT_FLUIDS })
							.setImpl(CJMachineTransferor.class),
							new String[] {
									"cj_inactive",
									"cj_whooper",
									"cj_swooper",
									"cj_flooper",
									"cj_slooper",
									"cj_multi_whooper",
									"cj_multi_flooper"
							}, CJMachineTransferor.MAX_DAMAGE);
		}
	}

	@Override
	public void onPostInit() {
		// TODO: A two way mapping between bucket/fluid IDs would probably be
		//       More efficient for lookup by `CJBlockMachineBase`.
		// Register fluids/buckets.
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

		// Recipe item stacks.
		RegisteredItemStack pasteStack = paste.newRegisteredItemStack();
		RegisteredItemStack primalJewelStack =
				primalJewel.newRegisteredItemStack();

		RegisteredItemStack machineFrameStack =
				machineFrame.newRegisteredItemStack();

		RegisteredItemStack machineFrame4Stack =
				machineFrame.newRegisteredItemStack();

		machineFrame4Stack.setRegisteredStackSize(4);

		RegisteredItemStack liquefierStack =
				liquefier.newRegisteredItemStack();

		RegisteredItemStack solidifierStack =
				solidifier.newRegisteredItemStack();

		RegisteredItemStack refineryStack =
				refinery.newRegisteredItemStack();

		RegisteredItemStack pulverizerStack =
				pulverizer.newRegisteredItemStack();

		RegisteredItemStack pressStack =
				press.newRegisteredItemStack();

		RegisteredItemStack toolStationStack =
				toolStation.newRegisteredItemStack();

		//RegisteredItemStack flooper4Stack = flooper.newRegisteredItemStack();
		//flooper4Stack.setRegisteredStackSize(4);

//		RegisteredItemStack whooper4Stack = whooper.newRegisteredItemStack();
//		whooper4Stack.setRegisteredStackSize(4);

		RegisteredItemStack ironRodStack = ironRod.newRegisteredItemStack();

		RegisteredItemStack furnaceStack =
				Block.furnaceIdle.newRegisteredItemStack();

		RegisteredItemStack poweredFurnaceStack =
				furnace.newRegisteredItemStack();

		RegisteredItemStack cobblestoneStack =
				Block.cobblestone.newRegisteredItemStack();

		RegisteredItemStack ironAxeStack =
				Item.axeSteel.newRegisteredItemStack();

		RegisteredItemStack ironPickaxeStack =
				Item.pickaxeSteel.newRegisteredItemStack();

		RegisteredItemStack ironShovelStack =
				Item.shovelSteel.newRegisteredItemStack();

		RegisteredItemStack bucketStack =
				Item.bucketEmpty.newRegisteredItemStack();

		RegisteredItemStack brickStack =
				Item.brick.newRegisteredItemStack();

		RegisteredItemStack ironBlockStack =
				Block.blockIron.newRegisteredItemStack();

		RegisteredItemStack diamondStack =
				Item.diamond.newRegisteredItemStack();

		RegisteredItemStack ironStack =
				Item.ingotIron.newRegisteredItemStack();

		RegisteredItemStack goldStack =
				Item.ingotGold.newRegisteredItemStack();

		RegisteredItemStack flintStack =
				Item.flint.newRegisteredItemStack();

		RegisteredItemStack gunpowderStack =
				Item.gunpowder.newRegisteredItemStack();

		RegisteredItemStack cauldronStack =
				Block.cauldron.newRegisteredItemStack();

		RegisteredItemStack chestStack =
				Block.chest.newRegisteredItemStack();

		RegisteredItemStack glassStack =
				Block.glass.newRegisteredItemStack();

		RegisteredItemStack ashStack = Item.ash.newRegisteredItemStack();
		RegisteredItemStack coalStack = Item.coal.newRegisteredItemStack();

		RegisteredItemStack jewelDustStack =
				jewelDust.newRegisteredItemStack();

		RegisteredItemStack soulDust2Stack =
				soulDust.newRegisteredItemStack();

		soulDust2Stack.setRegisteredStackSize(2);

		RegisteredItemStack soulEssenceStack =
				soulEssence.newRegisteredItemStack();

		RegisteredItemStack soulExtractorStack =
				soulExtractor.newRegisteredItemStack();

		soulExtractorStack.setRegisteredDamage(MAX_DAMAGE);

		RegisteredItemStack soulSwordStack =
				soulSword.newRegisteredItemStack();

		soulSwordStack.setRegisteredDamage(MAX_DAMAGE);

		RegisteredItemStack soulCoreStack = soulCore.newRegisteredItemStack();
		RegisteredItemStack magmaStack = Block.magma.newRegisteredItemStack();

		// Vanilla machine recipes.
		registerFurnaceRecipe(Block.leaves.asRegisteredItem(), pasteStack);
		registerFurnaceRecipe(goldDust, goldStack);
		registerFurnaceRecipe(ironDust, ironStack);

		// Crafting recipes.
		registerRecipe(
				machineFrame4Stack,
				"%~%",
				"~|~",
				"%#%",
				'%', cobblestoneStack,
				'#', ironStack,
				'~', pasteStack,
				'|', cauldronStack);

		registerRecipe(
				ironRodStack,
				"  #",
				" # ",
				"#  ",
				'#', ironStack);

		registerRecipe(
				primalJewelStack,
				" ~ ",
				"~@~",
				" ~ ",
				'~', pasteStack,
				'@', diamondStack);

		registerRecipe(
				liquefierStack,
				"@@@",
				"&|&",
				"&%&",
				'&', cobblestoneStack,
				'%', furnaceStack,
				'@', flintStack,
				'|', machineFrameStack);

		registerRecipe(
				solidifierStack,
				"&@&",
				"@|@",
				"&%&",
				'&', cobblestoneStack,
				'@', ashStack,
				'%', coalStack,
				'|', machineFrameStack);

		registerRecipe(
				refineryStack,
				"&%&",
				"@|@",
				"&&&",
				'&', cobblestoneStack,
				'@', glassStack,
				'%', bucketStack,
				'|', machineFrameStack);

		registerRecipe(
				pulverizerStack,
				"&@&",
				"@|@",
				"&%&",
				'&', cobblestoneStack,
				'@', flintStack,
				'%', gunpowderStack,
				'|', machineFrameStack);

		registerRecipe(
				pressStack,
				"&@&",
				"%|%",
				"&%&",
				'&', cobblestoneStack,
				'@', ironBlockStack,
				'%', brickStack,
				'|', machineFrameStack);

		registerRecipe(
				poweredFurnaceStack,
				"&&&",
				"@|@",
				"%&%",
				'&', cobblestoneStack,
				'@', coalStack,
				'%', furnaceStack,
				'|', machineFrameStack);

		registerRecipe(
				toolStationStack,
				"&&&",
				"a|c",
				"%b%",
				'&', cobblestoneStack,
				'a', ironAxeStack,
				'b', ironPickaxeStack,
				'c', ironShovelStack,
				'|', machineFrameStack);

//		registerRecipe(
//				flooper4Stack,
//				" % ",
//				" | ",
//				" # ",
//				'%', bucketStack,
//				'#', ironStack,
//				'|', machineFrameStack);

//		registerRecipe(
//				whooper4Stack,
//				" % ",
//				" | ",
//				" # ",
//				'%', chestStack,
//				'#', ironStack,
//				'|', machineFrameStack);

		registerRecipe(
				soulSwordStack,
				"  #",
				" % ",
				"@  ",
				'%', soulCoreStack,
				'#', magmaStack,
				'@', ironRodStack);

		registerRecipe(
				soulExtractorStack,
				" %#",
				" @%",
				"@  ",
				'%', pasteStack,
				'#', ironStack,
				'@', ironRodStack);

		registerShapelessRecipe(
				soulDust2Stack, jewelDustStack, soulEssenceStack);

		// TODO: Add an auto-crafter.

		// Consume furnace recipes.
		FurnaceRecipes furnaceRecipes = FurnaceRecipes.instance;
		Map<Integer, ItemStack> furnaceMap =
				furnaceRecipes.getSmeltingList();

		Set<Map.Entry<Integer, ItemStack>> furnaceEntries =
				furnaceMap.entrySet();

		CJMachineBuilder furnaceMachine = machines.get("cj_furnace");

		for(Map.Entry<Integer, ItemStack> entry : furnaceEntries) {
			furnaceMachine.addRecipe(
					20,
					new CJMachineRecipeComponent(0, entry.getKey(), 1),
					// TODO: `ItemStack` constructor for
					//       `CJMachineRecipeComponent`.
					new CJMachineRecipeComponent(
							1, entry.getValue().getRegisteredItem(), 1),
					200, false, -1); // Vanilla furnace ticks as base.
		}
	}
}
