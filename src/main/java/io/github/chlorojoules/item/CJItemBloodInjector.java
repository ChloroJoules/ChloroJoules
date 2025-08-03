package io.github.chlorojoules.item;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJRarity;
import io.github.chlorojoules.CJRarityInfo;
import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.entity.Entity;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.item.Item;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.world.World;

import java.util.ArrayList;

public class CJItemBloodInjector extends Item implements CJIItemSocket {
	private static final int REQUIRED = 35;
	private static final int COOLDOWN = 10;

	public CJItemBloodInjector(String name) {
		super(name);

		setMaxDamage(0);
		addDescription(new CJItemDescriptionSocket());
		setHasSubtypes(true);
	}

	@Override
	public void onUpdate(
			ItemStack itemstack, World world, Entity entity, int slot,
			boolean inHand) {

		super.onUpdate(itemstack, world, entity, slot, inHand);

		if(!(entity instanceof EntityPlayer player)) return;

		CompoundTag selfTag = itemstack.getTagCompoundNonNull();
		int cooldown = selfTag.getInteger("cooldown");

		selfTag.setInteger("cooldown", Math.max(--cooldown, 0));
		if(cooldown > 0) return;

		if(player.health >= 20) return;

		CJRarity rarity =
				CJRarityInfo.getDamageRarity(itemstack.getItemDamage());

		ArrayList<ItemStack> containers = new ArrayList<>();
		int consumed = 0;
		int required = REQUIRED * CJRarityInfo.getRarityPowerScale(rarity);

		while(consumed < required) {
			ItemStack container =
					CJItemFluidContainer.getContainerMatchingFluid(
							player.inventory.mainInventory,
							Blocks.SANGUIS_MOVING.blockID);

			if(container == null) return;

			CompoundTag tag = container.getTagCompound();
			CJTankVolume volume = new CJTankVolume();
			volume.readFromNBT(tag);

			consumed += Math.min(required - consumed, volume.current);
			containers.add(container);
		}

		consumed = 0;
		for(ItemStack container : containers) {
			CompoundTag tag = container.getTagCompound();
			CJTankVolume volume = new CJTankVolume();
			volume.readFromNBT(tag);

			consumed += volume.removeFluid(
					Blocks.SANGUIS_MOVING.blockID,
					Math.max(required - consumed, 0), false);

			volume.writeToNBT(tag);
		}

		player.heal(1);
		selfTag.setInteger(
				"cooldown",
				COOLDOWN / CJRarityInfo.getRarityTimeScale(rarity));
	}
}
