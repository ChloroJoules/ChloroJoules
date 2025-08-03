package io.github.chlorojoules.block;

import net.minecraft.common.block.Block;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.world.World;

import java.util.Random;

public class CJBlockRift extends Block {
	public CJBlockRift(String name) {
		super(name, Materials.METAL);
	}

	private static String nextParticle(Random random) {
		if(random.nextFloat() < 0.1) return "caustic_blood_large";

		return "portal";
	}

	@Override
	public void randomDisplayTick(
			World world, int x, int y, int z, Random random) {

		final float v = 1.0f;

		world.spawnParticle(nextParticle(random), x, y, z, v, v, v);
		world.spawnParticle(nextParticle(random), x, y, z + 1, v, v, -v);
		world.spawnParticle(nextParticle(random), x, y, z, v, -v, v);
		world.spawnParticle(nextParticle(random), x, y, z + 1, v, -v, -v);
		world.spawnParticle(nextParticle(random), x + 1, y, z, -v, v, v);
		world.spawnParticle(nextParticle(random), x + 1, y, z + 1, -v, v, -v);
		world.spawnParticle(nextParticle(random), x + 1, y, z, -v, -v, v);
		world.spawnParticle(nextParticle(random), x + 1, y, z + 1, -v, -v, -v);
	}
}
