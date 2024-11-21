package io.github.chlorojoules.client;

import io.github.chlorojoules.CJInstance;

import io.github.chlorojoules.client.gui.CJGuiGravity;
import io.github.chlorojoules.client.machine.*;

import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.block.tileentity.TileEntity;
import net.minecraft.src.game.item.Item;

import com.fox2code.foxloader.loader.ClientMod;
import com.fox2code.foxloader.registry.*;
import net.minecraft.src.game.item.ItemBucket;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	public static RegisteredBlock bugBlock;
	public static RegisteredBlock liquefier;
	public static RegisteredBlock solidifier;
	public static RegisteredBlock flooper;
	public static RegisteredBlock whooper;

	public static RegisteredItem paste;

	public static RegisteredItem primalJewel;
	public static RegisteredItem manufacturedJewel;
	public static RegisteredItem refinedJewel;
	public static RegisteredItem awakenedJewel;

	public static int fuelFluid;

	public RegisteredBlock registerNewMachine(
			String name, CJMachineBuilder builder) {

		builder.setMachineName(name);
		machines.put(name, builder);

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
		try {
			callStaticMethod(
					TileEntity.class, "addMapping",
					CJTileEntityMachineBase.class, "cj_machine_base");
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}

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
		// Machines.
		{

			bugBlock = registerNewMachine(
					"cj_bugblock", new CJMachineBuilder()
							.setRarity(CJRarity.AWAKENED)
							.addTank(15, 15, false, 4 * CJTank.BUCKET, 0)
							.addSlot(50, 35, false)
							.addSlot(75, 35, true)
							.setImpl(new CJMachineBugBlock()));

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
											0, Block.leaves, 1),
									new CJMachineRecipeComponent(
											1, fluidPaste, 50)
											.setTarget(TANK),
									50)
							.setImpl(new CJMachineRecipeConsumer()));

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
							.addRecipe(
									20,
									new CJMachineRecipeComponent(1, fluidPaste, 30)
											.setTarget(TANK),
									new CJMachineRecipeComponent(0, paste, 2),
									150)
							.setImpl(new CJMachineRecipeConsumer()));

			// TODO: UI to allow floopers to be filtered on one fluid kind.
			flooper = registerNewMachine(
					"cj_flooper", new CJMachineBuilder()
							.addTankGravity(
									CENTER, 0, 0, false, 2 * CJTank.BUCKET, 0)
							.setImpl(new CJMachineFlooper()));

			whooper = registerNewMachine(
					"cj_whooper", new CJMachineBuilder()
							.setMachineName("cj_whooper")
							.addSlotGravity(CENTER, 0, 0, false)
							.setImpl(new CJMachineWhooper()));
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

		RegisteredItemStack liquefierStack =
				liquefier.newRegisteredItemStack();

		RegisteredItemStack solidifierStack =
				solidifier.newRegisteredItemStack();

		RegisteredItemStack flooper4Stack = flooper.newRegisteredItemStack();
		flooper4Stack.setRegisteredStackSize(4);

		RegisteredItemStack whooper4Stack = whooper.newRegisteredItemStack();
		whooper4Stack.setRegisteredStackSize(4);

		RegisteredItemStack diamondStack =
				Item.diamond.newRegisteredItemStack();

		RegisteredItemStack ironStack =
				Item.ingotIron.newRegisteredItemStack();

		RegisteredItemStack flintStack =
				Item.flint.newRegisteredItemStack();

		RegisteredItemStack cauldronStack =
				Block.cauldron.newRegisteredItemStack();

		RegisteredItemStack chestStack =
				Block.chest.newRegisteredItemStack();

		RegisteredItemStack ashStack = Item.ash.newRegisteredItemStack();

		// Vanilla machine recipes.
		registerFurnaceRecipe(Block.leaves.asRegisteredItem(), pasteStack);

		// Crafting recipes.
		registerRecipe(
				primalJewelStack,
				" ~ ",
				"~@~",
				" ~ ",
				'~', pasteStack,
				'@', diamondStack);

		registerRecipe(
				liquefierStack,
				"#~#",
				"~@~",
				"#|#",
				'#', ironStack,
				'~', pasteStack,
				'@', flintStack,
				'|', cauldronStack);

		registerRecipe(
				solidifierStack,
				"#@#",
				"@~@",
				"#|#",
				'#', ironStack,
				'~', pasteStack,
				'@', ashStack,
				'|', cauldronStack);

		registerRecipe(
				flooper4Stack,
				" ~ ",
				"~|~",
				" # ",
				'#', ironStack,
				'~', pasteStack,
				'|', cauldronStack);

		registerRecipe(
				whooper4Stack,
				" ~ ",
				"~|~",
				" # ",
				'#', ironStack,
				'~', pasteStack,
				'|', chestStack);
	}
}
