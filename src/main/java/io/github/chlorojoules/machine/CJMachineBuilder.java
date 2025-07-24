package io.github.chlorojoules.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chlorojoules.*;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.*;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.TaggedIngredients;
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

	public String name;
	public CJRarity rarity;
	public Class<? extends CJIMachine> machineImpl =
			CJMachineRecipeConsumer.class;

	public boolean doDropMeta = false;

	public ArrayList<CJMachineSlotInfo> slots = new ArrayList<>();
	public ArrayList<CJTank> tanks = new ArrayList<>();
	public ArrayList<CJTankVolume> tankVolumes = new ArrayList<>();
	public CJGuiElement progressBar = null;
	public ArrayList<CJGuiButton> buttons = new ArrayList<>();
	public ArrayList<CJGuiCoordinate> linkCoordinates = new ArrayList<>();

	public ArrayList<CJMachineRecipe> recipes = new ArrayList<>();

	public String[] iconNames;
	public boolean iconDefault = true;
	public CJMachineBlockSideMode sideMode = CJMachineBlockSideMode.FRONT_FACE;
	public CJMachineTier tier = CJMachineTier.INDUSTRIAL;

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
								"Class '" + implName + "' does not " +
								"implement 'CJIMachine'");
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
				recipes.add(new CJMachineRecipe(recipe.getAsJsonObject()));
			}
		}
	}

	public int getNamedDamage(String id) {
		if(iconDefault) return 0;

		for(int i = 0; i < iconNames.length; i++) {
			if(iconNames[i].equals(id)) return i;
		}

		return 0;
	}

	private int getNamedSlotIndex(String id) {
		for(int i = 0; i < slots.size(); ++i) {
			CJMachineSlotInfo slot = slots.get(i);
			if(slot.id == null) continue;
			if(slot.id.equals(id)) return i;
		}

		throw new RuntimeException("Unknown slot id '" + id + "'");
	}

	private boolean hasNamedSlot(String id) {
		try {
			getNamedSlotIndex(id);
			return true;
		}
		catch(RuntimeException e) {
			return false;
		}
	}

	private ItemStack getNamedStack(
			CJTileEntityMachineBase machineEntity, String id) {

		return machineEntity.getStackInSlot(getNamedSlotIndex(id));
	}

	private void setNamedStack(
			CJTileEntityMachineBase machineEntity, String id,
			ItemStack stack) {

		machineEntity.setInventorySlotContents(getNamedSlotIndex(id), stack);
	}

	private boolean getNamedButtonState(
			CJTileEntityMachineBase machineEntity, String id) {

		for(int i = 0; i < buttons.size(); ++i) {
			CJGuiButton button = buttons.get(i);
			if(button.id == null) continue;
			if(button.id.equals(id)) return machineEntity.buttonStates.get(i);
		}

		throw new RuntimeException("Unknown button id '" + id + "'");
	}

	private int getNamedTankIndex(String id) {
		for(int i = 0; i < tanks.size(); ++i) {
			CJTank tank = tanks.get(i);
			if(tank.id == null) continue;
			if(tank.id.equals(id)) return i;
		}

		throw new RuntimeException("Unknown tank id '" + id + "'");
	}

	public boolean hasNamedTank(String id) {
		try {
			getNamedTankIndex(id);
			return true;
		}
		catch(RuntimeException e) {
			return false;
		}
	}

	private CJTankVolume getNamedTankVolume(
			CJTileEntityMachineBase machineEntity, String id) {

		return machineEntity.tanks.get(getNamedTankIndex(id));
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

	private boolean noComponentMatch(
			CJTileEntityMachineBase machineEntity,
			CJMachineRecipeComponent component, boolean input) {

		if(component.optional) return false;

		if(component.target == CJMachineRecipeTarget.TANK) {
			CJTankVolume volume =
					getNamedTankVolume(machineEntity, component.targetID);

			if(volume.fluidID == 0) return input;
			if(!input &&
					volume.max- volume.current < component.volume.current) {

				return true;
			}

			return volume.fluidID != component.volume.fluidID;
		}
		else {
			ItemStack stack = getNamedStack(machineEntity, component.targetID);

			if(stack == null) return input;
			if(!input &&
					stack.getMaxStackSize() - stack.stackSize <
					component.stack.stackSize) {

				return true;
			}

			if(component.isTag) {
				return !TaggedIngredients.get(component.tag)
						.matchIngredient(stack);
			}

			return stack.getItemID() != component.stack.getItemID() ||
					stack.getItemDamage() != component.stack.getItemDamage();
		}
	}

	public CJMachineRecipe getMatchingRecipe(
			CJTileEntityMachineBase machineEntity) {

		machineEntity.errorMessage = null;
		machineEntity.isWarning = false;
		machineEntity.isPassive = false;

		StringTranslate translate = StringTranslate.getInstance();
		CJMachineRecipe recipe = null;

		machineEntity.jewelRarity = CJRarity.MANUFACTURED;
		if(hasNamedSlot("jewel")) {
			ItemStack jewel = getNamedStack(machineEntity, "jewel");

			if(jewel == null) {
				machineEntity.errorMessage =
						translate.translateKey("message.cj_no_jewel");

				return null;
			}

			machineEntity.jewelRarity =
					CJRarityInfo.getJewelRarity(jewel.getItemID());

			if(machineEntity.jewelRarity == CJRarity.INVALID) {
				machineEntity.errorMessage =
						translate.translateKey("message.cj_bad_jewel");

				return null;
			}
		}

		boolean matchedRecipe = false;
		for(CJMachineRecipe machineRecipe : recipes) {
			matchedRecipe = true;
			recipe = machineRecipe;

			if(recipe.requiredButton != null) {
				if(!getNamedButtonState(
						machineEntity, recipe.requiredButton)) {

					matchedRecipe = false;
				}
			}

			for(int j = 0; j < recipe.inputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.inputs.get(j);

				if(noComponentMatch(machineEntity, component, true)) {
					matchedRecipe = false;
					break;
				}
			}

			for(int j = 0; j < recipe.outputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.outputs.get(j);

				if(noComponentMatch(machineEntity, component, false)) {
					matchedRecipe = false;
					break;
				}
			}

			if(!matchedRecipe) continue;

			for(int j = 0; j < recipe.inputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.inputs.get(j);

				if(component.target == CJMachineRecipeTarget.TANK) {
					CJTankVolume volume = getNamedTankVolume(
							machineEntity, component.targetID);

					boolean isPrimal = (machineEntity.jewelRarity == CJRarity.PRIMAL);
					if(component.targetID.equals("fuel")) {
						if(recipe.allowPassive &&
								isPrimal &&
								volume.current < component.volume.current) {

							machineEntity.isPassive = true;
							machineEntity.errorMessage = "message.cj_passive";
							machineEntity.isWarning = true;
							continue;
						}
					}

					if(volume.current < component.volume.current) {
						if(component.optional) {
							machineEntity.isPassive = true;
							machineEntity.errorMessage = "message.cj_no_optional";
							machineEntity.isWarning = true;
							continue;
						}

						machineEntity.errorMessage =
								translate.translateKey("message.cj_resource");

						return null;
					}
				}
				else {
					ItemStack stack =
							getNamedStack(machineEntity, component.targetID);

					if(component.optional && stack == null) {
						machineEntity.isPassive = true;
						machineEntity.errorMessage = "message.cj_no_optional";
						machineEntity.isWarning = true;
						continue;
					}

					if(stack.stackSize < component.getStackSize()) {
						if(component.optional) {
							machineEntity.isPassive = true;
							machineEntity.errorMessage = "message.cj_no_optional";
							machineEntity.isWarning = true;
							continue;
						}

						machineEntity.errorMessage =
								translate.translateKey("message.cj_resource");

						return null;
					}
				}
			}

			break;
		}

		if(recipe != null) {
			if(!raritySufficient(machineEntity.jewelRarity, recipe.requiredRarity)) {
				machineEntity.errorMessage =
						translate.translateKey("message.cj_poor_jewel");

				return null;
			}
		}

		if(!matchedRecipe) {
			machineEntity.errorMessage =
					translate.translateKey("message.cj_bad_recipe");

			return null;
		}

		return recipe;
	}

	public void runRecipe(
			CJMachineRecipe recipe, CJTileEntityMachineBase machineEntity) {

		for(int i = 0; i < recipe.outputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.outputs.get(i);

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume =
						getNamedTankVolume(machineEntity, component.targetID);

				if(volume.current >= volume.max) return;
			}
			else {
				ItemStack outputStack =
						getNamedStack(machineEntity, component.targetID);

				if(outputStack == null) continue;

				if(outputStack.stackSize >= outputStack.getMaxStackSize()) {
					return;
				}
			}
		}

		// Timescale and ticking.
		int timeScale = CJRarityInfo.getRarityTimeScale(machineEntity.jewelRarity);
		machineEntity.operationLength = recipe.processTime / timeScale;
		if(machineEntity.isPassive) machineEntity.operationLength *= 2;
		if(machineEntity.operationTicks++ < machineEntity.operationLength) return;

		// Handle fuel separately from other fluid inputs.
		int powerScale = CJRarityInfo.getRarityPowerScale(machineEntity.jewelRarity);
		CJMachineRecipeComponent fuelComponent = recipe.getFuelComponent();
		if(fuelComponent != null) {
			int cost = fuelComponent.volume.current / powerScale;

			if(recipe.fuelIndex != -1) {
				if(!machineEntity.isPassive) {
					CJTankVolume volume =
							getNamedTankVolume(machineEntity, "fuel");

					volume.removeFluid(0, cost, true);
				}
			}
		}

		// Generic inputs.
		for(int i = 0; i < recipe.inputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.inputs.get(i);

			if(machineEntity.worldObj.rand.nextFloat() > component.chance) {
				continue;
			}

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = getNamedTankVolume(
						machineEntity, component.targetID);

				volume.removeFluid(0, component.volume.current, true);
			}
			else {
				ItemStack inputStack =
						getNamedStack(machineEntity, component.targetID);

				if(component.optional && inputStack == null) continue;

				if(inputStack.stackSize == component.getStackSize()) {
					setNamedStack(machineEntity, component.targetID, null);
				}
				else inputStack.stackSize -= component.getStackSize();
			}
		}

		// Generic outputs.
		for(int i = 0; i < recipe.outputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.outputs.get(i);

			if(machineEntity.worldObj.rand.nextFloat() > component.chance) {
				continue;
			}

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = getNamedTankVolume(
						machineEntity, component.targetID);

				volume.addFluid(
						component.volume.fluidID, component.volume.current,
						true);
			}
			else {
				ItemStack inputStack =
						getNamedStack(machineEntity, component.targetID);

				if(inputStack == null) {
					setNamedStack(
							machineEntity, component.targetID,
							component.stack.copy());
				}
				else inputStack.stackSize += component.stack.stackSize;
			}
		}

		if(hasNamedSlot("jewel")) {
			ItemStack jewelStack = getNamedStack(machineEntity, "jewel");
			jewelStack.damageItem(1, null, true);
			if(jewelStack.stackSize == 0) {
				setNamedStack(machineEntity, "jewel", null);
			}
		}

		machineEntity.operationTicks = 0;
	}
}
