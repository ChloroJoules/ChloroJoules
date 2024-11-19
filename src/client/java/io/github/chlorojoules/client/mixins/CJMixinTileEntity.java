package io.github.chlorojoules.client.mixins;

import net.minecraft.src.game.block.tileentity.TileEntity;

import com.fox2code.foxloader.registry.RegisteredTileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntity.class)
public class CJMixinTileEntity implements RegisteredTileEntity {
	@Inject(method = "<clinit>", at = @At("HEAD"))
	private static void staticInitializerInjector(CallbackInfo ci) {
		throw new RuntimeException("Hello?");
	}

	/*@Accessor("nameToClassMap")
	public static Map<String, Class<?>> getNameToClassMap() {
		throw new AssertionError();
	}

	@Accessor("classToNameMap")
	public static Map<String, Class<?>> getClassToNameMap() {
		throw new AssertionError();
	}

	@Overwrite
	public static TileEntity createAndLoadEntity(
			NBTTagCompound nbt) {

		String id = nbt.getString("id");

		try {
			Class<?> entityType = getNameToClassMap().get(id);
			TileEntity tileEntity = (TileEntity) entityType.newInstance();

			tileEntity.readFromNBT(nbt);

			return tileEntity;
		}
		catch(Exception e) {
			Logger.getLogger("ChloroJoules").log(Level.SEVERE, "", e);

			return null;
		}
	}*/
}
