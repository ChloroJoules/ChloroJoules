package io.github.chlorojoules.gui;

import com.indigo3d.util.RenderSystem;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarityInfo;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.CJBlockMachineBase;
import io.github.chlorojoules.container.CJContainerGuide;
import io.github.chlorojoules.machine.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiContainer;
import net.minecraft.client.renderer.world.RenderHelper;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.recipe.Ingredient;
import net.minecraft.common.recipe.TaggedIngredient;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.*;
import static io.github.chlorojoules.gui.CJGuiMachineBaseLayout.BUTTON_LABEL_INSET;

public class CJGuiGuide extends GuiContainer<CJContainerGuide> {
	CJBlockMachineBase currentMachine = CJMod.pulverizer;
	int currentRecipe = 0;

	ArrayList<CJBlockMachineBase> machines = new ArrayList<>();
	ArrayList<CJGuiButton> buttons = new ArrayList<>();

	private boolean wasMousePressed = false;
	private boolean wasLeftPressed = false;
	private boolean wasRightPressed = false;

	public CJGuiGuide() {
		super(new CJContainerGuide());

		int column = 0;
		int row = 0;

		for(CJBlockMachineBase machine : CJMod.machines.values()) {
			Class<? extends CJIMachine> impl =
					machine.machineBuilder.machineImpl;

			if(impl == null ||
					!CJMachineRecipeConsumer.class.isAssignableFrom(impl)) {

				continue;
			}

			machines.add(machine);

			buttons.add(new CJGuiButton(
					"tile." + machine.machineBuilder.name + ".name", machine,
					column * (BUTTON_WIDTH + 3) + PLAYER_INVENTORY_X,
					row * (BUTTON_HEIGHT + 3) + PLAYER_INVENTORY_Y));

			column++;
			if (column >= 7) {
				column = 0;
				row++;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		StringTranslate translate = StringTranslate.getInstance();

		String name = translate.translateKey(
				"tile." + currentMachine.machineBuilder.name + ".name");

		// Machine label.
		fontRenderer.drawString(
				name, xSize / 2 - fontRenderer.getStringWidth(name) / 2, 6,
				CJGuiMachineBase.MACHINE_TEXT);

		String page = (currentRecipe + 1) + "/" +
				currentMachine.machineBuilder.recipes.size();

		fontRenderer.drawString(
				page, xSize - fontRenderer.getStringWidth(page) - 6, 6,
				CJGuiMachineBase.MACHINE_TEXT);
	}

	private ItemStack getTaggedDisplayStack(TaggedIngredient ingredient) {
		List<Ingredient> ingredients = ingredient.getIngredients();
		int select = (mc.ticksRan / 20) % ingredients.size();
		Ingredient displayIngredient = ingredients.get(select);

		return (ItemStack) displayIngredient;
	}

	private void drawMachineRecipeComponentTooltip(
			CJMachineBuilder machineBuilder,
			CJMachineRecipeComponent recipeComponent, int mouseX, int mouseY) {

		switch(recipeComponent.target) {
			case SLOT: {
				if(CJMachineBuilder.getIsMouseOverSlot(
						this, xSize, ySize,
						machineBuilder.getNamedSlot(recipeComponent.targetID),
						mouseX, mouseY)) {

					ItemStack stack;

					if(recipeComponent.item instanceof
							TaggedIngredient ingredient) {

						stack = getTaggedDisplayStack(ingredient);
					}
					else if(recipeComponent.item instanceof
							ItemStack componentStack) {

						stack = componentStack;
					}
					else return;

					RenderSystem.disableLighting();
					RenderSystem.disableDepthTest();

					this.drawItemTooltip(stack, mouseX, mouseY);

					RenderSystem.enableLighting();
					RenderSystem.enableDepthTest();
				}
				break;
			}

			case TANK: {
				if(CJMachineBuilder.getIsMouseOverTank(
						this, xSize, ySize,
						machineBuilder.getNamedTank(recipeComponent.targetID),
						mouseX, mouseY)) {

					String name = StringTranslate.getInstance().translateKey(
							"message.cj_empty_fluid");

					CJTankVolume volume = recipeComponent.volume;
					if(volume.fluidID != 0) {
						Block fluid = Blocks.BLOCKS_LIST[volume.fluidID];
						name = fluid.translateBlockName();
					}

					CJMachineBuilder.drawTooltip(
							name, volume.current + "/" + volume.max + "mB",
							mouseX, mouseY, -1);

					break;
				}

				break;
			}
		}
	}

	private boolean mouseState() {
		boolean returnValue = false;

		if(Mouse.isButtonDown(0)) {
			if(!wasMousePressed) {
				returnValue = true;
			}

			wasMousePressed = true;
		}
		else wasMousePressed = false;

		return returnValue;
	}

	private boolean leftState() {
		boolean returnValue = false;

		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			if(!wasLeftPressed) {
				returnValue = true;
			}

			wasLeftPressed = true;
		}
		else wasLeftPressed = false;

		return returnValue;
	}

	private boolean rightState() {
		boolean returnValue = false;

		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			if(!wasRightPressed) {
				returnValue = true;
			}

			wasRightPressed = true;
		}
		else wasRightPressed = false;

		return returnValue;
	}

	@Override
	public void drawScreen(float mouseXf, float mouseYf, float deltaTicks) {
		super.drawScreen(mouseXf, mouseYf, deltaTicks);

		int mouseX = (int) mouseXf;
		int mouseY = (int) mouseYf;

		boolean mouseState = mouseState();
		boolean leftState = leftState();
		boolean rightState = rightState();

		StringTranslate translate = StringTranslate.getInstance();

		CJMachineBuilder machineBuilder = currentMachine.machineBuilder;
		CJMachineRecipe machineRecipe =
				machineBuilder.recipes.get(currentRecipe);

		for(CJMachineRecipeComponent input : machineRecipe.inputs) {
			drawMachineRecipeComponentTooltip(
					machineBuilder, input, mouseX, mouseY);
		}

		for(CJMachineRecipeComponent output : machineRecipe.outputs) {
			drawMachineRecipeComponentTooltip(
					machineBuilder, output, mouseX, mouseY);
		}

		if(machineBuilder.hasNamedSlot("jewel")) {
			int jewel =
					CJRarityInfo.getRarityJewel(machineRecipe.requiredRarity);

			if(CJMachineBuilder.getIsMouseOverSlot(
					this, xSize, ySize,
					machineBuilder.getNamedSlot("jewel"),
					mouseX, mouseY)) {

				RenderSystem.disableLighting();
				RenderSystem.disableDepthTest();

				this.drawItemTooltip(new ItemStack(jewel, 1), mouseX, mouseY);

				RenderSystem.enableLighting();
				RenderSystem.enableDepthTest();
			}
		}

		if(machineBuilder.getIsMouseOverProgressBar(
				this, xSize, ySize, mouseX, mouseY)) {

			CJMachineBuilder.drawTooltip(
					translate.translateKey("message.cj_ticks"),
					"" + machineRecipe.processTime,
					mouseX, mouseY, -1);
		}

		int i = 0;
		for(CJBlockMachineBase machine : machines) {
			CJGuiButton button = buttons.get(i++);

			if(CJMachineBuilder.guiPointInRect(
					this, xSize, ySize, mouseX, mouseY,
					button.getXPlacement(), button.getYPlacement(),
					BUTTON_WIDTH, BUTTON_HEIGHT)) {

				String tooltip = button.tooltip;

				CJMachineBuilder.drawTooltip(
						translate.translateKey(tooltip), null,
						mouseX, mouseY, Color.WHITE.getRGB());

				if(mouseState) {
					this.mc.sndManager.playSoundFX(
							"random.click", 1.0F, 1.0F);

					currentMachine = machine;
					currentRecipe = 0;
					return;
				}
			}
		}

		if(mouseState || rightState) {
			this.mc.sndManager.playSoundFX(
					"random.click", 1.0F, 1.0F);

			currentRecipe++;

			if(currentRecipe >= machineBuilder.recipes.size()) {
				currentRecipe = 0;
			}
		}
		else if(leftState) {
			this.mc.sndManager.playSoundFX(
					"random.click", 1.0F, 1.0F);

			currentRecipe--;

			if(currentRecipe < 0) {
				currentRecipe = machineBuilder.recipes.size();
			}
		}
	}

	private void drawItemInSlot(
			CJMachineBuilder machineBuilder, String slot, ItemStack stack,
			int baseX, int baseY) {

		CJMachineSlotInfo machineSlot = machineBuilder.getNamedSlot(slot);

		int slotX = baseX + machineSlot.getXPlacement();
		int slotY = baseY + machineSlot.getYPlacement();

		RenderSystem.enableDepthTest();
		RenderHelper.enableStandardItemLighting();
		RenderSystem.disableLighting();

		if(stack.itemDamage < 0) stack.itemDamage = 0;

		itemRenderer.renderItemIntoGUI(
				fontRenderer, mc.renderEngine, stack, slotX, slotY);

		itemRenderer.renderItemOverlayIntoGUITextColor(
				this.fontRenderer, this.mc.renderEngine, stack, slotX, slotY,
				0xFFFFFF);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
	}

	private void drawMachineRecipeComponent(
			CJMachineBuilder machineBuilder,
			CJMachineRecipeComponent recipeComponent, int baseX, int baseY) {

		Minecraft mc = Minecraft.getInstance();

		switch(recipeComponent.target) {
			case SLOT: {
				ItemStack stack;

				if(recipeComponent.item instanceof
						TaggedIngredient ingredient) {

					stack = getTaggedDisplayStack(ingredient);
				}
				else if(recipeComponent.item instanceof
						ItemStack componentStack) {

					stack = componentStack;
				}
				else return;

				drawItemInSlot(
						machineBuilder, recipeComponent.targetID, stack,
						baseX, baseY);

				break;
			}

			case TANK: {
				CJMachineBuilder.drawTankWithVolume(
						this, baseX, baseY,
						machineBuilder.getNamedTank(recipeComponent.targetID),
						recipeComponent.volume, zLevel);

				break;
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float deltaTicks) {
		int baseX = (width - xSize) / 2;
		int baseY = (height - ySize) / 2;

		CJMachineBuilder.drawGui(this, baseX, baseY, xSize, ySize);
		int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

		CJMachineBuilder machineBuilder = currentMachine.machineBuilder;
		CJMachineRecipe machineRecipe =
				machineBuilder.recipes.get(currentRecipe);

		for(CJMachineSlotInfo slot : machineBuilder.slots) {
			CJMachineBuilder.drawSlot(this, baseX, baseY, slot);
		}

		for(int i = 0; i < machineBuilder.tanks.size(); i++) {
			CJTank tank = machineBuilder.tanks.get(i);
			CJTankVolume volume = new CJTankVolume();

			CJMachineBuilder.drawTankWithVolume(
					this, baseX, baseY, tank, volume, zLevel);
		}

		machineBuilder.drawProgressBar(this, baseX, baseY, 0, 100);

		for(CJMachineRecipeComponent input : machineRecipe.inputs) {
			mc.renderEngine.bindTexture(texture);
			drawMachineRecipeComponent(machineBuilder, input, baseX, baseY);
		}

		for(CJMachineRecipeComponent output : machineRecipe.outputs) {
			mc.renderEngine.bindTexture(texture);
			drawMachineRecipeComponent(machineBuilder, output, baseX, baseY);
		}

		if(machineBuilder.hasNamedSlot("jewel")) {
			int jewel =
					CJRarityInfo.getRarityJewel(machineRecipe.requiredRarity);

			drawItemInSlot(
					machineBuilder, "jewel", new ItemStack(jewel, 1),
					baseX, baseY);
		}


		for(CJGuiButton button : buttons) {
			int x = baseX + button.getXPlacement();
			int y = baseY + button.getYPlacement();

			mc.renderEngine.bindTexture(texture);
			drawTexturedModalRect(
					x, y, BUTTON_INACTIVE_X, BUTTON_INACTIVE_Y,
					BUTTON_WIDTH, BUTTON_HEIGHT);

			x += BUTTON_LABEL_INSET;
			y += BUTTON_LABEL_INSET;

			itemRenderer.renderItemIntoGUI(
					this.fontRenderer, mc.renderEngine, button.label,
					x, y);

			mc.renderEngine.bindTexture(texture);
		}
	}
}
