package io.github.chlorojoules.machine;

import com.indigo3d.util.RenderSystem;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import net.minecraft.client.renderer.world.Tessellator;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.entity.other.EntityItem;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;
import org.lwjgl.opengl.GL11;

class CJMachineRiftBeaconStorage {
	int riftTicks = 0;
}

@SuppressWarnings("unused")
public class CJMachineRiftBeacon implements CJIMachine {
	private static final int BAND_DIVISOR = 8;
	private static final int TIME_SCALE = 8;
	private static final int Y_OFFSET = 4;
	private static final double CAP = 0.8f;
	private static final int CUTSCENE_THRESHOLD = 80;

	private static CJMachineRiftBeaconStorage getStorage(
			CJTileEntityMachineBase machineEntity) {

		if(machineEntity.machineStorage == null) {
			machineEntity.machineStorage = new CJMachineRiftBeaconStorage();
		}

		return (CJMachineRiftBeaconStorage) machineEntity.machineStorage;
	}

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
			CJMachineRiftBeaconStorage storage = getStorage(machineEntity);

			for(Entity entity : machineEntity.worldObj.loadedEntityList) {
				double dx = (machineEntity.xCoord + 0.5) - entity.posX;
				double dz = (machineEntity.zCoord + 0.5) - entity.posZ;
				double dy =
						(machineEntity.yCoord + 0.5 + Y_OFFSET) - entity.posY;

				double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
				if(distance > 16.0) continue;

				if(entity instanceof EntityPlayer player && distance < 1.0) {
					if(storage.riftTicks == 0) {
						player.addChatMessage("message.cj_rift1");
					}
					else if(storage.riftTicks == CUTSCENE_THRESHOLD) {
						player.addChatMessage("message.cj_rift2");
					}
					else if(storage.riftTicks == CUTSCENE_THRESHOLD * 2) {
						player.addChatMessage("message.cj_rift3");
					}
					else if(storage.riftTicks == CUTSCENE_THRESHOLD * 3) {
						player.addChatMessage("message.cj_rift4");
					}
					else if(storage.riftTicks == CUTSCENE_THRESHOLD * 4) {
						EntityItem item = new EntityItem(
								machineEntity.worldObj,
								machineEntity.xCoord,
								machineEntity.yCoord + Y_OFFSET,
								machineEntity.zCoord,
								new ItemStack(CJMod.otherworld));

						machineEntity.worldObj.entityJoinedWorld(item);

						machineEntity.worldObj.setBlockWithNotify(
								machineEntity.xCoord,
								machineEntity.yCoord + 1,
								machineEntity.zCoord,
								Blocks.AIR.blockID);

						machineEntity.operationTicks = 0;
						return;
					}

					storage.riftTicks++;
				}

				dx /= distance * 16.0;
				dy /= distance * 16.0;
				dz /= distance * 16.0;

				if(entity.motionX < CAP) entity.addVelocity(dx, 0, 0);
				if(entity.motionY < CAP) entity.addVelocity(0, dy, 0);
				if(entity.motionZ < CAP) entity.addVelocity(0, 0, dz);
			}

			return;
		}

		int blockID = machineEntity.worldObj.getBlockId(
				machineEntity.xCoord,
				machineEntity.yCoord + 1,
				machineEntity.zCoord);

		if(blockID != CJMod.rift.blockID) {
			machineEntity.operationTicks = 0;
			machineEntity.errorMessage =
					translate.translateKey("message.cj_no_rift");

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

		if(fuelVolume.current <= 0) {
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
		fuelVolume.removeFluid(0, TIME_SCALE / 2, false);
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
		GL11.glTranslated(x + 0.5, y + 0.5 + Y_OFFSET, z + 0.5);

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
