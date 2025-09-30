package io.github.chlorojoules.mixins;

import io.github.chlorojoules.CJMod;
import net.minecraft.client.stats.StatFileWriter;
import net.minecraft.client.stats.StatsIO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(StatsIO.class)
public class MixinStatsIO {
	@Inject(method = "readStats", at = @At("HEAD"))
	private void readStats(
			File data, File temp, File old,
			CallbackInfoReturnable<StatFileWriter> cir) {

		CJMod.installAchievements();
	}
}
