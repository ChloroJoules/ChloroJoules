package io.github.chlorojoules.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chlorojoules.*;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.*;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.JsonUtils;
import net.minecraft.common.util.i18n.StringTranslate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Logger;

import static io.github.chlorojoules.CJRarityInfo.raritySufficient;
import static io.github.chlorojoules.gui.CJGuiGravity.*;
import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJMachineBuilder {
	public static final int FUEL_TANK_SIZE = 4 * CJTank.BUCKET;

	public String name = null;
	public CJRarity rarity = CJRarity.MANUFACTURED;
	public Class<? extends CJIMachine> machineImpl =
			CJMachineRecipeConsumer.class;

	public boolean doDropMeta = false;

	public int fuelTankIndex = -1;
	public int jewelSlotIndex = -1;

	public ArrayList<CJMachineSlotInfo> slots = new ArrayList<>();
	public ArrayList<CJTank> tanks = new ArrayList<>();
	public ArrayList<CJTankVolume> tankVolumes = new ArrayList<>();
	public CJGuiElement progressBar = null;
	public ArrayList<CJGuiButton> buttons = new ArrayList<>();
	// TODO: This is kind of hardcoded -- is there a way we can make
	//  	 `CJGuiElement` more generic/programmable?
	public ArrayList<CJGuiCoordinate> linkCoordinates = new ArrayList<>();

	public ArrayList<CJMachineRecipe> recipes = new ArrayList<>();

	public String[] iconNames = null;
	public boolean iconDefault = true;
	public CJMachineBlockSideMode sideMode = CJMachineBlockSideMode.FRONT_FACE;
	public CJMachineTier tier = CJMachineTier.INDUSTRIAL;

	public CJMachineBuilder() {}

	@SuppressWarnings("unchecked")
	public CJMachineBuilder(String jsonPath) {
		String source;
		try {
			InputStream stream = getClass().getResourceAsStream(jsonPath);
			if(stream == null) {
				throw new RuntimeException(
						"Failed to open resource stream for '" +
						jsonPath + "'");
			}
			ByteBuffer bytes = ByteBuffer.wrap(stream.readAllBytes());
			source = StandardCharsets.UTF_8.decode(bytes).toString();
			stream.close();
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}

		JsonObject root = JsonParser.parseString(source).getAsJsonObject();

		name = JsonUtils.getString(root, "name");
		rarity = CJRarityInfo.fromString(
				JsonUtils.getString(root, "rarity"));

		if(root.has("tier")) {
			tier = CJMachineTier.fromString(
					JsonUtils.getString(root, "tier"));
		}

		if(root.has("face")) {
			sideMode = CJMachineBlockSideMode.fromString(
					JsonUtils.getString(root, "face"));
		}

		if(root.has("implementation")) {
			JsonElement implElement = root.get("implementation");
			if(implElement.isJsonNull()) machineImpl = null;
			else {
				String implName = JsonUtils.getString(root, "implementation");
				try {
					Class<?> implClass = Class.forName(implName);
					if(!CJIMachine.class.isAssignableFrom(implClass)) {
						throw new RuntimeException(
								"Implementation class '" + implName +
								"' does not implement 'CJIMachine'");
					}

					machineImpl = (Class<? extends CJIMachine>) implClass;
				}
				catch(ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}

		if(root.has("icons")) {
			JsonArray icons = JsonUtils.getJsonArray(root, "icons");
			iconDefault = false;
			iconNames = new String[icons.size()];

			for(int i = 0; i < icons.size(); ++i) {
				iconNames[i] = icons.get(i).getAsString();
			}
		}
		else iconNames = new String[] { name };

		if(root.has("tanks")) {
			for(JsonElement tank : JsonUtils.getJsonArray(root, "tanks")) {
				if(tank.isJsonObject()) {
					JsonObject tankObject = tank.getAsJsonObject();
					tanks.add(new CJTank(tankObject));
					tankVolumes.add(new CJTankVolume(tankObject));
					continue;
				}

				String special = tank.getAsString();
				if(special.equals("fuel")) {
					if(fuelTankIndex != -1) {
						throw new RuntimeException(
								"Cannot specify multiple fuel tanks");
					}

					fuelTankIndex = tanks.size();

					tanks.add((CJTank) new CJTank(LEFT, FUEL_TANK_INSET, 0)
							.setID("fuel"));

					tankVolumes.add(new CJTankVolume()
							.setMax(FUEL_TANK_SIZE)
							.setLockFluid(CJMod.fuelFluid));
				}
				else {
					Logger.getLogger("Chlorojoules").warning(
							"Unknown tank constant '`'" + special + "'");
				}
			}
		}

		if(root.has("slots")) {
			for(JsonElement slot : JsonUtils.getJsonArray(root, "slots")) {
				if(slot.isJsonObject()) {
					JsonObject slotObject = slot.getAsJsonObject();
					slots.add(new CJMachineSlotInfo(slotObject));
					continue;
				}

				String special = slot.getAsString();
				if(special.equals("jewel")) {
					if(jewelSlotIndex != -1) {
						throw new RuntimeException(
								"Cannot specify multiple jewel slots");
					}

					jewelSlotIndex = slots.size();

					slots.add((CJMachineSlotInfo) new CJMachineSlotInfo(
							JEWEL_SLOT_INSET_X, JEWEL_SLOT_INSET_Y)
							.setGravity(BOTTOM_LEFT)
							.setRenderType(CJMachineSlotRenderType.JEWEL)
							.setAllowedItems(new ItemStack[] {
									new ItemStack(CJMod.fauxJewel),
									new ItemStack(CJMod.primalJewel),
									new ItemStack(CJMod.manufacturedJewel),
									new ItemStack(CJMod.refinedJewel),
									new ItemStack(CJMod.awakenedJewel)
							})
							.setID("jewel"));
				}
				else {
					Logger.getLogger("Chlorojoules").warning(
							"Unknown slot constant '`'" + special + "'");
				}
			}
		}

		if(root.has("buttons")) {
			for(JsonElement button : JsonUtils.getJsonArray(root, "buttons")) {
				buttons.add(new CJGuiButton(button.getAsJsonObject()));
			}
		}

		if(root.has("coordinates")) {
			for(JsonElement coordinate :
					JsonUtils.getJsonArray(root, "coordinates")) {

				linkCoordinates.add(new CJGuiCoordinate(
						coordinate.getAsJsonObject()));
			}
		}

		if(root.has("progress")) {
			JsonObject progressObject =
					JsonUtils.getJsonObject(root, "progress");

			progressBar = new CJGuiElement(progressObject)
					.setSize(PROGRESS_WIDTH, PROGRESS_HEIGHT);
		}

		if(root.has("recipes")) {
			for(JsonElement recipe : JsonUtils.getJsonArray(root, "recipes")) {
				recipes.add(new CJMachineRecipe(
						this, recipe.getAsJsonObject()));
			}
		}
	}

	public int getSlotIndex(String id) {
		for(int i = 0; i < slots.size(); i++) {
			CJMachineSlotInfo slot = slots.get(i);
			if(slot.id == null) continue;
			if(slot.id.equals(id)) return i;
		}

		return -1;
	}

	public int getNamedDamage(String id) {
		if(iconDefault) return 0;

		for(int i = 0; i < iconNames.length; i++) {
			if(iconNames[i].equals(id)) return i;
		}

		return 0;
	}

	public int getButtonIndex(String id) {
		for(int i = 0; i < buttons.size(); i++) {
			CJGuiButton button = buttons.get(i);
			if(button.id == null) continue;
			if(button.id.equals(id)) return i;
		}

		return -1;
	}

	public int getTankIndex(String id) {
		for(int i = 0; i < tanks.size(); i++) {
			CJTank tank = tanks.get(i);
			if(tank.id == null) continue;
			if(tank.id.equals(id)) return i;
		}

		return -1;
	}

	public CJMachineBuilder setName(String value) {
		iconNames = new String[] { value };
		name = value;
		return this;
	}

	public CJMachineBuilder setRarity(CJRarity value) {
		rarity = value;
		return this;
	}

	// TODO: Output components don't verify that there is space left.
	private boolean componentMatch(
			CJTileEntityMachineBase entity,
			CJMachineRecipeComponent component, boolean input) {

		if(component.optional) return false;

		if(component.target == CJMachineRecipeTarget.TANK) {
			CJTankVolume volume = entity.tanks.get(component.index);

			if(volume.fluidID == 0) return input;

			return volume.fluidID != component.volume.fluidID;
		}
		else {
			ItemStack stack = entity.getStackInSlot(component.index);
			if(stack == null) return input;

			if(component.isTag) {
				return !CJMod.matchesTagItem(component.tag, stack);
			}

			return stack.getItemID() != component.stack.getItemID() ||
					stack.getItemDamage() != component.stack.getItemDamage();
		}
	}

	public CJMachineRecipe getMatchingRecipe(CJTileEntityMachineBase entity) {
		entity.errorMessage = null;
		entity.isWarning = false;
		entity.isPassive = false;

		StringTranslate translate = StringTranslate.getInstance();
		CJMachineRecipe recipe = null;

		entity.jewelRarity = CJRarity.MANUFACTURED;
		if(jewelSlotIndex != -1) {
			ItemStack jewel = entity.getStackInSlot(jewelSlotIndex);

			if(jewel == null) {
				entity.errorMessage =
						translate.translateKey("message.cj_no_jewel");

				return null;
			}

			entity.jewelRarity =
					CJRarityInfo.getJewelRarity(jewel.getItemID());

			if(entity.jewelRarity == CJRarity.INVALID) {
				entity.errorMessage =
						translate.translateKey("message.cj_bad_jewel");

				return null;
			}
		}

		boolean matchedRecipe = false;
		for(CJMachineRecipe machineRecipe : recipes) {
			matchedRecipe = true;
			recipe = machineRecipe;

			if(recipe.requiredButton != -1) {
				if(!entity.buttonStates.get(recipe.requiredButton)) {
					matchedRecipe = false;
				}
			}

			for(int j = 0; j < recipe.inputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.inputs.get(j);

				if(componentMatch(entity, component, true)) {
					matchedRecipe = false;
					break;
				}
			}

			for(int j = 0; j < recipe.outputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.outputs.get(j);

				if(componentMatch(entity, component, false)) {
					matchedRecipe = false;
					break;
				}
			}

			if(!matchedRecipe) continue;

			for(int j = 0; j < recipe.inputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.inputs.get(j);

				if(component.target == CJMachineRecipeTarget.TANK) {
					CJTankVolume volume = entity.tanks.get(component.index);

					boolean isPrimal = (entity.jewelRarity == CJRarity.PRIMAL);
					if(component.index == recipe.fuelIndex) {
						if(recipe.allowPassive &&
								isPrimal &&
								volume.current < component.volume.current) {

							entity.isPassive = true;
							entity.errorMessage = "message.cj_passive";
							entity.isWarning = true;
							continue;
						}
					}

					if(volume.current < component.volume.current) {
						if(component.optional) {
							entity.isPassive = true;
							entity.errorMessage = "message.cj_no_optional";
							entity.isWarning = true;
							continue;
						}

						entity.errorMessage =
								translate.translateKey("message.cj_resource");

						return null;
					}
				}
				else {
					ItemStack stack = entity.getStackInSlot(component.index);

					if(component.optional && stack == null) {
						entity.isPassive = true;
						entity.errorMessage = "message.cj_no_optional";
						entity.isWarning = true;
						continue;
					}

					if(stack.stackSize < component.getStackSize()) {
						if(component.optional) {
							entity.isPassive = true;
							entity.errorMessage = "message.cj_no_optional";
							entity.isWarning = true;
							continue;
						}

						entity.errorMessage =
								translate.translateKey("message.cj_resource");

						return null;
					}
				}
			}

			break;
		}

		if(recipe != null) {
			if(!raritySufficient(entity.jewelRarity, recipe.requiredRarity)) {
				entity.errorMessage =
						translate.translateKey("message.cj_poor_jewel");

				return null;
			}
		}

		if(!matchedRecipe) {
			entity.errorMessage =
					translate.translateKey("message.cj_bad_recipe");

			return null;
		}

		return recipe;
	}

	public void runRecipe(
			CJMachineRecipe recipe, CJTileEntityMachineBase entity) {

		for(int i = 0; i < recipe.outputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.outputs.get(i);

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = entity.tanks.get(component.index);
				if(volume.current >= volume.max) return;
			}
			else {
				ItemStack outputStack =
						entity.getStackInSlot(component.index);

				if(outputStack == null) continue;

				if(outputStack.stackSize >= outputStack.getMaxStackSize()) {
					return;
				}
			}
		}

		// Timescale and ticking.
		int timeScale = CJRarityInfo.getRarityTimeScale(entity.jewelRarity);
		entity.operationLength = recipe.processTime / timeScale;
		if(entity.isPassive) entity.operationLength *= 2;
		if(entity.operationTicks++ < entity.operationLength) return;

		// Handle fuel separately from other fluid inputs.
		int powerScale = CJRarityInfo.getRarityPowerScale(entity.jewelRarity);
		CJMachineRecipeComponent fuelComponent = recipe.getFuelComponent();
		if(fuelComponent != null) {
			int cost = fuelComponent.volume.current / powerScale;

			if(recipe.fuelIndex != -1) {
				if(!entity.isPassive) {
					CJTankVolume volume =
							entity.tanks.get(fuelComponent.index);

					volume.removeFluid(0, cost, true);
				}
			}
		}

		// Generic inputs.
		for(int i = 0; i < recipe.inputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.inputs.get(i);

			if(entity.worldObj.rand.nextFloat() > component.chance) {
				continue;
			}

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = entity.tanks.get(component.index);
				volume.removeFluid(0, component.volume.current, true);
			}
			else {
				ItemStack inputStack = entity.getStackInSlot(component.index);

				if(component.optional && inputStack == null) continue;

				if(inputStack.stackSize == component.getStackSize()) {
					entity.setInventorySlotContents(component.index, null);
				}
				else inputStack.stackSize -= component.getStackSize();
			}
		}

		// Generic outputs.
		for(int i = 0; i < recipe.outputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.outputs.get(i);

			if(entity.worldObj.rand.nextFloat() > component.chance) {
				continue;
			}

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = entity.tanks.get(component.index);
				volume.addFluid(
						component.volume.fluidID, component.volume.current,
						true);
			}
			else {
				ItemStack inputStack = entity.getStackInSlot(component.index);

				if(inputStack == null) {
					entity.setInventorySlotContents(
							component.index, component.stack.copy());
				}
				else inputStack.stackSize += component.stack.stackSize;
			}
		}

		if(jewelSlotIndex != -1) {
			entity.getStackInSlot(jewelSlotIndex).damageItem(1, null, true);
		}

		entity.operationTicks = 0;

	}
}
