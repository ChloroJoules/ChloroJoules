package io.github.chlorojoules.machine;

import com.fox2code.foxloader.energy.FoxPowerType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.indigo3d.util.RenderSystem;
import io.github.chlorojoules.*;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiContainer;
import net.minecraft.client.renderer.world.RenderEngine;
import net.minecraft.client.renderer.world.RenderHelper;
import net.minecraft.client.renderer.world.Tessellator;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.block.icon.Icon;
import net.minecraft.common.block.texture.Face;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.Ingredient;
import net.minecraft.common.util.JsonUtils;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Logger;

import static io.github.chlorojoules.CJRarityInfo.raritySufficient;
import static io.github.chlorojoules.gui.CJGuiGravity.*;
import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;

public class CJMachineBuilder {
	public static final int FUEL_TANK_SIZE = 2 * CJTank.BUCKET;

	public String name;
	public CJRarity rarity;
	public Class<? extends CJIMachine> machineImpl =
			CJMachineRecipeConsumer.class;

	public boolean doDropMeta = false;
	public boolean hasRiftView = false;

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

	public FoxPowerType powerType = FoxPowerType.RECEIVER;

	private static FoxPowerType powerTypeFromString(String string) {
		return switch(string) {
			case "generator" -> FoxPowerType.PRODUCER;
			case "cable" -> FoxPowerType.TRANSMITTER;
			case "battery" -> FoxPowerType.STORAGE;
			// case "receiver" -> FoxPowerType.RECEIVER;
			default -> FoxPowerType.RECEIVER;
		};
	}

	@SuppressWarnings("unchecked")
	public CJMachineBuilder(String jsonPath) {
		JsonObject root = CJMod.jsonAsset(jsonPath);

		name = JsonUtils.getString(root, "name");
		rarity = CJRarityInfo.fromString(
				JsonUtils.getString(root, "rarity"));

		if(root.has("tier")) {
			tier = CJMachineTier.fromString(
					JsonUtils.getString(root, "tier"));
		}

		if(root.has("fox_transmission")) {
			powerType = powerTypeFromString(
					JsonUtils.getString(root, "fox_transmission"));
		}

		if(root.has("rift")) {
			hasRiftView = JsonUtils.getBoolean(root, "rift");
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
							.setAllowedItems(new Ingredient[] {
									CJMod.tagJewel
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

	public CJMachineSlotInfo getNamedSlot(String id) {
		for(CJMachineSlotInfo slot : slots) {
			if(slot.id == null) continue;
			if(slot.id.equals(id)) return slot;
		}

		throw new RuntimeException("Unknown slot id '" + id + "'");
	}

	public boolean hasNamedSlot(String id) {
		try {
			getNamedSlotIndex(id);
			return true;
		}
		catch(RuntimeException e) {
			return false;
		}
	}

	public ItemStack getNamedStack(
			CJTileEntityMachineBase machineEntity, String id) {

		return machineEntity.getStackInSlot(getNamedSlotIndex(id));
	}

	public void setNamedStack(
			CJTileEntityMachineBase machineEntity, String id,
			ItemStack stack) {

		machineEntity.setInventorySlotContents(getNamedSlotIndex(id), stack);
	}

	public boolean getNamedButtonState(
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

	public CJTank getNamedTank(String id) {
		for(CJTank tank : tanks) {
			if(tank.id == null) continue;
			if(tank.id.equals(id)) return tank;
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

	public CJTankVolume getNamedTankVolume(
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

				machineEntity.errorMessage = StringTranslate.getInstance()
						.translateKey("message.cj_full");

				return true;
			}

			return volume.fluidID != component.volume.fluidID;
		}
		else {
			ItemStack stack = getNamedStack(machineEntity, component.targetID);

			if(stack == null) return input;
			if(!input &&
					stack.getMaxStackSize() - stack.stackSize <
							component.getStackSize()) {

				machineEntity.errorMessage = StringTranslate.getInstance()
						.translateKey("message.cj_full");

				return true;
			}

			return !CJMod.matchIngredientLenient(stack, component.item);
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

					boolean isPrimal =
							(machineEntity.jewelRarity == CJRarity.PRIMAL);

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
							machineEntity.errorMessage =
									"message.cj_no_optional";

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
							machineEntity.errorMessage =
									"message.cj_no_optional";

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
			if(!raritySufficient(
					machineEntity.jewelRarity, recipe.requiredRarity)) {

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
		int timeScale =
				CJRarityInfo.getRarityTimeScale(machineEntity.jewelRarity);

		machineEntity.operationLength = recipe.processTime / timeScale;
		if(machineEntity.isPassive) machineEntity.operationLength *= 2;
		if(machineEntity.operationTicks++ < machineEntity.operationLength) {
			return;
		}

		// Handle fuel separately from other fluid inputs.
		int powerScale =
				CJRarityInfo.getRarityPowerScale(machineEntity.jewelRarity);

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
				ItemStack outputStack =
						getNamedStack(machineEntity, component.targetID);

				ItemStack componentStack = (ItemStack) component.item;

				if(outputStack == null) {
					setNamedStack(
							machineEntity, component.targetID,
							componentStack.copy());
				}
				else outputStack.stackSize += componentStack.stackSize;
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

	public static void renderIconClipped(
			int x, int y, Icon icon, int width, int height, float z) {

		Tessellator t = Tessellator.instance;
		Tessellator.instance.startDrawingQuads();

		float w = icon.getMaxU() - icon.getMinU();
		float h = icon.getMaxV() - icon.getMinV();
		float maxU = icon.getMinU() + (w * (width / 16F));
		float maxV = icon.getMinV() + (h * (height / 16F));

		t.addVertexWithUV(x, y + height, z, icon.getMinU(), maxV);
		t.addVertexWithUV(x + width, y + height, z, maxU, maxV);
		t.addVertexWithUV(x + width, y, z, maxU, icon.getMinV());
		t.addVertexWithUV(x, y, z, icon.getMinU(), icon.getMinV());

		t.draw();
	}

	public static boolean guiPointInRect(
			GuiContainer<?> gui, int xSize, int ySize, int mouseX, int mouseY,
			int x, int y, int width, int height) {

		int guiWidth = (gui.width - xSize) / 2;
		int guiHeight = (gui.height - ySize) / 2;

		mouseX -= guiWidth;
		mouseY -= guiHeight;

		return mouseX >= x
				&& mouseX < x + width
				&& mouseY >= y
				&& mouseY < y + height;
	}

	public boolean getIsMouseOverProgressBar(
			GuiContainer<?> container, int xSize, int ySize, int x, int y) {

		if(progressBar == null) return false;

		return guiPointInRect(
				container, xSize, ySize, x, y,
				progressBar.getXPlacement(), progressBar.getYPlacement(),
				PROGRESS_WIDTH, PROGRESS_HEIGHT);
	}

	public static boolean getIsMouseOverSlot(
			GuiContainer<?> container, int xSize, int ySize,
			CJMachineSlotInfo slot, int x, int y) {

		return guiPointInRect(
				container, xSize, ySize,
				x + SLOT_IN_OFFSET_X, y + SLOT_IN_OFFSET_Y,
				slot.getXPlacement(), slot.getYPlacement(),
				SLOT_IN_WIDTH - SLOT_IN_OFFSET_X,
				SLOT_IN_HEIGHT - SLOT_IN_OFFSET_Y);
	}

	public static boolean getIsMouseOverTank(
			GuiContainer<?> container, int xSize, int ySize, CJTank tank,
			int x, int y) {

		return guiPointInRect(
				container, xSize, ySize,
				x + FLUID_OFFSET_X, y + FLUID_OFFSET_Y,
				tank.getXPlacement(), tank.getYPlacement(),
				FLUID_WIDTH - FLUID_OFFSET_X, FLUID_HEIGHT - FLUID_OFFSET_Y);
	}

	public static void drawTooltipRect(
			float minX, float minY, float maxX, float maxY) {

		RenderSystem.disableTexture2D();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderSystem.useSmoothShadeModel();

		Tessellator t = Tessellator.instance;
		Tessellator.instance.startDrawingQuads();

		t.setColorRGBA_F(0.0f, 0.0f, 0.0f, 0xC0 / (float) 0xFF);
		t.addVertex(maxX, minY, 0.0f);
		t.addVertex(minX, minY, 0.0f);
		t.addVertex(minX, maxY, 0.0f);
		t.addVertex(maxX, maxY, 0.0f);
		t.draw();

		RenderSystem.useFlatShadeModel();
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture2D();
	}

	public static void drawTooltip(
			String name, String description, int x, int y, int titleColor) {

		Minecraft mc = Minecraft.getInstance();
		FontRenderer fontRenderer = mc.fontRenderer;

		int nameWidth = fontRenderer.getStringWidth(name);
		int boxHeight = TOOLTIP_HEIGHT;

		int descriptionWidth = 0;
		if(description != null) {
			descriptionWidth = fontRenderer.getStringWidth(description);
			boxHeight += TOOLTIP_DESCRIPTION_HEIGHT;
		}

		int textWidth = Math.max(nameWidth, descriptionWidth);

		int xSlot = x + TOOLTIP_OFFSET_X;
		int ySlot = y - TOOLTIP_OFFSET_Y;

		RenderSystem.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();

		drawTooltipRect(
				xSlot - TOOLTIP_BORDER, ySlot - TOOLTIP_BORDER,
				xSlot + textWidth + TOOLTIP_BORDER, ySlot + boxHeight);

		if(description != null) {
			fontRenderer.drawStringWithShadow(
					description, xSlot, ySlot + TOOLTIP_DESCRIPTION_OFFSET,
					Color.GRAY.getRGB());
		}

		fontRenderer.drawStringWithShadow(name, xSlot, ySlot, titleColor);

		RenderSystem.enableLighting();
		RenderSystem.enableDepthTest();
	}

	public static void drawTankWithVolume(
			Gui gui, int baseX, int baseY, CJTank tank, CJTankVolume volume,
			float z) {

		Minecraft mc = Minecraft.getInstance();
		RenderEngine renderEngine = mc.renderEngine;

		int tankX = baseX + tank.getXPlacement();
		int tankY = baseY + tank.getYPlacement();

		gui.drawTexturedModalRect(
				tankX, tankY, FLUID_FULL_X, FLUID_FULL_Y,
				FLUID_WIDTH, FLUID_HEIGHT);

		if(volume.fluidID != CJMod.fuelFluid) {
			Icon icon = Blocks.BLOCKS_LIST[volume.fluidID].getIcon(
					Face.TOP.direction(), 0);

			int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
			int fluidTexture = renderEngine.getTexture("/terrain.png");
			renderEngine.bindTexture(fluidTexture);

			for(int j = 0; j < FLUID_HEIGHT - 2; j += 16) {
				renderIconClipped(
						tankX + 1, tankY + 1 + j, icon, FLUID_WIDTH - 2,
						Math.min(16, FLUID_HEIGHT - 2 - j), z);
			}

			renderEngine.bindTexture(texture);
		}

		// Overlay the full tank graphic with an amount of the empty one.
		int tankFill = (volume.current * FLUID_HEIGHT) / volume.max;
		if(volume.current > 0) tankFill = Math.max(2, tankFill);
		int tankEmptyDrawHeight = FLUID_HEIGHT - tankFill;

		gui.drawTexturedModalRect(
				tankX, tankY,
				FLUID_EMPTY_X, FLUID_EMPTY_Y,
				FLUID_WIDTH, tankEmptyDrawHeight);
	}

	public void drawProgressBar(
			Gui gui, int baseX, int baseY, int ticks, int length) {

		if(progressBar == null) return;

		// Draw progress bar.
		int x = baseX + progressBar.getXPlacement();
		int y = baseY + progressBar.getYPlacement();

		gui.drawTexturedModalRect(
				x, y, PROGRESS_EMPTY_X, PROGRESS_EMPTY_Y,
				PROGRESS_WIDTH, PROGRESS_HEIGHT);

		gui.drawTexturedModalRect(
				x, y, PROGRESS_FULL_X, PROGRESS_FULL_Y,
				(ticks * PROGRESS_WIDTH) / length, PROGRESS_HEIGHT);
	}

	public static void drawSlot(
			Gui gui, int baseX, int baseY, CJMachineSlotInfo slot) {

		int slotX = baseX + slot.getXPlacement();
		int slotY = baseY + slot.getYPlacement();

		if(slot.output) {
			gui.drawTexturedModalRect(
					slotX - SLOT_OUT_OFFSET_X, slotY - SLOT_OUT_OFFSET_Y,
					SLOT_OUT_X, SLOT_OUT_Y,
					SLOT_OUT_WIDTH, SLOT_OUT_HEIGHT);
		}
		else {
			int inX = SLOT_IN_X;
			int inY = SLOT_IN_Y;

			switch (slot.renderType) {
				case CJMachineSlotRenderType.JEWEL: {
					inX = SLOT_JEWEL_X;
					inY = SLOT_JEWEL_Y;
					break;
				}

				case CJMachineSlotRenderType.FERTILIZER: {
					inX = SLOT_FERTILIZER_X;
					inY = SLOT_FERTILIZER_Y;
					break;
				}

				case CJMachineSlotRenderType.PASTE: {
					inX = SLOT_PASTE_X;
					inY = SLOT_PASTE_Y;
					break;
				}

				case CJMachineSlotRenderType.OTHERWORLD: {
					inX = SLOT_OTHERWORLD_X;
					inY = SLOT_OTHERWORLD_Y;
					break;
				}

				case CJMachineSlotRenderType.FILTER: {
					inX = SLOT_FILTER_X;
					inY = SLOT_FILTER_Y;
					break;
				}
			}

			gui.drawTexturedModalRect(
					slotX - SLOT_IN_OFFSET_X, slotY - SLOT_IN_OFFSET_Y,
					inX, inY, SLOT_IN_WIDTH, SLOT_IN_HEIGHT);
		}
	}

	public static void drawGui(
			Gui gui, int baseX, int baseY, int xSize, int ySize) {

		Minecraft mc = Minecraft.getInstance();
		RenderEngine renderEngine = mc.renderEngine;

		int texture = renderEngine.getTexture("/gui/cj_machinebase.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		renderEngine.bindTexture(texture);

		gui.drawTexturedModalRect(baseX, baseY, 0, 0, xSize, ySize);
	}
}
