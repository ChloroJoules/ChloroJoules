package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.util.i18n.StringTranslate;
import net.minecraft.common.world.World;

public class CJItemRiftIdentifier extends Item {
	private static final int RIFT_MIN = 128;
	private static final int RIFT_JITTER = 25;
	private static final int RIFT_RANGE = 8;

	public CJItemRiftIdentifier(String name) {
		super(name);
	}

	private static int signNoZero(int n) {
		int ret = Integer.signum(n);
		return ret == 0 ? 1 : ret;
	}

	private static int blockDist(
			int ax, int ay, int az, int bx, int by, int bz) {

		int dx = ax - bx;
		int dy = ay - by;
		int dz = az - bz;

		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	@Override
	public ItemStack onItemRightClick(
			ItemStack itemstack, World world, EntityPlayer player) {

		StringTranslate translate = StringTranslate.getInstance();

		if(player.dimension != 0) {
			CJMod.sendChat(translate.translateKey(
					"message.cj_unstable_dimension"));

			return itemstack;
		}

		int blockX = (int) Math.round(player.posX);
		int blockY = (int) Math.round(player.posY);
		int blockZ = (int) Math.round(player.posZ);

		int riftX;
		int riftY;
		int riftZ;

		int absX = Math.abs(blockX);
		int absZ = Math.abs(blockZ);

		if(absX < RIFT_MIN) riftX = RIFT_MIN;
		else riftX = 1 << Math.round(Math.log10(absX) / Math.log10(2));
		if(absZ < RIFT_MIN) riftZ = RIFT_MIN;
		else riftZ = 1 << Math.round(Math.log10(absZ) / Math.log10(2));

		riftY = (int) ((Math.cos(riftX + riftZ) * 0.5 + 0.6) * RIFT_JITTER);

		int riftXHold = riftX;
		riftX += (int) (Math.cos(riftZ) * RIFT_JITTER);
		riftZ += (int) (Math.cos(riftXHold) * RIFT_JITTER);

		riftX *= signNoZero(blockX);
		riftZ *= signNoZero(blockZ);

		int dist = blockDist(riftX, riftY, riftZ, blockX, blockY, blockZ);
		if(dist < RIFT_RANGE) {
			if(world.getBlockId(riftX, riftY, riftZ) == CJMod.rift.blockID) {
				CJMod.sendChat(translate.translateKey(
						"message.cj_rift_active"));
			}
			else {
				CJMod.sendChat(translate.translateKey(
						"message.cj_activate_rift"));

				world.createExplosion(null, riftX, riftY, riftZ, 6.0F);
				world.setBlockWithNotify(
						riftX, riftY, riftZ, CJMod.rift.blockID);
			}
		}
		else {
			CJMod.sendChat(translate.translateKeyFormat(
					"message.cj_rift_locate", dist));
		}

		return itemstack;
	}
}
