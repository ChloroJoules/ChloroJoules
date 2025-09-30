package io.github.chlorojoules.mixins;

import net.minecraft.client.renderer.entity.RenderBiped;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderBiped.class)
public class MixinRenderBiped {
	@Shadow
	protected static final String[] ARMOR_FILENAME_PREFIX = new String[]{
			"leather", "chain", "iron", "diamond", "gold", "cloth",
			"cj_untethered"
	};
}
