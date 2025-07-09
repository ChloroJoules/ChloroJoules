package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.*;
import io.github.chlorojoules.client.gui.CJGuiElement;
import io.github.chlorojoules.client.gui.CJGuiGravity;
import io.github.chlorojoules.client.gui.CJGuiGravityInfo;
import io.github.chlorojoules.client.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.client.gui.CJGuiCoordinate;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.item.ItemStack;

import java.util.ArrayList;

import static io.github.chlorojoules.client.CJRarityInfo.raritySufficient;
import static io.github.chlorojoules.client.gui.CJGuiGravity.*;
import static io.github.chlorojoules.client.gui.CJGuiMachineBaseLayout.FLUID_HEIGHT;
import static io.github.chlorojoules.client.gui.CJGuiMachineBaseLayout.WORKING_HEIGHT;

public class CJMachineBuilder {
	public static final int FUEL_TANK_INSET = 10;
	public static final int FUEL_TANK_SIZE = 4 * CJTank.BUCKET;

	public static final int JEWEL_SLOT_INSET = 35;

	public String name = null;
	public CJRarity rarity = CJRarity.MANUFACTURED;
	public Class<?> machineImpl = null;

	public int fuelTankIndex = -1;
	public int jewelSlotIndex = -1;

	public ArrayList<CJMachineSlotInfo> slots = new ArrayList<>();
	public ArrayList<CJTank> tanks = new ArrayList<>();
	public ArrayList<CJTankVolume> tankVolumes = new ArrayList<>();
	public ArrayList<CJGuiElement> progressBars = new ArrayList<>();
	// TODO: This is kind of hardcoded -- is there a way we can make
	//  	 `CJGuiElement` more generic/programmable?
	public ArrayList<CJGuiCoordinate> linkCoordinates =
			new ArrayList<>();

	public ArrayList<CJMachineRecipe> recipes = new ArrayList<>();

	public CJMachineBuilder setMachineName(String value) {
		name = value;
		return this;
	}

	public CJMachineBuilder setRarity(CJRarity value) {
		rarity = value;
		return this;
	}

	public CJMachineBuilder setImpl(Class<?> value) {
		machineImpl = value;
		return this;
	}

	// TODO: Mechanism for anchoring/centering slots/tanks in builder
	//       Interface.
	public CJMachineBuilder addSlot(int x, int y, boolean output) {
		slots.add(new CJMachineSlotInfo(x, y, output));

		return this;
	}

	public CJMachineBuilder setSlotDamageExclusive(
			int slot, int[] damageExclusive) {

		slots.get(slot).setDamageExclusive(damageExclusive);

		return this;
	}

	public CJMachineBuilder setTankDamageExclusive(
			int tank, int[] damageExclusive) {

		tanks.get(tank).setDamageExclusive(damageExclusive);

		return this;
	}

	public CJMachineBuilder addSlotGravity(
			CJGuiGravity anchor, int x, int y, boolean output) {

		return addSlot(
				CJGuiGravityInfo.getSlotAnchoredX(anchor, x, output),
				CJGuiGravityInfo.getSlotAnchoredY(anchor, y, output),
				output);
	}

	public CJMachineBuilder addSlotGravityVCenter(
			CJGuiGravity anchor, int x, boolean output) {

		return addSlot(
				CJGuiGravityInfo.getSlotAnchoredX(anchor, x, output),
				CJGuiGravityInfo.getSlotAnchoredY(CENTER, 0, output),
				output);
	}

	public CJMachineBuilder addCoordinate(int x, int y, String label) {
		linkCoordinates.add(new CJGuiCoordinate(x, y, label));

		return this;
	}

	public CJMachineBuilder addCoordinateGravity(
			CJGuiGravity anchor, int x, int y, String label) {

		return addCoordinate(
				CJGuiGravityInfo.getCoordinateAnchoredX(anchor, x),
				CJGuiGravityInfo.getCoordinateAnchoredY(anchor, y),
				label);
	}

	public CJMachineBuilder addCoordinateGravityHCenter(
			CJGuiGravity anchor, int y, String label) {

		return addCoordinate(
				CJGuiGravityInfo.getCoordinateAnchoredX(CENTER, 0),
				CJGuiGravityInfo.getCoordinateAnchoredY(anchor, y),
				label);
	}

	// TODO: Add helper for adding specifically fuel tanks.
	// NOTE: Pass the block ID of the flowing variant of the fluid you want
	//       To set as locked to `lockFluid`, otherwise `0`.
	public CJMachineBuilder addTank(
			int x, int y, boolean output, int max, int lockFluid) {

		CJTank tank = new CJTank(x, y);
		tank.output = output;
		tanks.add(tank);

		CJTankVolume tankVolume = new CJTankVolume();
		tankVolume.max = max;
		tankVolume.fluidID = lockFluid;
		tankVolume.lockFluid = (lockFluid != 0);
		tankVolumes.add(tankVolume);

		return this;
	}

	public CJMachineBuilder addFuelTank() {
		fuelTankIndex = tanks.size();

		return addTankGravityVCenter(
				CJGuiGravity.TOP_LEFT, FUEL_TANK_INSET, false, FUEL_TANK_SIZE,
				CJClient.fuelFluid);
	}

	public CJMachineBuilder addJewelSlot() {
		jewelSlotIndex = slots.size();

		return addSlotGravity(
				CJGuiGravity.BOTTOM_LEFT, JEWEL_SLOT_INSET,
				/* Align bottom of Jewel slot with fuel tank. */
				(WORKING_HEIGHT - FLUID_HEIGHT) / 2, false);
	}

	public CJMachineBuilder addTankGravity(
			CJGuiGravity anchor, int x, int y, boolean output, int max,
			int lockFluid) {

		return addTank(
				CJGuiGravityInfo.getTankAnchoredX(anchor, x),
				CJGuiGravityInfo.getTankAnchoredY(anchor, y),
				output, max, lockFluid);
	}

	// TODO: Add "ElementBuilder" which has sensible alignment/type defaults
	//       Then set further sub-members from there.
	public CJMachineBuilder addTankGravityVCenter(
			CJGuiGravity anchor, int x, boolean output, int max,
			int lockFluid) {

		return addTank(
				CJGuiGravityInfo.getTankAnchoredX(anchor, x),
				CJGuiGravityInfo.getTankAnchoredY(CENTER, 0),
				output, max, lockFluid);
	}

	public CJMachineBuilder addProgressBar(int x, int y) {
		progressBars.add(new CJGuiElement(x, y));

		return this;
	}

	public CJMachineBuilder addProgressBarGravity(
			CJGuiGravity anchor, int x, int y) {

		return addProgressBar(
				CJGuiGravityInfo.getProgressBarAnchoredX(anchor, x),
				CJGuiGravityInfo.getProgressBarAnchoredY(anchor, y));
	}

	public CJMachineBuilder addProgressBarGravityVCenter(
			CJGuiGravity anchor, int x) {

		return addProgressBar(
				CJGuiGravityInfo.getProgressBarAnchoredX(anchor, x),
				CJGuiGravityInfo.getProgressBarAnchoredY(CENTER, 0));
	}

	// TODO: Make a recipe builder.
	public CJMachineBuilder addRecipe(
			int fuelVolume, CJMachineRecipeComponent in,
			CJMachineRecipeComponent out, int ticks, boolean allowPassive) {

		CJMachineRecipe recipe = new CJMachineRecipe(
				this, fuelVolume, in, out, ticks, allowPassive);

		recipes.add(recipe);

		return this;
	}

	public CJMachineBuilder addRecipeNoFuel(
			CJMachineRecipeComponent in, CJMachineRecipeComponent out,
			int ticks) {

		recipes.add(new CJMachineRecipe(in, out, ticks));

		return this;
	}

	public CJMachineBuilder addRecipeRarity(
			int fuelVolume, CJMachineRecipeComponent in,
			CJMachineRecipeComponent out, int ticks, CJRarity rarity) {

		CJMachineRecipe recipe = new CJMachineRecipe(
				this, fuelVolume, in, out, ticks, false);

		recipe.requiredRarity = rarity;

		recipes.add(recipe);

		return this;
	}

	public int getPrimaryInputTankIndex() {
		for(int i = 0; i < tanks.size(); i++) {
			if(!tanks.get(i).output) return i;
		}

		return -1;
	}

	public int getPrimaryOutputTankIndex() {
		for(int i = 0; i < tanks.size(); i++) {
			if(tanks.get(i).output) return i;
		}

		return -1;
	}

	public int getPrimaryInputSlotIndex() {
		for(int i = 0; i < slots.size(); i++) {
			if(!slots.get(i).output) return i;
		}

		return -1;
	}

	public int getPrimaryOutputSlotIndex() {
		for(int i = 0; i < slots.size(); i++) {
			if(slots.get(i).output) return i;
		}

		return -1;
	}

	private boolean componentMatch(
			CJTileEntityMachineBase entity,
			CJMachineRecipeComponent component, boolean input) {

		if(component.target == CJMachineRecipeTarget.TANK) {
			CJTankVolume volume = entity.tanks.get(component.index);

			if(volume.fluidID == 0) return !input;

			return volume.fluidID == component.id;
		}
		else {
			ItemStack stack = entity.stacks.get(component.index);

			if(stack == null) return !input;

			return stack.itemID == component.id;
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
			ItemStack jewel = entity.stacks.get(jewelSlotIndex);

			if(jewel == null) {
				entity.errorMessage =
						translate.translateKey("message.cj_no_jewel");

				return null;
			}

			entity.jewelRarity = CJRarityInfo.getJewelRarity(jewel.itemID);

			if(entity.jewelRarity == CJRarity.INVALID) {
				entity.errorMessage =
						translate.translateKey("message.cj_bad_jewel");

				return null;
			}
		}

		boolean matchedRecipe = false;
		for(int i = 0; i < entity.machineBuilder.recipes.size(); i++) {
			matchedRecipe = true;
			recipe = entity.machineBuilder.recipes.get(i);

			for(int j = 0; j < recipe.inputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.inputs.get(j);

				if(!componentMatch(entity, component, true)) {
					matchedRecipe = false;
					break;
				}
			}

			for(int j = 0; j < recipe.outputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.outputs.get(j);

				if(!componentMatch(entity, component, false)) {
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
								volume.current < component.count) {

							entity.isPassive = true;
							entity.errorMessage = "message.cj_passive";
							entity.isWarning = true;
							continue;
						}
					}

					if(volume.current < component.count) {
						entity.errorMessage =
								translate.translateKey("message.cj_resource");

						return null;
					}
				}
				else {
					ItemStack stack = entity.stacks.get(component.index);

					if(stack.stackSize < component.count) {
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

	public boolean runRecipe(
			CJMachineRecipe recipe, CJTileEntityMachineBase entity) {

		for(int i = 0; i < recipe.outputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.outputs.get(i);

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = entity.tanks.get(component.index);
				if(volume.current >= volume.max) return false;
			}
			else {
				ItemStack outputStack =
						entity.stacks.get(component.index);

				if(outputStack == null) continue;

				if(outputStack.stackSize >= outputStack.getMaxStackSize()) {
					return false;
				}
			}
		}

		// Timescale and ticking.
		int timeScale = CJRarityInfo.getRarityTimeScale(entity.jewelRarity);
		entity.operationLength = recipe.processTime / timeScale;
		if(entity.operationTicks++ < entity.operationLength) return false;

		// Handle fuel separately from other fluid inputs.
		int powerScale = CJRarityInfo.getRarityPowerScale(entity.jewelRarity);
		CJMachineRecipeComponent fuelComponent = recipe.getFuelComponent();
		int cost = fuelComponent.count / powerScale;

		if(recipe.fuelIndex != -1) {
			if(!entity.isPassive) {
				CJTankVolume volume = entity.tanks.get(fuelComponent.index);

				volume.removeFluid(0, cost, true);
			}
		}

		// Generic inputs.
		for(int i = 0; i < recipe.inputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.inputs.get(i);

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = entity.tanks.get(component.index);
				volume.removeFluid(0, component.count, true);
			}
			else {
				ItemStack inputStack = entity.stacks.get(component.index);

				if(inputStack.stackSize == component.count) {
					entity.stacks.set(component.index, null);
				}
				else inputStack.stackSize -= component.count;
			}
		}

		// Generic outputs.
		for(int i = 0; i < recipe.outputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.outputs.get(i);

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = entity.tanks.get(component.index);
				volume.addFluid(component.id, component.count, true);
			}
			else {
				ItemStack inputStack = entity.stacks.get(component.index);

				if(inputStack == null) {
					entity.stacks.set(
							component.index,
							new ItemStack(component.id, component.count));
				}
				else inputStack.stackSize += component.count;
			}
		}

		entity.operationTicks = 0;

		return true;
	}
}
