package io.github.chlorojoules;

import com.fox2code.foxloader.loader.Mod;
import io.github.chlorojoules.block.CJBlockMachineBase;
import io.github.chlorojoules.gui.CJGuiButton;
import io.github.chlorojoules.item.*;
import io.github.chlorojoules.machine.*;

import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.creative.CreativeTab;
import net.minecraft.client.gui.creative.CreativeTabs;
import net.minecraft.common.block.*;
import net.minecraft.common.block.children.BlockFluid;
import net.minecraft.common.block.children.BlockLeavesBase;
import net.minecraft.common.block.data.Material;
import net.minecraft.common.block.data.MaterialLiquid;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.block.sound.StepSound;
import net.minecraft.common.block.sound.StepSounds;
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.item.Item;

import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.*;
import net.minecraft.common.item.children.ItemBucket;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.recipe.CraftingManager;
import net.minecraft.common.recipe.FurnaceRecipes;

import java.util.*;
import java.util.logging.Logger;

import static net.minecraft.common.block.Blocks.*;
import static net.minecraft.common.item.Items.*;

import static io.github.chlorojoules.gui.CJGuiGravity.*;
import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;
import static io.github.chlorojoules.CJRarityInfo.*;
import static io.github.chlorojoules.machine.CJMachineBuilder.*;
import static io.github.chlorojoules.machine.CJMachineRecipeTarget.*;
import static io.github.chlorojoules.block.CJBlockMachineBase.*;

public class CJMod extends Mod {
	public static Map<String, CJMachineBuilder> machines = new HashMap<>();

	// TODO: This can be removed in favour of `Fluid[s].java`.
	public static List<ItemBucket> buckets = new ArrayList<>();
	public static List<Integer> bucketFluids = new ArrayList<>();

	public static HashMap<String, ArrayList<Item>> tagList = new HashMap<>();

	public static Block fluidChlorojoules;
	public static Item bucketFluidChlorojoules;
	public static Block fluidPaste;
	public static Item bucketFluidPaste;
	public static Block fluidSouls;
	public static Item bucketFluidSouls;

	public static Block primitiveMachineFrame;
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
	public static Item pasteBowl;

	public static Item primalJewel;
	public static Item manufacturedJewel;
	public static Item refinedJewel;
	public static Item awakenedJewel;

	public static Item jewelDust;
	public static Item soulDust;
	public static Item soulEssence;
	public static Item stoneDust;
	public static Item ironDust;
	public static Item goldDust;
	public static Item soulCore;
	public static Item moss;

	public static Item soulExtractor;
	public static Item soulSword;
	public static Item linker;
	public static Item pruningShears;

	public static Item ironRod;

	public static int fuelFluid;
	public static int waterFluid;
	public static int lavaFluid;

	private static final CreativeTab fallbackTab =
			CreativeTabs.MECHANICAL_BLOCKS;

	public static CreativeTab creativeTab = fallbackTab;

	public static Class<?>[] objectArrayToTypes(Object[] objects) {
		Class<?>[] types = new Class<?>[objects.length];

		for(int i = 0; i < objects.length; ++i) {
			Class<?> type = objects[i].getClass();

			// Primitive handling.
			if(type == Boolean.class) type = Boolean.TYPE;
			if(type == Integer.class) type = Integer.TYPE;
			// MC type handling.
			else if(type == MaterialLiquid.class) type = Material.class;

			types[i] = type;
		}

		return types;
	}

	public static void addTagItem(String tag, Item item) {
		tagList.putIfAbsent(tag, new ArrayList<>());
		tagList.get(tag).add(item);
	}

	public static void addTagItem(String tag, int itemID) {
		addTagItem(tag, ITEMS_LIST[itemID]);
	}

	public static void addTagItem(String tag, Block block) {
		addTagItem(tag, block.getItemID());
	}

	public static boolean matchesTagItem(String tag, Item item) {
		return tagList.get(tag).contains(item);
	}

	public static boolean matchesTagItem(String tag, ItemStack stack) {
		return matchesTagItem(tag, stack.getItem());
	}

	public static void sendChat(String message) {
		Minecraft.getInstance().ingameGUI.addChatMessage(message);
	}

	public static boolean isGemId(int id) {
		return id == primalJewel.itemID ||
				id == manufacturedJewel.itemID ||
				id == refinedJewel.itemID ||
				id == awakenedJewel.itemID;
	}

	public Block registerMachine(
			String name, CJMachineBuilder builder, String[] iconNames,
			int sideMode) {

		builder.setMachineName(name);
		machines.put(name, builder);

		return new CJBlockMachineBase(
				name, builder, iconNames, sideMode)
				.setBlockName(name)
				.setCreativeTab(creativeTab)
				.addDescription(new CJItemDescriptionModTag())
				.setTooltipColor(getRarityColor(builder.rarity));
	}

	private Block registerFluid(String name) {
		Block ret = registerBlock(
				100.0f, 0.0f, StepSounds.SOUND_UNUSED, EnumTools.PICKAXE,
				BlockFluid.class, name, Materials.WATER, name, true)
				.disableStats()
				.setLightOpacity(1);

		return ret;
	}

	private Item registerFluidBucket(String name, int rarity, Block tile) {
		return registerItem(rarity, 1, ItemBucket.class, name, tile.blockID)
				.setContainerItem(EMPTY_BUCKET);
	}

	public void registerRecipe(ItemStack output, Object... params) {
		CraftingManager.getInstance(). addRecipe(output, params);
	}

	public void registerFurnaceRecipe(int output, int input) {
		FurnaceRecipes.instance. addSmelting(output, new ItemStack(input, 1));
	}

	public void registerFurnaceRecipe(Block output, Item input) {
		registerFurnaceRecipe(output.getItemID(), input.itemID);
	}

	public void registerFurnaceRecipe(Item output, Block input) {
		registerFurnaceRecipe(output.itemID, input.getItemID());
	}

	public void registerFurnaceRecipe(Item output, Item input) {
		registerFurnaceRecipe(output.itemID, input.itemID);
	}

	public Item registerItem(
			int rarity, int maxStack,
			Class<? extends Item> itemType, Object... args) {

		String name = (String) args[0];
		String desc = "message." + name + ".description";

		try {
			return itemType
					.getDeclaredConstructor(objectArrayToTypes(args))
					.newInstance(args)
					.setItemName(name)
					.setMaxStackSize(maxStack)
					.setTooltipColor(rarity)
					.addDescription(new CJItemDescriptionModTag())
					.setCreativeTab(creativeTab);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Item registerItem(String name, int rarity, int maxStack) {
		return registerItem(rarity, maxStack, Item.class, name);
	}

	public Item registerItem(String name, int rarity) {
		return registerItem(rarity, 64, Item.class, name);
	}

	public Block registerBlock(
			float hardness, float resistance,
			StepSound sound, EnumTools tool, Class<? extends Block> blockType,
			Object... args) {

		String name = (String) args[0];
		Material material = (Material) args[1];
		String desc = "message." + name + ".description";

		try {
			return blockType
					.getDeclaredConstructor(objectArrayToTypes(args))
					.newInstance(args)
					.setBlockName(name)
					.setHardness(hardness)
					.setResistance(resistance)
					.setSound(sound)
					.setEffectiveTool(tool)
					.addDescription(new CJItemDescriptionModTag())
					.setCreativeTab(creativeTab);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Block registerBlock(
			String name, Material material, float hardness, float resistance,
			StepSound sound, EnumTools tool) {

		return registerBlock(
				hardness, resistance, sound, tool, Block.class, name,
				material);
	}

	@Override
	public void onPreInit() {
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

		fluidChlorojoules = registerFluid("cj_fluid_chlorojoules");
		bucketFluidChlorojoules = registerFluidBucket(
				"cj_fluid_chlorojoules_bucket", MANUFACTURED_COLOR,
				fluidChlorojoules);

		fluidPaste = registerFluid("cj_fluid_paste");
		bucketFluidPaste = registerFluidBucket(
				"cj_fluid_paste_bucket", PRIMAL_COLOR, fluidPaste);

		fluidSouls = registerFluid("cj_fluid_souls");
		bucketFluidSouls = registerFluidBucket(
				"cj_fluid_souls_bucket", REFINED_COLOR, fluidSouls);

		fuelFluid = fluidChlorojoules.blockID;
		waterFluid = WATER_MOVING.blockID;
		lavaFluid = LAVA_MOVING.blockID;

		pasteBowl = registerItem(
				PRIMAL_COLOR, 1, CJItemPasteBowl.class, "cj_paste_bowl");

		paste = registerItem("cj_paste", PRIMAL_COLOR);
		jewelDust = registerItem("cj_jewel_dust", MANUFACTURED_COLOR);
		stoneDust = registerItem("cj_stone_dust", PRIMAL_COLOR);
		ironDust = registerItem("cj_iron_dust", PRIMAL_COLOR);
		goldDust = registerItem("cj_gold_dust", PRIMAL_COLOR);
		ironRod = registerItem("cj_iron_rod", PRIMAL_COLOR);
		moss = registerItem("cj_moss", PRIMAL_COLOR);
		soulDust = registerItem("cj_soul_dust", REFINED_COLOR);
		soulEssence = registerItem("cj_soul_essence", REFINED_COLOR);
		soulCore = registerItem("cj_soul_core", REFINED_COLOR, 1);

		primalJewel = registerItem(
				"cj_jewel_primal", PRIMAL_COLOR, 1);

		manufacturedJewel = registerItem(
				"cj_jewel_manufactured", MANUFACTURED_COLOR, 1);

		refinedJewel = registerItem(
				"cj_jewel_refined", REFINED_COLOR, 1);

		awakenedJewel = registerItem(
				"cj_jewel_awakened", AWAKENED_COLOR, 1);

		soulExtractor = registerItem(
				REFINED_COLOR, 1, CJItemSoulExtractor.class,
				"cj_soul_extractor");

		soulSword = registerItem(
				REFINED_COLOR, 1, CJItemToolSoulSword.class,
				"cj_soul_sword");

		linker = registerItem(
				MANUFACTURED_COLOR, 1, CJItemLinker.class, "cj_linker",
				new String[] {
						"cj_link_item_empty",
						"cj_link_item_full",
						"cj_link_fluid_empty",
						"cj_link_fluid_full",
						"cj_multi_item_empty",
						"cj_multi_item_full",
						"cj_multi_fluid_empty",
						"cj_multi_fluid_full" });

		pruningShears = registerItem(
				PRIMAL_COLOR, 1, CJItemToolPruningShears.class,
				"cj_pruning_shears");

		machineFrame = registerBlock(
				"cj_machine_frame", Materials.ROCK, 1.5F, 10.0F,
				StepSounds.SOUND_STONE, EnumTools.PICKAXE);

		primitiveMachineFrame = registerBlock(
				"cj_machine_frame_primitive", Materials.WOOD, 1.0F, 6.0F,
				StepSounds.SOUND_WOOD, EnumTools.AXE);

		compactedJewelDust = registerBlock(
				"cj_block_jewel_dust", Materials.SAND, 1.5F, 0.0F,
				StepSounds.SOUND_SAND, EnumTools.SHOVEL);

		// TODO: Feels like machines could be declared in JSON or smth (so too
		//       For all our registry here -- make our lives easier?)

		// TODO: Need a big machine UI fixup to make them more distinct and
		//       Improve alignment.

		// TODO: Make achievements to get ready for when they start working!

		bugBlock = registerMachine(
				"cj_bugblock", new CJMachineBuilder()
						.setRarity(CJRarity.AWAKENED)
						.addTank(
								15, 15, false, false, 4 * CJTank.BUCKET, 0)
						.addSlot(50, 35, false)
						.addSlot(75, 35, true)
						.setImpl(CJMachineBugBlock.class),
				null, ALL_FACES);

		cultivator = registerMachine(
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
												0, new ItemStack(SEEDS, 0)),
										new CJMachineRecipeComponent(
												1, new CJTankVolume(
														WATER_MOVING, 100)),
										new CJMachineRecipeComponent(
												1, new ItemStack(
														DYE_POWDER, 1, 15))
												.setOptional(true),
								},
								new CJMachineRecipeComponent[] {
										new CJMachineRecipeComponent(
												2, new ItemStack(WHEAT))
								},
								100, false, -1)
						.addRecipe(
								20,
								new CJMachineRecipeComponent[] {
										new CJMachineRecipeComponent(
												0, new ItemStack(
														MOSSY_COBBLESTONE, 0)),
										new CJMachineRecipeComponent(
												1, new CJTankVolume(
														WATER_MOVING, 100)),
										new CJMachineRecipeComponent(
												1, new ItemStack(
														DYE_POWDER, 1, 15))
												.setOptional(true),
								},
								new CJMachineRecipeComponent[] {
										new CJMachineRecipeComponent(
												2, new ItemStack(moss))
								},
								100, false, -1)
						.addProgressBarGravityVCenter(
								CENTER, (SLOT_IN_WIDTH * 4) / 3)
						.setImpl(CJMachineRecipeConsumer.class),
				null, FRONT_FACE);

		liquefier = registerMachine(
				"cj_liquefier", new CJMachineBuilder()
						.setRarity(CJRarity.PRIMAL)
						.addFuelTank()
						.addTankGravityVCenter(
								TOP_RIGHT, 10, true, false,
								8 * CJTank.BUCKET, 0)
						.addSlotGravity(CENTER, 0, 0, false)
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
										SLOT, 0, "#leaves", 1),
								new CJMachineRecipeComponent(
										1, new CJTankVolume(fluidPaste, 50)),
								50, true, -1)
						.setImpl(CJMachineRecipeConsumer.class),
				null, FRONT_FACE);

		refinery = registerMachine(
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
										1, new CJTankVolume(fluidPaste, 20)),
								new CJMachineRecipeComponent(
										2, new CJTankVolume(
												fluidChlorojoules, 25)),
								10, true, -1)
						.setImpl(CJMachineRecipeConsumer.class),
				null, FRONT_FACE);

		solidifier = registerMachine(
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
								manufacturedJewel.itemID)
						.addRecipe(
								20,
								new CJMachineRecipeComponent(
										1, new CJTankVolume(fluidPaste, 30)),
								new CJMachineRecipeComponent(
										0, new ItemStack(paste, 2)),
								150, true, -1)
						.addRecipe(
								100,
								new CJMachineRecipeComponent(
										0, new CJTankVolume(
												fluidChlorojoules, 900)),
								new CJMachineRecipeComponent(
										0, new ItemStack(manufacturedJewel)),
								650, false, 0)
						.setImpl(CJMachineRecipeConsumer.class),
				null, FRONT_FACE);

		// TODO: Secondary output.
		pulverizer = registerMachine(
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
										0, new ItemStack(primalJewel)),
								new CJMachineRecipeComponent(
										1, new ItemStack(jewelDust, 2)),
								100, false, -1)
						.addRecipe(
								10,
								new CJMachineRecipeComponent(
										0, new ItemStack(manufacturedJewel)),
								new CJMachineRecipeComponent(
										1, new ItemStack(jewelDust, 4)),
								200, false, -1)
						.addRecipe(
								10,
								new CJMachineRecipeComponent(
										0, new ItemStack(refinedJewel)),
								new CJMachineRecipeComponent(
										1, new ItemStack(jewelDust, 8)),
								250, false, -1)
						.addRecipe(
								10,
								new CJMachineRecipeComponent(
										0, new ItemStack(awakenedJewel)),
								new CJMachineRecipeComponent(
										1, new ItemStack(jewelDust, 16)),
								350, false, -1)
						// TODO: Need to handle more ore types.
						.addRecipe(
								30,
								new CJMachineRecipeComponent(
										SLOT, 0, "#iron_ore", 1),
								new CJMachineRecipeComponent(
										1, new ItemStack(ironDust, 2)),
								150, false, -1)
						.addRecipe(
								30,
								new CJMachineRecipeComponent(
										SLOT, 0, "#gold_ore", 1),
								new CJMachineRecipeComponent(
										1, new ItemStack(goldDust, 2)),
								150, false, -1)
						.addRecipe(
								10,
								new CJMachineRecipeComponent(
										0, new ItemStack(Items.SUGAR_CANE)),
								new CJMachineRecipeComponent(
										1, new ItemStack(SUGAR, 4)),
								150, false, -1)
						// TODO: Add dyes when we have damage values.
						/*.addRecipe(
								10,
								new CJMachineRecipeComponent(
										0, Item.bone, 1),
								new CJMachineRecipeComponent(
										1, Item.dyePowder, 3),
								150, false)*/
						.setImpl(CJMachineRecipeConsumer.class),
				null, FRONT_FACE);

		press = registerMachine(
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
								160,
								new CJMachineRecipeComponent(
										0, new ItemStack(jewelDust, 4)),
								new CJMachineRecipeComponent(
										1, new ItemStack(
												compactedJewelDust)),
								150, false, -1)
						.addRecipe(
								65,
								new CJMachineRecipeComponent(
										0, new ItemStack(IRON_INGOT)),
								new CJMachineRecipeComponent(
										1, new ItemStack(GEAR, 5)),
								75, false, -1)
						.setImpl(CJMachineRecipeConsumer.class),
				null, FRONT_FACE);

		furnace = registerMachine(
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
								500,
								new CJMachineRecipeComponent(
										0, new ItemStack(compactedJewelDust)),
								new CJMachineRecipeComponent(
										1, new ItemStack(refinedJewel)),
								300, CJRarity.MANUFACTURED, -1)
						.setImpl(CJMachineRecipeConsumer.class),
				null, FRONT_FACE);

		toolStation = registerMachine(
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
						.setImpl(CJMachineToolStation.class),
				null, ALL_SIDES);

		// TODO: For `Soul Extractor` -- make base tool then socket a
		//       `Refined ChloroJewel` to use; allows player to reclaim
		//       The jewel once they don't need the tool anymore.

		// TODO: Figure out how to make Gear controls.
		// TODO: UI to allow floopers to be filtered on one fluid kind.
		transferor = registerMachine(
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
						"cj_multi_flooper" }, ALL_FACES);

		tank = registerMachine(
				"cj_tank", new CJMachineBuilder()
						.addTankGravity(
								CENTER, 0, 0, false, true,
								16 * CJTank.BUCKET, 0),
				null, ALL_SIDES);

		mixer = registerMachine(
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
								manufacturedJewel.itemID)
						.addRecipe(
								150,
								new CJMachineRecipeComponent[] {
										new CJMachineRecipeComponent(
												0, new CJTankVolume(
														fluidChlorojoules,
														250)),
										new CJMachineRecipeComponent(
												0, new ItemStack(soulDust))
								},
								new CJMachineRecipeComponent[] {
										new CJMachineRecipeComponent(
												2, new CJTankVolume(
														fluidSouls, 50))
								},
								250, false, 0)
						.addRecipe(
								50,
								new CJMachineRecipeComponent[] {
										new CJMachineRecipeComponent(
												1, new CJTankVolume(
														WATER_MOVING, 250)),
										new CJMachineRecipeComponent(
												0, new ItemStack(moss))
								},
								new CJMachineRecipeComponent[] {
										new CJMachineRecipeComponent(
												2, new CJTankVolume(
														fluidChlorojoules,
														700))
								},
								300, false, -1)
						.setImpl(CJMachineRecipeConsumer.class),
				null, FRONT_FACE);
	}

	@Override
	public void onPostInit() {
		if(creativeTab != fallbackTab) {
			creativeTab.setTabIcon(new ItemStack(awakenedJewel));
		}

		ItemStack bowlStack = new ItemStack(BOWL);
		ItemStack pasteBowlStack = new ItemStack(pasteBowl);

		for(Block block : BLOCKS_LIST) {
			if(block instanceof BlockLeavesBase) {
				addTagItem("#leaves", block);

				CraftingManager.getInstance().addShapelessRecipe(
						pasteBowlStack, block, bowlStack);
			}
		}

		addTagItem("#iron_ore", IRON_ORE);
		addTagItem("#iron_ore", NETHER_IRON_ORE);

		addTagItem("#gold_ore", GOLD_ORE);
		addTagItem("#gold_ore", NETHER_GOLD_ORE);

		// TODO: A two way mapping between bucket/fluid IDs would probably be
		//       More efficient for lookup by `CJBlockMachineBase`.
		// Register fluids/buckets.
		for(Item item : ITEMS_LIST) {
			if(item == null) continue;
			if(!(item instanceof ItemBucket bucket)) continue;
			if(item == EMPTY_BUCKET) continue;

			buckets.add(bucket);

			int fluidID = bucket.getHeldLiquid();
			bucketFluids.add(fluidID);

			Logger.getLogger("ChloroJoules").info("Added bucket \"" +
					item.getItemName() + "\" (" + item.itemID + ") for " +
					"fluid \"" + BLOCKS_LIST[fluidID].getBlockName() +
					"\" (" + fluidID + ")");
		}

		// Recipe item stacks.
		ItemStack pasteStack = new ItemStack(paste);
		ItemStack primalJewelStack = new ItemStack(primalJewel);
		ItemStack machineFrameStack = new ItemStack(machineFrame);
		ItemStack machineFrame4Stack = new ItemStack(machineFrame, 4);
		ItemStack liquefierStack = new ItemStack(liquefier);
		ItemStack solidifierStack = new ItemStack(solidifier);
		ItemStack refineryStack = new ItemStack(refinery);
		ItemStack pulverizerStack = new ItemStack(pulverizer);
		ItemStack pressStack = new ItemStack(press);
		ItemStack toolStationStack = new ItemStack(toolStation);
		ItemStack transferor8Stack = new ItemStack(transferor, 8);
		ItemStack linkerStack = new ItemStack(linker);
		ItemStack ironRodStack = new ItemStack(ironRod);
		ItemStack furnaceStack = new ItemStack(FURNACE_IDLE);
		ItemStack poweredFurnaceStack = new ItemStack(furnace);
		ItemStack cobblestoneStack = new ItemStack(COBBLESTONE);
		ItemStack ironAxeStack = new ItemStack(IRON_AXE);
		ItemStack ironPickaxeStack = new ItemStack(IRON_PICKAXE);
		ItemStack ironShovelStack = new ItemStack(IRON_SHOVEL);
		ItemStack bucketStack = new ItemStack(EMPTY_BUCKET);
		ItemStack brickStack = new ItemStack(Items.BRICK);
		ItemStack ironBlockStack = new ItemStack(IRON_BLOCK);
		ItemStack diamondStack = new ItemStack(DIAMOND);
		ItemStack ironStack = new ItemStack(IRON_INGOT);
		ItemStack goldStack = new ItemStack(GOLD_INGOT);
		ItemStack flintStack = new ItemStack(FLINT);
		ItemStack gunpowderStack = new ItemStack(GUNPOWDER);
		ItemStack cauldronStack = new ItemStack(CAULDRON);
		ItemStack chestStack = new ItemStack(CHEST);
		ItemStack glassStack = new ItemStack(GLASS);
		ItemStack ashStack = new ItemStack(ASH);
		ItemStack coalStack = new ItemStack(COAL);
		ItemStack jewelDustStack = new ItemStack(jewelDust);
		ItemStack soulDust2Stack = new ItemStack(soulDust, 2);
		ItemStack soulEssenceStack = new ItemStack(soulEssence);
		ItemStack soulExtractorStack = new ItemStack(soulExtractor, 1, MAX_DAMAGE);
		ItemStack soulSwordStack = new ItemStack(soulSword, 1, MAX_DAMAGE);
		ItemStack mossStack = new ItemStack(moss);
		ItemStack soulCoreStack = new ItemStack(soulCore);
		ItemStack magmaStack = new ItemStack(MAGMA);
		ItemStack mossyCobblestoneStack =
				new ItemStack(MOSSY_COBBLESTONE);

		// Vanilla machine recipes.
		registerFurnaceRecipe(OAK_LEAVES, paste);
		registerFurnaceRecipe(goldDust, GOLD_INGOT);
		registerFurnaceRecipe(ironDust, IRON_INGOT);

		// Crafting recipes.
		registerRecipe(
				machineFrame4Stack,
				"%~%",
				"~|~",
				"% %",
				'%', cobblestoneStack,
				'~', pasteStack,
				'|', cauldronStack);

		CraftingManager.getInstance().addShapelessRecipe(
				mossyCobblestoneStack, cobblestoneStack, mossStack);

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

		// TODO: This is broken.
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

		registerRecipe(
				transferor8Stack,
				"&%&",
				"&|&",
				"&@&",
				'&', cobblestoneStack,
				'%', bucketStack,
				'@', chestStack,
				'|', machineFrameStack);

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

		registerRecipe(
				linkerStack,
				" %|",
				" @#",
				"@  ",
				'%', chestStack,
				'#', bucketStack,
				'|', pasteStack,
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
					new CJMachineRecipeComponent(
							0, new ItemStack(entry.getKey(), 1)),
					new CJMachineRecipeComponent(1, entry.getValue()),
					200, false, -1); // Vanilla furnace ticks as base.
		}
	}
}
