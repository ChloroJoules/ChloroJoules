package io.github.chlorojoules.machine;

import io.github.chlorojoules.*;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.*;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

import static io.github.chlorojoules.CJRarityInfo.raritySufficient;
import static io.github.chlorojoules.gui.CJGuiGravity.*;
import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.FLUID_HEIGHT;
import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.WORKING_HEIGHT;

public class CJMachineBuilder {
	// TODO: Ensure CJ can't be routed into non-fuel tanks.

	public static final int FUEL_TANK_INSET = 10;
	public static final int FUEL_TANK_SIZE = 4 * CJTank.BUCKET;

	public static final int JEWEL_SLOT_INSET = 35;

	public String name = null;
	public CJRarity rarity = CJRarity.MANUFACTURED;
	public Class<? extends CJIMachine> machineImpl = null;
	public boolean doDropMeta = true;

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

	public CJMachineBuilder setMachineName(String value) {
		name = value;
		return this;
	}

	public CJMachineBuilder setRarity(CJRarity value) {
		rarity = value;
		return this;
	}

	public CJMachineBuilder setImpl(Class<? extends CJIMachine> value) {
		machineImpl = value;
		return this;
	}

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

	public CJMachineBuilder addButton(
			int x, int y, String tooltip, ItemStack label) {

		buttons.add(new CJGuiButton(x, y, tooltip, label));

		return this;
	}

	public CJMachineBuilder addButtonGravity(
			CJGuiGravity anchor, int x, int y, String tooltip,
			ItemStack label) {

		return addButton(
				CJGuiGravityInfo.getButtonAnchoredX(anchor, x),
				CJGuiGravityInfo.getButtonAnchoredY(anchor, y),
				tooltip, label);
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

	// NOTE: Pass the block ID of the flowing variant of the fluid you want
	//       To set as locked to `lockFluid`, otherwise `0`.
	public CJMachineBuilder addTank(
			int x, int y, boolean output, boolean bidirectional,
			int max, int lockFluid) {

		CJTank tank = new CJTank(x, y);
		tank.output = output;
		tank.bidirectional = bidirectional;
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
				CJGuiGravity.TOP_LEFT, FUEL_TANK_INSET, false, false,
				FUEL_TANK_SIZE, CJMod.fuelFluid);
	}

	public CJMachineBuilder addJewelSlot() {
		jewelSlotIndex = slots.size();

		addSlotGravity(
				CJGuiGravity.BOTTOM_LEFT, JEWEL_SLOT_INSET,
				/* Align bottom of Jewel slot with fuel tank. */
				(WORKING_HEIGHT - FLUID_HEIGHT) / 2, false);

		slots.getLast().setGem(true);

		return this;
	}

	public CJMachineBuilder addTankGravity(
			CJGuiGravity anchor, int x, int y, boolean output,
			boolean bidirectional, int max, int lockFluid) {

		return addTank(
				CJGuiGravityInfo.getTankAnchoredX(anchor, x),
				CJGuiGravityInfo.getTankAnchoredY(anchor, y),
				output, bidirectional, max, lockFluid);
	}

	// TODO: Add "ElementBuilder" which has sensible alignment/type defaults
	//       Then set further sub-members from there.
	public CJMachineBuilder addTankGravityVCenter(
			CJGuiGravity anchor, int x, boolean output, boolean bidirectional,
			int max, int lockFluid) {

		return addTank(
				CJGuiGravityInfo.getTankAnchoredX(anchor, x),
				CJGuiGravityInfo.getTankAnchoredY(CENTER, 0),
				output, bidirectional, max, lockFluid);
	}

	public CJMachineBuilder addProgressBar(int x, int y) {
		progressBar = new CJGuiElement(x, y);

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

	public CJMachineBuilder addRecipe(
			int fuelVolume, CJMachineRecipeComponent in,
			CJMachineRecipeComponent out, int ticks, boolean allowPassive,
			int requiredButton) {

		CJMachineRecipe recipe = new CJMachineRecipe(
				this, fuelVolume, in, out, ticks, allowPassive);

		recipe.requiredButton = requiredButton;

		recipes.add(recipe);

		return this;
	}

	public CJMachineBuilder addRecipe(
			int fuelVolume, CJMachineRecipeComponent[] in,
			CJMachineRecipeComponent[] out, int ticks, boolean allowPassive,
			int requiredButton) {

		CJMachineRecipe recipe = new CJMachineRecipe(
				this, fuelVolume, in, out, ticks, allowPassive);

		recipe.requiredButton = requiredButton;

		recipes.add(recipe);

		return this;
	}

	public CJMachineBuilder addRecipeNoFuel(
			CJMachineRecipeComponent in, CJMachineRecipeComponent out,
			int ticks) {

		recipes.add(new CJMachineRecipe(in, out, ticks));

		return this;
	}

	public CJMachineBuilder addRecipe(CJMachineRecipe recipe) {
		recipes.add(recipe);
		return this;
	}

	public CJMachineBuilder addRecipeRarity(
			int fuelVolume, CJMachineRecipeComponent in,
			CJMachineRecipeComponent out, int ticks, CJRarity rarity,
			int requiredButton) {

		CJMachineRecipe recipe = new CJMachineRecipe(
				this, fuelVolume, in, out, ticks, false);

		recipe.requiredRarity = rarity;
		recipe.requiredButton = requiredButton;

		recipes.add(recipe);

		return this;
	}

	// TODO: Output components don't verify that there is space left.
	private boolean componentMatch(
			CJTileEntityMachineBase entity,
			CJMachineRecipeComponent component, boolean input) {

		if(component.target == CJMachineRecipeTarget.TANK) {
			CJTankVolume volume = entity.tanks.get(component.index);

			if(volume.fluidID == 0) return !input;

			return volume.fluidID == component.volume.fluidID;
		}
		else {
			ItemStack stack = entity.stacks.get(component.index);

			if(stack == null) return !input;
			if(component.isTag) {
				return CJMod.matchesTagItem(component.tag, stack);
			}

			return stack.getItemID() == component.stack.getItemID() &&
					stack.getItemDamage() == component.stack.getItemDamage();
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

			entity.jewelRarity =
					CJRarityInfo.getJewelRarity(jewel.getItemID());

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

			if(recipe.requiredButton != -1) {
				if(!entity.buttonStates.get(recipe.requiredButton)) {
					matchedRecipe = false;
				}
			}

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
								volume.current < component.volume.current) {

							entity.isPassive = true;
							entity.errorMessage = "message.cj_passive";
							entity.isWarning = true;
							continue;
						}
					}

					if(volume.current < component.volume.current) {
						entity.errorMessage =
								translate.translateKey("message.cj_resource");

						return null;
					}
				}
				else {
					ItemStack stack = entity.stacks.get(component.index);

					if(stack.stackSize < component.getStackSize()) {
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

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = entity.tanks.get(component.index);
				volume.removeFluid(0, component.volume.current, true);
			}
			else {
				ItemStack inputStack = entity.stacks.get(component.index);

				if(inputStack.stackSize == component.getStackSize()) {
					entity.stacks.set(component.index, null);
				}
				else inputStack.stackSize -= component.getStackSize();
			}
		}

		// Generic outputs.
		for(int i = 0; i < recipe.outputs.size(); i++) {
			CJMachineRecipeComponent component = recipe.outputs.get(i);

			if(component.target == CJMachineRecipeTarget.TANK) {
				CJTankVolume volume = entity.tanks.get(component.index);
				volume.addFluid(component.volume, true);
			}
			else {
				ItemStack inputStack = entity.stacks.get(component.index);

				if(inputStack == null) {
					entity.stacks.set(
							component.index, component.stack.copy());
				}
				else inputStack.stackSize += component.stack.stackSize;
			}
		}

		entity.operationTicks = 0;

		return true;
	}
}
