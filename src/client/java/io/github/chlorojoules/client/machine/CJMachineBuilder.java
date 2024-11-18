package io.github.chlorojoules.client.machine;

import io.github.chlorojoules.client.*;
import io.github.chlorojoules.client.gui.CJGuiElement;
import io.github.chlorojoules.client.gui.CJGuiGravity;
import io.github.chlorojoules.client.gui.CJGuiGravityInfo;
import net.minecraft.src.client.gui.StringTranslate;
import net.minecraft.src.game.item.ItemStack;

import java.util.ArrayList;
import java.util.logging.Logger;

import static io.github.chlorojoules.client.CJRarityInfo.raritySufficient;
import static io.github.chlorojoules.client.gui.CJGuiGravity.*;

public class CJMachineBuilder {
	private static final int FUEL_TANK_INSET = 10;
	private static final int FUEL_TANK_SIZE = 4 * CJTank.BUCKET;

	public String name = null;
	public CJRarity rarity = CJRarity.MANUFACTURED;
	public CJIMachine machineImpl = null;

	public int fuelTankIndex = -1;

	public ArrayList<CJMachineSlotInfo> slots = new ArrayList<>();
	public ArrayList<CJTank> tanks = new ArrayList<>();
	public ArrayList<CJTankVolume> tankVolumes = new ArrayList<>();
	public ArrayList<CJGuiElement> progressBars = new ArrayList<>();

	public ArrayList<CJMachineRecipe> recipies = new ArrayList<>();

	public CJMachineBuilder setMachineName(String value) {
		name = value;
		return this;
	}

	public CJMachineBuilder setRarity(CJRarity value) {
		rarity = value;
		return this;
	}

	public CJMachineBuilder setImpl(CJIMachine value) {
		machineImpl = value;
		return this;
	}

	// TODO: Mechanism for anchoring/centering slots/tanks in builder
	//       Interface.
	// TODO: Helper for ChloroJewel slots.
	public CJMachineBuilder addSlot(int x, int y, boolean output) {
		slots.add(new CJMachineSlotInfo(x, y, output));

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

	public CJMachineBuilder addTankGravity(
			CJGuiGravity anchor, int x, int y, boolean output, int max,
			int lockFluid) {

		return addTank(
				CJGuiGravityInfo.getTankAnchoredX(anchor, x),
				CJGuiGravityInfo.getTankAnchoredY(anchor, y),
				output, max, lockFluid);
	}

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

	public CJMachineBuilder addRecipe(
			int fuelVolume, CJMachineRecipeComponent in,
			CJMachineRecipeComponent out, int ticks) {

		recipies.add(new CJMachineRecipe(this, fuelVolume, in, out, ticks));

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

	public CJMachineRecipe getMatchingRecipe(
			CJRarity jewelRarity, CJTileEntityMachineBase entity) {

		StringTranslate translate = StringTranslate.getInstance();
		CJMachineRecipe recipe = null;

		boolean matchedRecipe = false;
		for(int i = 0; i < entity.machineBuilder.recipies.size(); i++) {
			matchedRecipe = true;
			recipe = entity.machineBuilder.recipies.get(i);

			for(int j = 0; j < recipe.inputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.inputs.get(i);

				if(component.target == CJMachineRecipeTarget.TANK) {
					CJTankVolume volume = entity.tanks.get(component.index);

					if(volume.fluidID != component.id) {
						matchedRecipe = false;
						break;
					}
				}
				else {
					ItemStack stack = entity.stacks.get(component.index);

					if(stack.itemID != component.id) {
						matchedRecipe = false;
						break;
					}
				}
			}

			if(!matchedRecipe) continue;

			for(int j = 0; j < recipe.inputs.size(); j++) {
				CJMachineRecipeComponent component = recipe.inputs.get(i);

				if(component.target == CJMachineRecipeTarget.TANK) {
					CJTankVolume volume = entity.tanks.get(component.index);

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
			if(!raritySufficient(jewelRarity, recipe.requiredRarity)) {
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
}
