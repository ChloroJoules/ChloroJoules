package io.github.chlorojoules.machine;

import com.indigo3d.util.RenderSystem;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.client.renderer.world.Tessellator;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class CJMachineRiftBeacon implements CJIMachine {
	private static final int BAND_DIVISOR = 5;
	private static final int TIME_SCALE = 8;

	@Override
	public void updateMachine(CJTileEntityMachineBase machineEntity) {
		StringTranslate translate = StringTranslate.getInstance();

		CJMachineBuilder machineBuilder = machineEntity.getBuilder();

		ItemStack jewelStack =
				machineBuilder.getNamedStack(machineEntity, "jewel");

		CJTankVolume fuelVolume =
				machineBuilder.getNamedTankVolume(machineEntity, "fuel");

		CJTankVolume soulVolume =
				machineBuilder.getNamedTankVolume(machineEntity, "soul");

		CJTankVolume etchingVolume =
				machineBuilder.getNamedTankVolume(machineEntity, "etching");

		machineEntity.errorMessage = null;
		machineEntity.isWarning = false;
		machineEntity.isPassive = false;
		machineEntity.operationLength = soulVolume.max / TIME_SCALE;
		machineEntity.riftDensity = machineEntity.operationTicks /
				(machineEntity.operationLength / BAND_DIVISOR);

		if(machineEntity.operationTicks >= machineEntity.operationLength) {
			return;
		}

		if(jewelStack == null) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_jewel");

			return;
		}

		CJRarity rarity = CJRarityInfo.getJewelRarity(jewelStack.getItemID());
		if(rarity != CJRarity.AWAKENED) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_poor_jewel");

			return;
		}

		if(fuelVolume.current < fuelVolume.max) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_power");

			return;
		}

		if(soulVolume.current <= 0) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_resource");

			return;
		}

		if(etchingVolume.current <= 0) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_resource");

			return;
		}

		soulVolume.removeFluid(0, TIME_SCALE, false);
		etchingVolume.removeFluid(0, TIME_SCALE, false);
		machineEntity.operationTicks++;
	}

	private void lineCube(Tessellator tess) {
		final double[] vertices = new double[] {
				 1.0f,  1.0f,  1.0f,
				 1.0f,  1.0f, -1.0f,
				 1.0f, -1.0f,  1.0f,
				 1.0f, -1.0f, -1.0f,
				-1.0f,  1.0f,  1.0f,
				-1.0f,  1.0f, -1.0f,
				-1.0f, -1.0f,  1.0f,
				-1.0f, -1.0f, -1.0f
		};

		final int[] indices = new int[] {
				0, 1,
				0, 2,
				2, 3,
				3, 1,

				4, 5,
				4, 6,
				6, 7,
				7, 5,

				0, 4,
				1, 5,
				2, 6,
				3, 7
		};

		for(int i = 0; i < indices.length; i += 2) {
			int indexBase = indices[i] * 3;
			tess.addVertex(
					vertices[indexBase],
					vertices[indexBase + 1],
					vertices[indexBase + 2]);

			indexBase = indices[i + 1] * 3;
			tess.addVertex(
					vertices[indexBase],
					vertices[indexBase + 1],
					vertices[indexBase + 2]);
		}

		tess.draw();
	}

	public void renderTileEntityAt(
			CJTileEntityMachineBase machineEntity,
			double x, double y, double z, float deltaTicks, int progress) {

		machineEntity.renderDelta += deltaTicks;

		Tessellator tess = Tessellator.instance;

		RenderSystem.disableTexture2D();
		RenderSystem.disableLighting();
		RenderSystem.enableDepthMask();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_LINES);

		GL11.glLineWidth(4.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW_MATRIX);
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 3.5, z + 0.5);

		for(int i = 0; i < machineEntity.riftDensity; ++i) {
			tess.startDrawing(GL11.GL_LINES);

			float r = 1.0f - (i * (1.0f / BAND_DIVISOR));
			if(machineEntity.operationTicks >= machineEntity.operationLength) {
				tess.setColorRGBA_F(r, 0.0f, 0.0f, 1.0f);
			}
			else {
				tess.setColorRGBA_F(1.0f, 1.0f, 1.0f, r);
			}

			GL11.glPushMatrix();
			GL11.glRotated(
					machineEntity.renderDelta * (i + 1), 1.0, 1.0,
					Math.sin(Math.sqrt(machineEntity.renderDelta)));

			float scale = 0.65f + i * 0.15f;
			GL11.glScalef(scale, scale, scale);

			lineCube(tess);
			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();

		RenderSystem.disableBlend();
		RenderSystem.enableLighting();
		RenderSystem.enableTexture2D();
		RenderSystem.enableDepthMask();
	}
}
