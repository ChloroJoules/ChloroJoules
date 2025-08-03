package io.github.chlorojoules;

import com.fox2code.foxloader.loader.Mod;
import com.fox2code.foxloader.registry.GameRegistry;

import com.google.gson.JsonObject;

import io.github.chlorojoules.block.CJBlockMachineBase;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.item.*;
import io.github.chlorojoules.machine.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.creative.CreativeTab;
import net.minecraft.client.gui.creative.CreativeTabs;

import net.minecraft.common.block.*;
import net.minecraft.common.block.children.*;
import net.minecraft.common.block.data.Material;
import net.minecraft.common.block.data.MaterialLiquid;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.block.fluid.Fluid;
import net.minecraft.common.block.fluid.Fluids;
import net.minecraft.common.block.sound.StepSound;
import net.minecraft.common.block.sound.StepSounds;
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.item.*;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.recipe.*;
import net.minecraft.common.util.JsonUtils;

import java.util.*;

import static net.minecraft.common.block.Blocks.*;
import static net.minecraft.common.item.Items.*;

import static io.github.chlorojoules.CJRarityInfo.*;

public class CJMod extends Mod {
	public static Map<String, CJBlockMachineBase> machines = new HashMap<>();

	// NOTE: This is turbo stupid but there doesn't seem to be a way
	//		 To iterate *just* the Vanilla mod container.
	//		 This is not guaranteed to be full -- and honestly we don't want
	//		 It to be -- but should contain all Vanilla items mapped from their
	//		 Display name translation key to their implementation.
	public static HashMap<String, Item> earlyItemMap = new HashMap<>();

	public static TaggedIngredient tagLeaves =
			TaggedIngredients.get("cj_leaves");

	public static TaggedIngredient tagLogs = TaggedIngredients.get("cj_logs");
	public static TaggedIngredient tagFlower =
			TaggedIngredients.get("cj_flowers");

	public static TaggedIngredient tagGlass =
			TaggedIngredients.get("cj_glass");

	public static TaggedIngredient tagRawStone =
			TaggedIngredients.get("cj_raw_stone");

	public static TaggedIngredient tagEmptyBucket =
			TaggedIngredients.get("cj_empty_bucket");

	public static TaggedIngredient tagIronOre =
			TaggedIngredients.get("cj_iron_ore");

	public static TaggedIngredient tagGoldOre =
			TaggedIngredients.get("cj_gold_ore");

	public static TaggedIngredient tagCompostable =
			TaggedIngredients.get("cj_compostable");

	public static TaggedIngredient tagJewel =
			TaggedIngredients.get("cj_jewel");

	public static Block fluidChlorojoules;
	public static Item bucketFluidChlorojoules;
	public static Block fluidPaste;
	public static Item bucketFluidPaste;
	public static Block fluidSouls;
	public static Item bucketFluidSouls;
	public static Block fluidDimension;
	public static Item bucketFluidDimension;
	public static Block fluidEtching;
	public static Item bucketFluidEtching;

	public static Block primitiveMachineFrame;
	public static Block machineFrame;
	public static Block compactedJewelDust;

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
	public static Block composter;
	public static Block primitiveCentrifuge;
	public static Block enervator;
	public static Block pump;
	public static Block reactor;
	public static Block injector;

	public static Item paste;
	public static Item pasteBowl;
	public static Item dirtBowl;

	public static Item blender;

	public static Item fauxJewel;
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
	public static Item tinyStoneDust;
	public static Item tinyIronDust;
	public static Item tinyGoldDust;
	public static Item soulCore;
	public static Item moss;

	public static Item soulExtractor;
	public static Item soulSword;
	public static Item linker;
	public static Item pruningShears;
	public static Item sundial;
	public static Item bloodInjector;
	public static Item fluidContainer;

	public static Item ironRod;

	public static int fuelFluid;
	public static int waterFluid;
	public static int lavaFluid;

	public static CreativeTab creativeTab = CreativeTabs.MECHANICAL_BLOCKS;

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

	public static Block fluidFromName(String name) {
		Block block = GameRegistry.getRegisteredBlock(name);
		if(block != null) return block;

		for(Fluid fluid : Fluids.getFluids()) {
			if(fluid.getName().equals(name)) return fluid.getMoving();
		}

		throw new RuntimeException("No such fluid '" + name + "'");
	}

	public static void sendChat(String message) {
		Minecraft.getInstance().ingameGUI.addChatMessage(message);
	}

	public static boolean matchIngredientLenient(
			ItemStack value, Ingredient match) {

		// We don't want to discard all damage value differences.
		if(match instanceof ItemStack matchStack) {
			if(matchStack.getItemID() == value.getItemID() &&
					(matchStack.getItemDamage() ==
							value.getItemDamage() ||
							!matchStack.getHasSubtypes())) {

				return true;
			}
		}

		return match.matchIngredient(value);
	}

	public static Ingredient ingredientFromJson(JsonObject jsonObject) {
		String key = JsonUtils.getString(jsonObject, "item");

		if(key.startsWith("#")) {
			String tag = key.substring(1);
			return TaggedIngredients.get(tag);
		}

		ItemStack result;

		Item item = GameRegistry.getRegisteredItem(key);
		if(item == null) {
			Block block = GameRegistry.getRegisteredBlock(key);
			if(block == null) {
				result = new ItemStack(earlyItemMap.get("item." + key));
			}
			else result = new ItemStack(block);
		}
		else result = new ItemStack(item);

		if(jsonObject.has("amount")) {
			result.stackSize = JsonUtils.getInt(jsonObject, "amount");
		}

		if(jsonObject.has("damage")) {
			result.setItemDamage(JsonUtils.getInt(jsonObject, "damage"));
		}

		return result;
	}

	public Block registerMachine(CJMachineBuilder builder) {
		Block result = new CJBlockMachineBase(builder)
				.setCreativeTab(creativeTab);

		machines.put(builder.name, (CJBlockMachineBase) result);

		return result;
	}

	private Block registerFluid(String name) {
		return registerBlock(
				100.0f, 0.0f, StepSounds.SOUND_UNUSED, EnumTools.PICKAXE,
				BlockFluid.class, name, Materials.WATER, name, true)
				.disableStats()
				.setLightOpacity(1);
	}

	private Item registerFluidBucket(String name, int rarity, Block tile) {
		registerItem(
				rarity, 1, CJItemGoldenBucket.class, name + "_gold",
				tile.blockID)
				.setContainerItem(GOLDEN_EMPTY_BUCKET);

		return registerItem(rarity, 1, CJItemBucket.class, name, tile.blockID)
				.setContainerItem(EMPTY_BUCKET);
	}

	public void registerRecipe(ItemStack output, Object... params) {
		CraftingManager.getInstance().addRecipe(output, params);
	}

	public void registerRecipe(Item output, Object... params) {
		registerRecipe(new ItemStack(output), params);
	}

	public void registerRecipe(Block output, Object... params) {
		registerRecipe(new ItemStack(output), params);
	}

	public void registerShapelessRecipe(ItemStack output, Object... params) {
		CraftingManager.getInstance().addShapelessRecipe(output, params);
	}

	public void registerShapelessRecipe(Item output, Object... params) {
		registerShapelessRecipe(new ItemStack(output), params);
	}

	public void registerShapelessRecipe(Block output, Object... params) {
		registerShapelessRecipe(new ItemStack(output), params);
	}

	public void registerFurnaceRecipe(int output, int input) {
		FurnaceRecipes.instance.addSmelting(output, new ItemStack(input, 1));
	}

	public void registerFurnaceRecipe(Item output, Item input) {
		registerFurnaceRecipe(output.itemID, input.itemID);
	}

	public Item registerItem(
			int rarity, int maxStack,
			Class<? extends Item> itemType, Object... args) {

		String name = (String) args[0];
		//String desc = "message." + name + ".description";

		try {
			return itemType
					.getDeclaredConstructor(objectArrayToTypes(args))
					.newInstance(args)
					.setItemName(name)
					.setMaxStackSize(maxStack)
					.setTooltipColor(rarity)
					.setCreativeTab(creativeTab);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
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
		//String desc = "message." + name + ".description";

		try {
			return blockType
					.getDeclaredConstructor(objectArrayToTypes(args))
					.newInstance(args)
					.setBlockName(name)
					.setHardness(hardness)
					.setResistance(resistance)
					.setSound(sound)
					.setEffectiveTool(tool)
					//.addDescription(new CJItemDescriptionModTag())
					.setCreativeTab(creativeTab);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
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
		for(Item item : ITEMS_LIST) {
			if(item == null) continue;

			String name = item.getItemName();
			if(name == null) continue;

			earlyItemMap.putIfAbsent(name.replace("tile.", "item."), item);
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

		fluidDimension = registerFluid("cj_fluid_dimension");
		bucketFluidDimension = registerFluidBucket(
				"cj_fluid_dimension_bucket", MANUFACTURED_COLOR,
				fluidDimension);

		fluidEtching = registerFluid("cj_fluid_etching");
		bucketFluidEtching = registerFluidBucket(
				"cj_fluid_etching_bucket", MANUFACTURED_COLOR, fluidEtching);

		fuelFluid = fluidChlorojoules.blockID;
		waterFluid = WATER_MOVING.blockID;
		lavaFluid = LAVA_MOVING.blockID;

		paste = registerItem("cj_paste", PRIMAL_COLOR);
		jewelDust = registerItem("cj_jewel_dust", MANUFACTURED_COLOR);
		stoneDust = registerItem("cj_stone_dust", PRIMAL_COLOR);
		ironDust = registerItem("cj_iron_dust", PRIMAL_COLOR);
		goldDust = registerItem("cj_gold_dust", PRIMAL_COLOR);
		tinyStoneDust = registerItem("cj_tiny_stone_dust", PRIMAL_COLOR);
		tinyIronDust = registerItem("cj_tiny_iron_dust", PRIMAL_COLOR);
		tinyGoldDust = registerItem("cj_tiny_gold_dust", PRIMAL_COLOR);
		ironRod = registerItem("cj_iron_rod", PRIMAL_COLOR);
		moss = registerItem("cj_moss", PRIMAL_COLOR);
		soulDust = registerItem("cj_soul_dust", REFINED_COLOR);
		soulEssence = registerItem("cj_soul_essence", REFINED_COLOR);
		soulCore = registerItem("cj_soul_core", REFINED_COLOR, 1);

		pasteBowl = registerItem(
				PRIMAL_COLOR, 1, CJItemConvertBowl.class, "cj_paste_bowl",
				new ItemStack(paste));

		dirtBowl = registerItem(
				PRIMAL_COLOR, 1, CJItemConvertBowl.class, "cj_dirt_bowl",
				new ItemStack(stoneDust));

		blender = registerItem("cj_blender", PRIMAL_COLOR, 1)
				.setDoNotConsumeOnCrafting(true);

		fauxJewel = registerItem(
				"cj_jewel_faux", PRIMAL_COLOR, 1)
				.setMaxDamage(100);

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

		sundial = registerItem("cj_sundial", REFINED_COLOR, 1);

		bloodInjector = registerItem(
				REFINED_COLOR, 1, CJItemBloodInjector.class,
				"cj_blood_injector");

		fluidContainer = registerItem(
				REFINED_COLOR, 1, CJItemFluidContainer.class,
				"cj_fluid_container")
				.addDescription(new CJItemDescriptionFluidContainer());

		machineFrame = registerBlock(
				"cj_machine_frame", Materials.ROCK, 1.5F, 10.0F,
				StepSounds.SOUND_STONE, EnumTools.PICKAXE);

		primitiveMachineFrame = registerBlock(
				"cj_machine_frame_primitive", Materials.WOOD, 1.0F, 6.0F,
				StepSounds.SOUND_WOOD, EnumTools.AXE);

		compactedJewelDust = registerBlock(
				"cj_block_jewel_dust", Materials.SAND, 1.5F, 0.0F,
				StepSounds.SOUND_SAND, EnumTools.SHOVEL);

		// TODO: Make achievements to get ready for when they start working!

		cultivator = registerMachine(
				new CJMachineBuilder("/machines/cj_cultivator.json"));

		liquefier = registerMachine(
				new CJMachineBuilder("/machines/cj_liquefier.json"));

		refinery = registerMachine(
				new CJMachineBuilder("/machines/cj_refinery.json"));

		solidifier = registerMachine(
				new CJMachineBuilder("/machines/cj_solidifier.json"));

		pulverizer = registerMachine(
				new CJMachineBuilder("/machines/cj_pulverizer.json"));

		press = registerMachine(
				new CJMachineBuilder("/machines/cj_press.json"));

		furnace = registerMachine(
				new CJMachineBuilder("/machines/cj_furnace.json"));

		toolStation = registerMachine(
				new CJMachineBuilder("/machines/cj_tool_station.json"));

		// TODO: Figure out how to make Gear controls.
		// TODO: Filtered transferors.
		transferor = registerMachine(
				new CJMachineBuilder("/machines/cj_transferor.json"));

		tank = registerMachine(
				new CJMachineBuilder("/machines/cj_tank.json"));

		composter = registerMachine(
				new CJMachineBuilder("/machines/cj_composter.json"));

		primitiveCentrifuge = registerMachine(
				new CJMachineBuilder(
						"/machines/cj_primitive_centrifuge.json"));

		mixer = registerMachine(
				new CJMachineBuilder("/machines/cj_mixer.json"));

		enervator = registerMachine(
				new CJMachineBuilder("/machines/cj_enervator.json"));

		pump = registerMachine(
				new CJMachineBuilder("/machines/cj_pump.json"));

		reactor = registerMachine(
				new CJMachineBuilder("/machines/cj_reactor.json"));

		injector = registerMachine(
				new CJMachineBuilder("/machines/cj_injector.json"));
	}

	@Override
	public void onPostInit() {
		for(Block block : BLOCKS_LIST) {
			if(block instanceof BlockLeavesBase) {
				tagLeaves.addIngredient(block);
				tagCompostable.addIngredient(block);
			}

			if(block instanceof BlockBasicPlant) {
				tagCompostable.addIngredient(block);
			}

			if(block instanceof BlockLog) {
				tagLogs.addIngredient(block);
			}

			if(block instanceof BlockFlower) {
				tagFlower.addIngredient(block);
			}

			if(block instanceof BlockGlass) {
				tagGlass.addIngredient(block);
			}
		}

		tagIronOre.addIngredient(IRON_ORE);
		tagIronOre.addIngredient(NETHER_IRON_ORE);

		tagGoldOre.addIngredient(GOLD_ORE);
		tagGoldOre.addIngredient(NETHER_GOLD_ORE);

		tagJewel.addIngredient(fauxJewel);
		tagJewel.addIngredient(primalJewel);
		tagJewel.addIngredient(manufacturedJewel);
		tagJewel.addIngredient(refinedJewel);
		tagJewel.addIngredient(awakenedJewel);

		tagEmptyBucket.addIngredient(EMPTY_BUCKET);
		tagEmptyBucket.addIngredient(GOLDEN_EMPTY_BUCKET);

		tagRawStone.addIngredient(STONE);
		tagRawStone.addIngredient(COBBLESTONE);
		tagRawStone.addIngredient(MOSSY_COBBLESTONE);
		tagRawStone.addIngredient(LIMESTONE);
		tagRawStone.addIngredient(CLOUDSTONE);
		tagRawStone.addIngredient(BRIMSTONE);

		registerFurnaceRecipe(goldDust, GOLD_INGOT);
		registerFurnaceRecipe(ironDust, IRON_INGOT);

		for(Ingredient ingredient : tagLeaves.getIngredients()) {
			ItemStack stack = (ItemStack) ingredient;

			registerFurnaceRecipe(stack.getItem(), paste);
		}

		ItemStack machineFrame4Stack = new ItemStack(machineFrame, 4);
		ItemStack transferor8Stack = new ItemStack(transferor, 8);
		ItemStack soulDust2Stack = new ItemStack(soulDust, 2);
		ItemStack soulSwordStack = new ItemStack(soulSword, 1, MAX_DAMAGE);
		ItemStack soulExtractorStack = new ItemStack(
				soulExtractor, 1, MAX_DAMAGE);

		registerShapelessRecipe(pasteBowl, tagLeaves, BOWL);
		registerRecipe(
				composter,
				"%%%",
				"#|#",
				"%%%",
				'%', TaggedIngredients.WOODEN_PLANKS,
				'|', primitiveMachineFrame,
				'#', tagLeaves);

		registerRecipe(
				pruningShears,
				"# #",
				" # ",
				"/ /",
				'/', STICK,
				'#', TaggedIngredients.WOODEN_PLANKS);

		registerRecipe(
				primitiveCentrifuge,
				"###",
				"/|/",
				"#^#",
				'/', STICK,
				'#', TaggedIngredients.WOODEN_PLANKS,
				'|', primitiveMachineFrame,
				'^', FLINT);

		registerRecipe(
				primitiveMachineFrame,
				"%|%",
				"%~%",
				"%#%",
				'%', TaggedIngredients.WOODEN_PLANKS,
				'~', paste,
				'|', STICK,
				'#', tagLogs);

		registerShapelessRecipe(MOSSY_COBBLESTONE, COBBLESTONE, moss);
		registerShapelessRecipe(soulDust2Stack, jewelDust, soulEssence);
		registerShapelessRecipe(dirtBowl, DIRT, BOWL);
		registerShapelessRecipe(COBBLESTONE, DIRT, stoneDust);
		registerShapelessRecipe(sundial, CLOCK, GLOWSTONE_DUST);

		registerRecipe(
				GRAVEL,
				"%%",
				"%%",
				'%', stoneDust);

		registerRecipe(
				stoneDust,
				"%%",
				"%%",
				'%', tinyStoneDust);

		registerRecipe(
				ironDust,
				"%%",
				"%%",
				'%', tinyIronDust);

		registerRecipe(
				goldDust,
				"%%",
				"%%",
				'%', tinyGoldDust);

		registerRecipe(
				blender,
				" % ",
				"%~%",
				"%%%",
				'%', tagRawStone,
				'~', IRON_INGOT);

		registerShapelessRecipe(GRAVEL, blender, tagRawStone);
		registerShapelessRecipe(SAND, blender, GRAVEL);

		registerShapelessRecipe(
				new ItemStack(tinyIronDust, 6), blender, tagIronOre);

		registerShapelessRecipe(
				new ItemStack(tinyGoldDust, 6), blender, tagGoldOre);

		registerRecipe(
				machineFrame4Stack,
				"%~%",
				"~|~",
				"% %",
				'%', tagRawStone,
				'~', paste,
				'|', tagEmptyBucket);

		registerRecipe(
				ironRod,
				"  #",
				" # ",
				"#  ",
				'#', IRON_INGOT);

		registerRecipe(
				primalJewel,
				" ~ ",
				"~@~",
				" ~ ",
				'~', paste,
				'@', DIAMOND);

		registerRecipe(
				fauxJewel,
				"#~#",
				"~@~",
				"#~#",
				'#', ASH,
				'~', paste,
				'@', IRON_INGOT);

		registerRecipe(
				liquefier,
				"@@@",
				"&|&",
				"&%&",
				'&', tagRawStone,
				'%', FURNACE_IDLE,
				'@', FLINT,
				'|', machineFrame);

		registerRecipe(
				solidifier,
				"&@&",
				"@|@",
				"&%&",
				'&', COBBLESTONE,
				'@', ASH,
				'%', COAL,
				'|', machineFrame);

		registerRecipe(
				enervator,
				"&#&",
				"*|*",
				"&&&",
				'&', tagRawStone,
				'*', tagFlower,
				'#', tagGlass,
				'|', machineFrame);

		registerRecipe(
				reactor,
				"&@&",
				"*|*",
				"&&&",
				'&', tagRawStone,
				'*', tagEmptyBucket,
				'#', tagGlass,
				'@', BOTTLE,
				'|', machineFrame);

		registerRecipe(
				pump,
				"&&&",
				"~|~",
				"&&&",
				'&', tagRawStone,
				'|', machineFrame,
				'~', tagEmptyBucket);

		registerRecipe(
				refinery,
				"&%&",
				"@|@",
				"&&&",
				'&', tagRawStone,
				'@', tagGlass,
				'%', tagEmptyBucket,
				'|', machineFrame);

		registerRecipe(
				pulverizer,
				"&@&",
				"@|@",
				"&%&",
				'&', tagRawStone,
				'@', FLINT,
				'%', GUNPOWDER,
				'|', machineFrame);

		registerRecipe(
				press,
				"&@&",
				"%|%",
				"&%&",
				'&', tagRawStone,
				'@', IRON_BLOCK,
				'%', Items.BRICK,
				'|', machineFrame);

		registerRecipe(
				furnace,
				"&&&",
				"@|@",
				"%&%",
				'&', tagRawStone,
				'@', COAL,
				'%', FURNACE_IDLE,
				'|', machineFrame);

		registerRecipe(
				toolStation,
				"&&&",
				"a|c",
				"%b%",
				'&', tagRawStone,
				'a', IRON_AXE,
				'b', IRON_PICKAXE,
				'c', IRON_SHOVEL,
				'|', machineFrame);

		registerRecipe(
				transferor8Stack,
				"&%&",
				"&|&",
				"&@&",
				'&', tagRawStone,
				'%', EMPTY_BUCKET,
				'@', CHEST,
				'|', machineFrame);

		registerRecipe(
				mixer,
				"&#&",
				"&|&",
				"&@&",
				'&', tagRawStone,
				'@', BOWL,
				'#', WOOD_SHOVEL,
				'|', machineFrame);

		registerRecipe(
				tank,
				"#&#",
				"#|#",
				"#&#",
				'&', tagRawStone,
				'#', GLASS,
				'|', machineFrame);

		registerRecipe(
				soulSwordStack,
				"  #",
				" % ",
				"@  ",
				'%', soulCore,
				'#', MAGMA,
				'@', ironRod);

		registerRecipe(
				soulExtractorStack,
				" %#",
				" @%",
				"@  ",
				'%', paste,
				'#', IRON_INGOT,
				'@', ironRod);

		registerRecipe(
				linker,
				" %|",
				" @#",
				"@  ",
				'%', CHEST,
				'#', EMPTY_BUCKET,
				'|', paste,
				'@', ironRod);

		// Consume furnace recipes.
		FurnaceRecipes furnaceRecipes = FurnaceRecipes.instance;
		Map<Integer, ItemStack> furnaceMap =
				furnaceRecipes.getSmeltingList();

		Set<Map.Entry<Integer, ItemStack>> furnaceEntries =
				furnaceMap.entrySet();

		CJBlockMachineBase furnaceMachine = machines.get("cj_furnace");
		CJMachineBuilder furnaceMachineBuilder = furnaceMachine.machineBuilder;

		for(Map.Entry<Integer, ItemStack> entry : furnaceEntries) {
			furnaceMachineBuilder.recipes.add(new CJMachineRecipe()
					.addInput(new CJMachineRecipeComponent(
							"fuel", new CJTankVolume(fluidChlorojoules, 70)))
					.addInput(new CJMachineRecipeComponent(
							"input", new ItemStack(entry.getKey(), 1)))
					.addOutput(new CJMachineRecipeComponent(
							"output", entry.getValue()))
					.setProcessTime(200));
		}
	}
}
