package io.github.chlorojoules.item;

import com.mojang.nbt.CompoundTag;
import io.github.chlorojoules.CJTankVolume;
import net.minecraft.common.block.Blocks;
import net.minecraft.common.item.ItemStack;
import net.minecraft.common.item.description.ItemDesc;
import net.minecraft.common.util.i18n.StringTranslate;
import net.minecraft.common.world.World;

import java.util.List;

public class CJItemDescriptionFluidContainer implements ItemDesc {
	@Override
	public void runDesc(World world, List<String> desc, ItemStack item) {
		if(!(item.getItem() instanceof CJItemFluidContainer)) return;

		CJTankVolume volume = new CJTankVolume();
		CompoundTag tagCompound = item.getTagCompound();
		if(tagCompound != null) volume.readFromNBT(tagCompound);

		String name = StringTranslate.getInstance()
				.translateKey("message.cj_empty_fluid");

		if(volume.fluidID != 0) {
			name = Blocks.BLOCKS_LIST[volume.fluidID].translateBlockName();
		}

		desc.add(name);
		desc.add(volume.current + "/" + volume.max + "mB");
	}
}
