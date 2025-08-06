package io.github.chlorojoules.item;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

public class CJItemJetpack extends Item implements CJIItemSocket {
	private static final int MAX_TICKS = 1;
	private static final int REQUIRED = 1;

	public CJItemJetpack(String id) {
		super(id);

		setMaxDamage(0);
		addDescription(new CJItemDescriptionSocket());
		setHasSubtypes(true);
	}

	public static float getRarityThrust(CJRarity rarity) {
		return switch(rarity) {
			case PRIMAL -> 0.1f;
			case MANUFACTURED -> 0.2f;
			case REFINED -> 0.3f;
			case AWAKENED -> 0.5f;
			default -> 0;
		};
	}

	@Override
	public void onUpdate(
			ItemStack stack, World world, Entity entity, int slot,
			boolean inHand) {

		super.onUpdate(stack, world, entity, slot, inHand);

		if(!(entity instanceof EntityPlayer player)) return;

		if(player.onGround) return;

		if(Minecraft.getInstance().gameSettings.keyBindJump.pressed) {
			CJRarity rarity =
					CJRarityInfo.getDamageRarity(stack.getItemDamage());

			boolean consumed = CJItemFluidContainer.consumeFromContainers(
					player.inventory.mainInventory, CJMod.fuelFluid,
					REQUIRED);

			if(!consumed) return;

			float thrust = getRarityThrust(rarity);
			if(player.motionY < thrust * MAX_TICKS) {
				player.addVelocity(0.0f, thrust, 0.0f);
			}

			player.fallDistance = 0.0f;
		}
	}
}
