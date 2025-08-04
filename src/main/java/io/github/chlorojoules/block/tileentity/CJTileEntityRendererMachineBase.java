package io.github.chlorojoules.block.tileentity;

import net.minecraft.client.renderer.block.tileentity.TileEntityRenderer;

public class CJTileEntityRendererMachineBase extends TileEntityRenderer<CJTileEntityMachineBase> {
	@Override
	public void renderTileEntityAt(
			CJTileEntityMachineBase machineEntity,
			double x, double y, double z, float deltaTicks, int progress) {

		if(machineEntity.impl == null) return;

		machineEntity.impl.renderTileEntityAt(
				machineEntity, x, y, z, deltaTicks, progress);
	}
}
