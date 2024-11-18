package io.github.chlorojoules.client.mixins;

import net.minecraft.src.game.block.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

// TODO: Cannot get this to work -- just going to have to work without
//       Machine serialization until we can work this out.
@Mixin(TileEntity.class)
public interface CJMixinTileEntity {}
