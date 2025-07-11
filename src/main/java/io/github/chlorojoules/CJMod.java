package io.github.chlorojoules;

import com.fox2code.foxloader.loader.Mod;
import io.github.chlorojoules.block.CJBlockMachineBase;
import io.github.chlorojoules.gui.CJGuiButton;
import io.github.chlorojoules.gui.creative.CJCreativeTabChlorojoules;
import io.github.chlorojoules.item.*;
import io.github.chlorojoules.machine.*;

import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.item.CJItemBlockMachineBase;
import io.github.chlorojoules.item.CJItemLinker;
import io.github.chlorojoules.item.CJItemSoulExtractor;
import io.github.chlorojoules.item.CJItemToolSoulSword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.creative.CreativeTab;
import net.minecraft.client.gui.creative.CreativeTabs;
import net.minecraft.common.block.*;
import net.minecraft.common.block.children.BlockFluid;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.block.sound.StepSounds;
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.item.Item;

import com.fox2code.foxloader.registry.*;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.Items;
import net.minecraft.common.item.children.ItemBucket;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.recipe.CraftingManager;
import net.minecraft.common.recipe.FurnaceRecipes;
import net.minecraft.common.recipe.RecipesCrafting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import static io.github.chlorojoules.gui.CJGuiGravity.*;
import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;
import static io.github.chlorojoules.CJRarityInfo.*;
import static io.github.chlorojoules.machine.CJMachineBuilder.*;
import static io.github.chlorojoules.machine.CJMachineRecipeTarget.*;

public class CJMod extends Mod {
	public static Map<String, CJMachineBuilder> machines = new HashMap<>();

	public static List<ItemBucket> buckets = new ArrayList<>();
	public static List<Integer> bucketFluids = new ArrayList<>();

	public static Block fluidChlorojoules;
	public static Item bucketFluidChlorojoules;
	public static Block fluidPaste;
	public static Item bucketFluidPaste;
	public static Block fluidSouls;
	public static Item bucketFluidSouls;

	public static Block machineFrame;
	public static Block compactedJewelDust;

	public static Block bugBlock;
	public static Block cultivator;
	public static Block liquefier;
	public static Block solidifier;
	public static Block refinery;
	public static Block pulverizer;
	public static Block press;
	public static Block furnace;
	public static Block toolStation;
	public static Block transferor;
	public static Block tank;
	public static Block mixer;

	public static Item paste;

	public static Item primalJewel;
	public static Item manufacturedJewel;
	public static Item refinedJewel;
	public static Item awakenedJewel;

	public static Item jewelDust;
	public static Item soulDust;
	public static Item soulEssence;
	public static Item ironDust;
	public static Item goldDust;
	public static Item soulCore;
	public static Item moss;

	public static Item soulExtractor;
	public static Item soulSword;
	public static Item linker;

	public static Item ironRod;

	public static int fuelFluid;
	public static int waterFluid;
	public static int lavaFluid;

	public static CreativeTab creativeTab = CreativeTabs.MECHANICAL_BLOCKS;

	public static void sendChat(String message) {
		Minecraft.getInstance().ingameGUI.addChatMessage(message);
	}

	public Block registerNewMachine(
			String name, CJMachineBuilder builder,
			String[] iconNames, int maxMetadata) {

		builder.setMachineName(name);
		machines.put(name, builder);

		Block block =
				new CJBlockMachineBase(name, builder, iconNames, maxMetadata)
						.setBlockName(name)
						.setTooltipColor(getRarityColor(builder.rarity))
						.hideFromCreativeMenu();

		addItemDamagesToCreative(block.getItemID(), maxMetadata);

		return block;
	}

	private void addItemDamagesToCreative(int item, int maxDamage) {
		for(int i = 0; i <= maxDamage; ++i) {
			creativeTab.add(new ItemStack(item, 1, i));
		}
	}

	private Block registerNewFluid(String name) {
		return new BlockFluid(name, Materials.WATER, name, true)
				.setBlockName(name)
				.setCreativeTab(creativeTab);
	}

	private Item registerFluidBucket(String name, Block tile) {
		return new ItemBucket(name, tile.blockID)
				.setItemName(name)
				.setMaxStackSize(1)
				.setContainerItem(Items.EMPTY_BUCKET)
				.setCreativeTab(creativeTab);
	}

	@Override
	public void onPreInit() {
		// TODO: Allow all balancing to be controlled from config -- we may
		//		 Need to request some upstream changes or provide our own
		//		 Config UI.

		boolean setTab = false;
		// TODO: This doesn't work atm.
//		for(int i = 0; i < CreativeTabs.TABS.length; ++i) {
//			if(CreativeTabs.TABS[i] == null) {
//				creativeTab = new CJCreativeTabChlorojoules(i);
//				setTab = true;
//				break;
//			}
//		}

		if(!setTab) {
			Logger.getLogger("ChloroJoules").warning(
					"Failed to add Creative tab -- using Mechanical Blocks " +
					"as fallback");
		}

		TileEntity.addMapping(
				CJTileEntityMachineBase.class, "cj_machine_base");

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

		fuelFluid = fluidChlorojoules.blockID;
		waterFluid = Blocks.WATER_MOVING.blockID;
		lavaFluid = Blocks.LAVA_MOVING.blockID;

		// Items.
		{
			paste = new Item("cj_paste")
					.setItemName("cj_paste")
					.setTooltipColor(PRIMAL_COLOR)
					.setCreativeTab(creativeTab);

			primalJewel = new Item("cj_jewel_primal")
					.setItemName("cj_jewel_primal")
					.setMaxStackSize(1)
					.setTooltipColor(PRIMAL_COLOR)
					.setCreativeTab(creativeTab);

			manufacturedJewel = new Item("cj_jewel_manufactured")
					.setItemName("cj_jewel_manufactured")
					.setMaxStackSize(1)
					.setTooltipColor(MANUFACTURED_COLOR)
					.setCreativeTab(creativeTab);

			refinedJewel = new Item("cj_jewel_refined")
					.setItemName("cj_jewel_refined")
					.setMaxStackSize(1)
					.setTooltipColor(REFINED_COLOR)
					.setCreativeTab(creativeTab);

			awakenedJewel = new Item("cj_jewel_awakened")
					.setItemName("cj_jewel_awakened")
					.setMaxStackSize(1)
					.setTooltipColor(AWAKENED_COLOR)
					.setCreativeTab(creativeTab);

			if(creativeTab != CreativeTabs.MECHANICAL_BLOCKS) {
				creativeTab.setTabIcon(new ItemStack(awakenedJewel));
			}

			jewelDust = new Item("cj_jewel_dust")
					.setItemName("cj_jewel_dust")
					.setTooltipColor(REFINED_COLOR)
					.setCreativeTab(creativeTab);

			soulDust = new Item("cj_soul_dust")
					.setItemName("cj_soul_dust")
					.setTooltipColor(REFINED_COLOR)
					.setCreativeTab(creativeTab);

			soulEssence = new Item("cj_soul_essence")
					.setItemName("cj_soul_essence")
					.setTooltipColor(REFINED_COLOR)
					.setCreativeTab(creativeTab);

			ironDust = new Item("cj_iron_dust")
					.setItemName("cj_iron_dust")
					.setCreativeTab(creativeTab);

			goldDust = new Item("cj_gold_dust")
					.setItemName("cj_gold_dust")
					.setCreativeTab(creativeTab);

			soulExtractor = new CJItemSoulExtractor("cj_soul_extractor")
					.setItemName("cj_soul_extractor")
					.setMaxStackSize(1)
					.setTooltipColor(REFINED_COLOR)
					.setCreativeTab(creativeTab);

			soulSword = new CJItemToolSoulSword("cj_soul_sword")
					.setItemName("cj_soul_sword")
					.setMaxStackSize(1)
					.setTooltipColor(REFINED_COLOR)
					.setCreativeTab(creativeTab);

			linker = new CJItemLinker(
					"cj_linker",
					new String[] {
							"cj_link_item_empty",
							"cj_link_item_full",
							"cj_link_fluid_empty",
							"cj_link_fluid_full",
							"cj_multi_item_empty",
							"cj_multi_item_full",
							"cj_multi_fluid_empty",
							"cj_multi_fluid_full"
					})
					.setItemName("cj_linker")
					.setMaxStackSize(1)
					.setTooltipColor(MANUFACTURED_COLOR)
					.setCreativeTab(creativeTab);

			soulCore = new Item("cj_soul_core")
					.setItemName("cj_soul_core")
					.setMaxStackSize(1)
					.setTooltipColor(AWAKENED_COLOR)
					.setCreativeTab(creativeTab);

			ironRod = new Item("cj_iron_rod")
					.setItemName("cj_iron_rod")
					.setCreativeTab(creativeTab);

			moss = new Item("cj_moss")
					.setItemName("cj_moss")
					.setCreativeTab(creativeTab);
		}

		// TODO: Feature request for registering ores.
		// TODO: Feature request for registering loot table additions.

		// Blocks.
		{
			machineFrame = new Block("cj_machine_frame", Materials.ROCK)
					.setBlockName("cj_machine_frame")
					.setHardness(1.5F)
					.setResistance(10.0F)
					.setSound(StepSounds.SOUND_STONE)
					.setEffectiveTool(EnumTools.PICKAXE)
					.setCreativeTab(creativeTab);

			compactedJewelDust = new Block(
					"cj_block_jewel_dust", Materials.SAND)
					.setBlockName("cj_block_jewel_dust")
					.setHardness(0.5F)
					.setSound(StepSounds.SOUND_SAND)
					.setEffectiveTool(EnumTools.SHOVEL)
					.setCreativeTab(creativeTab);
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
							.addTank(
									15, 15, false, false, 4 * CJTank.BUCKET, 0)
							.addSlot(50, 35, false)
							.addSlot(75, 35, true)
							.setImpl(CJMachineBugBlock.class), null, 0);

			cultivator = registerNewMachine(
					"cj_cultivator", new CJMachineBuilder()
							.setRarity(CJRarity.MANUFACTURED)
							.addFuelTank()
							.addTankGravityVCenter(
									CENTER, -25, false, false,
									4 * CJTank.BUCKET,
									waterFluid)
							.addSlotGravity(CENTER, 0, 0, false)
							.addSlotGravity(CENTER, 0, 24, false)
							.addSlotGravity(
									CENTER, SLOT_IN_WIDTH * 4, 0, true)
							.addJewelSlot()
							.addRecipe(
									50,
									new CJMachineRecipeComponent[] {
											new CJMachineRecipeComponent(
													0, Items.SEEDS, 0),
											new CJMachineRecipeComponent(
													1, waterFluid, 100)
													.setTarget(TANK),
											// TODO: Damage to bone meal.
											// TODO: Bone meal should be a
											//		 Catalyst -- recipe
											//		 Components which are
											//		 Optional?
											new CJMachineRecipeComponent(
													1, Items.DYE_POWDER, 1),
									},
									new CJMachineRecipeComponent[] {
											new CJMachineRecipeComponent(
													2, Items.WHEAT, 1)
									},
									100, false, -1)
							.addRecipe(
									20,
									new CJMachineRecipeComponent[] {
											new CJMachineRecipeComponent(
													0,
													Blocks.MOSSY_COBBLESTONE,
													0, false),
											new CJMachineRecipeComponent(
													1, waterFluid, 50)
													.setTarget(TANK),
											new CJMachineRecipeComponent(
													1, Items.DYE_POWDER, 1),
									},
									new CJMachineRecipeComponent[] {
											new CJMachineRecipeComponent(
													2, moss, 1)
									},
									100, false, -1)
							.addProgressBarGravityVCenter(
									CENTER, (SLOT_IN_WIDTH * 4) / 3)
							.setImpl(CJMachineRecipeConsumer.class),
					null, 0);

			liquefier = registerNewMachine(
					"cj_liquefier", new CJMachineBuilder()
							.setRarity(CJRarity.PRIMAL)
							.addFuelTank()
							.addTankGravityVCenter(
									TOP_RIGHT, 10, true, false,
									8 * CJTank.BUCKET, 0)
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
									// TODO: Add recipes for all leaf types.
									//		 Can we implement a proto-tagging
									//		 System like modern MC's #leaves?
									new CJMachineRecipeComponent(
											0, Blocks.OAK_LEAVES, 1, false),
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
									false, false, 8 * CJTank.BUCKET, 0)
							.addTankGravityVCenter(
									CENTER, SLOT_IN_WIDTH * 4, true, false,
									8 * CJTank.BUCKET, 0)
							.addJewelSlot()
							.addProgressBarGravityVCenter(CENTER, 0)
							.addRecipe(
									1,
									new CJMachineRecipeComponent(
											1, fluidPaste, 20, true)
											.setTarget(TANK),
									new CJMachineRecipeComponent(
											2, fluidChlorojoules, 25, true)
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
									false, false, 8 * CJTank.BUCKET, 0)
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
											1, jewelDust, 8),
									250, false, -1)
							.addRecipe(
									10,
									new CJMachineRecipeComponent(
											0, awakenedJewel, 1),
									new CJMachineRecipeComponent(
											1, jewelDust, 16),
									350, false, -1)
							// TODO: Need to handle more ore types.
							.addRecipe(
									30,
									new CJMachineRecipeComponent(
											0, Blocks.IRON_ORE, 1, false),
									new CJMachineRecipeComponent(
											1, ironDust, 2),
									150, false, -1)
							.addRecipe(
									30,
									new CJMachineRecipeComponent(
											0, Blocks.NETHER_IRON_ORE, 1,
											false),
									new CJMachineRecipeComponent(
											1, ironDust, 3),
									150, false, -1)
							.addRecipe(
									30,
									new CJMachineRecipeComponent(
											0, Blocks.GOLD_ORE, 1, false),
									new CJMachineRecipeComponent(
											1, goldDust, 2),
									150, false, -1)
							.addRecipe(
									30,
									new CJMachineRecipeComponent(
											0, Blocks.NETHER_GOLD_ORE, 1,
											false),
									new CJMachineRecipeComponent(
											1, goldDust, 3),
									150, false, -1)
							.addRecipe(
									10,
									new CJMachineRecipeComponent(
											0, Blocks.SUGAR_CANE, 1, false),
									new CJMachineRecipeComponent(
											1, Items.SUGAR, 4),
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
											0, Items.IRON_INGOT, 1),
									new CJMachineRecipeComponent(
											1, Blocks.GEAR, 5, false),
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
									CENTER, 0, 0, false, false,
									2 * CJTank.BUCKET, 0)
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
							"cj_multi_flooper" },
					CJMachineTransferor.MAX_DAMAGE);

			tank = registerNewMachine(
					"cj_tank", new CJMachineBuilder()
							.addTankGravity(
									CENTER, 0, 0, false, true,
									16 * CJTank.BUCKET, 0),
					null, 0);

			mixer = registerNewMachine(
					"cj_mixer", new CJMachineBuilder()
							.setRarity(CJRarity.REFINED)
							.addFuelTank()
							.addTankGravityVCenter(
									TOP_LEFT,
									JEWEL_SLOT_INSET + SLOT_OUT_WIDTH,
									false, false, 8 * CJTank.BUCKET, 0)
							.addTankGravityVCenter(
									CENTER, SLOT_IN_WIDTH * 4, true, false,
									8 * CJTank.BUCKET, 0)
							.addSlotGravityVCenter(
									CENTER, 0, false)
							.addJewelSlot()
							.addProgressBarGravityVCenter(
									CENTER, SLOT_IN_WIDTH * 2)
							.addButtonGravity(
									BOTTOM_RIGHT, JEWEL_SLOT_INSET,
									(WORKING_HEIGHT - FLUID_HEIGHT) / 2,
									"message.cj_enable_refine_fuel",
									CJGuiButton.JEWEL)
							//.addRecipe(
							//		150,
							//		new CJMachineRecipeComponent[] {
							//				new CJMachineRecipeComponent(
							//						0, fluidChlorojoules, 250,
							//						true)
							//						.setTarget(TANK),
							//				new CJMachineRecipeComponent(
							//						0, soulDust, 1)
							//		},
							//		new CJMachineRecipeComponent[] {
							//				new CJMachineRecipeComponent(
							//						2, fluidSouls, 50, true)
							//						.setTarget(TANK)
							//		},
							//		250, false, 0)
							.addRecipe(
									50,
									new CJMachineRecipeComponent[] {
											new CJMachineRecipeComponent(
													1, waterFluid, 250)
													.setTarget(TANK),
											new CJMachineRecipeComponent(
													0, moss, 1)
									},
									new CJMachineRecipeComponent[] {
											new CJMachineRecipeComponent(
													2, fluidChlorojoules,
													700, true)
													.setTarget(TANK)
									},
									300, false, -1)
							.setImpl(CJMachineRecipeConsumer.class), null, 0);
		}
	}

	@Override
	public void onPostInit() {
		// TODO: A two way mapping between bucket/fluid IDs would probably be
		//       More efficient for lookup by `CJBlockMachineBase`.
		// Register fluids/buckets.
		for(int i = 0; i < Items.ITEMS_LIST.length; i++) {
			Item item = Items.ITEMS_LIST[i];

			if(item == null) continue;
			if(!(item instanceof ItemBucket)) continue;
			if(item == Items.EMPTY_BUCKET) continue;

			ItemBucket bucket = (ItemBucket) item;
			buckets.add(bucket);

			int fluidID = bucket.getHeldLiquid();
			bucketFluids.add(fluidID);

			Logger.getLogger("ChloroJoules").info("Added bucket \"" +
					item.getItemName() + "\" (" + item.itemID + ") for " +
					"fluid \"" + Blocks.BLOCKS_LIST[fluidID].getBlockName() +
					"\" (" + fluidID + ")");
		}

		// Recipe item stacks.
		ItemStack pasteStack = new ItemStack(paste);
		ItemStack primalJewelStack = new ItemStack(primalJewel);
		ItemStack machineFrameStack = new ItemStack(machineFrame);
		ItemStack machineFrame4Stack = new ItemStack(machineFrame);
		machineFrame4Stack.stackSize = 4;

		ItemStack liquefierStack = new ItemStack(liquefier);
		ItemStack solidifierStack = new ItemStack(solidifier);
		ItemStack refineryStack = new ItemStack(refinery);
		ItemStack pulverizerStack = new ItemStack(pulverizer);
		ItemStack pressStack = new ItemStack(press);
		ItemStack toolStationStack = new ItemStack(toolStation);
		ItemStack transferor8Stack = new ItemStack(transferor);
		transferor8Stack.stackSize = 8;

		ItemStack ironRodStack = new ItemStack(ironRod);
		ItemStack furnaceStack = new ItemStack(Blocks.FURNACE_IDLE);
		ItemStack poweredFurnaceStack = new ItemStack(furnace);
		ItemStack cobblestoneStack = new ItemStack(Blocks.COBBLESTONE);
		ItemStack ironAxeStack = new ItemStack(Items.IRON_AXE);
		ItemStack ironPickaxeStack = new ItemStack(Items.IRON_PICKAXE);
		ItemStack ironShovelStack = new ItemStack(Items.IRON_SHOVEL);
		ItemStack bucketStack = new ItemStack(Items.EMPTY_BUCKET);
		ItemStack brickStack = new ItemStack(Items.BRICK);
		ItemStack ironBlockStack = new ItemStack(Blocks.IRON_BLOCK);
		ItemStack diamondStack = new ItemStack(Items.DIAMOND);
		ItemStack ironStack = new ItemStack(Items.IRON_INGOT);
		ItemStack goldStack = new ItemStack(Items.GOLD_INGOT);
		ItemStack flintStack = new ItemStack(Items.FLINT);
		ItemStack gunpowderStack = new ItemStack(Items.GUNPOWDER);
		ItemStack cauldronStack = new ItemStack(Blocks.CAULDRON);
		ItemStack chestStack = new ItemStack(Blocks.CHEST);
		ItemStack glassStack = new ItemStack(Blocks.GLASS);
		ItemStack ashStack = new ItemStack(Items.ASH);
		ItemStack coalStack = new ItemStack(Items.COAL);
		ItemStack jewelDustStack = new ItemStack(jewelDust);
		ItemStack soulDust2Stack = new ItemStack(soulDust);
		soulDust2Stack.stackSize = 2;

		ItemStack soulEssenceStack = new ItemStack(soulEssence);
		ItemStack soulExtractorStack = new ItemStack(soulExtractor);
		soulExtractorStack.setItemDamage(MAX_DAMAGE);

		ItemStack soulSwordStack = new ItemStack(soulSword);
		soulSwordStack.setItemDamage(MAX_DAMAGE);

		ItemStack mossStack = new ItemStack(moss);
		ItemStack mossyCobblestoneStack =
				new ItemStack(Blocks.MOSSY_COBBLESTONE);

		ItemStack soulCoreStack = new ItemStack(soulCore);
		ItemStack magmaStack = new ItemStack(Blocks.MAGMA);

		// Vanilla machine recipes.
		FurnaceRecipes.instance.addSmelting(
				Blocks.OAK_LEAVES.getItemID(), pasteStack);

		FurnaceRecipes.instance.addSmelting(goldDust.itemID, pasteStack);
		FurnaceRecipes.instance.addSmelting(ironDust.itemID, pasteStack);

		// Crafting recipes.
		CraftingManager.getInstance().addRecipe(
				machineFrame4Stack,
				"%~%",
				"~|~",
				"% %",
				'%', cobblestoneStack,
				'~', pasteStack,
				'|', cauldronStack);

		CraftingManager.getInstance().addShapelessRecipe(
				mossyCobblestoneStack, cobblestoneStack, mossStack);

		CraftingManager.getInstance().addRecipe(
				ironRodStack,
				"  #",
				" # ",
				"#  ",
				'#', ironStack);

		CraftingManager.getInstance().addRecipe(
				primalJewelStack,
				" ~ ",
				"~@~",
				" ~ ",
				'~', pasteStack,
				'@', diamondStack);

		CraftingManager.getInstance().addRecipe(
				liquefierStack,
				"@@@",
				"&|&",
				"&%&",
				'&', cobblestoneStack,
				'%', furnaceStack,
				'@', flintStack,
				'|', machineFrameStack);

		CraftingManager.getInstance().addRecipe(
				solidifierStack,
				"&@&",
				"@|@",
				"&%&",
				'&', cobblestoneStack,
				'@', ashStack,
				'%', coalStack,
				'|', machineFrameStack);

		CraftingManager.getInstance().addRecipe(
				refineryStack,
				"&%&",
				"@|@",
				"&&&",
				'&', cobblestoneStack,
				'@', glassStack,
				'%', bucketStack,
				'|', machineFrameStack);

		CraftingManager.getInstance().addRecipe(
				pulverizerStack,
				"&@&",
				"@|@",
				"&%&",
				'&', cobblestoneStack,
				'@', flintStack,
				'%', gunpowderStack,
				'|', machineFrameStack);

		CraftingManager.getInstance().addRecipe(
				pressStack,
				"&@&",
				"%|%",
				"&%&",
				'&', cobblestoneStack,
				'@', ironBlockStack,
				'%', brickStack,
				'|', machineFrameStack);

		CraftingManager.getInstance().addRecipe(
				poweredFurnaceStack,
				"&&&",
				"@|@",
				"%&%",
				'&', cobblestoneStack,
				'@', coalStack,
				'%', furnaceStack,
				'|', machineFrameStack);

		// TODO: This is broken.
		CraftingManager.getInstance().addRecipe(
				toolStationStack,
				"&&&",
				"a|c",
				"%b%",
				'&', cobblestoneStack,
				'a', ironAxeStack,
				'b', ironPickaxeStack,
				'c', ironShovelStack,
				'|', machineFrameStack);

		CraftingManager.getInstance().addRecipe(
				transferor8Stack,
				"&%&",
				"&|&",
				"&@&",
				'&', cobblestoneStack,
				'%', bucketStack,
				'@', chestStack,
				'|', machineFrameStack);

//		CraftingManager.getInstance().addRecipe(
//				whooper4Stack,
//				" % ",
//				" | ",
//				" # ",
//				'%', chestStack,
//				'#', ironStack,
//				'|', machineFrameStack);

		CraftingManager.getInstance().addRecipe(
				soulSwordStack,
				"  #",
				" % ",
				"@  ",
				'%', soulCoreStack,
				'#', magmaStack,
				'@', ironRodStack);

		CraftingManager.getInstance().addRecipe(
				soulExtractorStack,
				" %#",
				" @%",
				"@  ",
				'%', pasteStack,
				'#', ironStack,
				'@', ironRodStack);

		CraftingManager.getInstance().addShapelessRecipe(
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
							1, entry.getValue().getItemID(), 1),
					200, false, -1); // Vanilla furnace ticks as base.
		}
	}
}
