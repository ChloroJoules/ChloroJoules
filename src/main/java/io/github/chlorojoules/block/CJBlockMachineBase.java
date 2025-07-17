package io.github.chlorojoules.block;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.CJGuiMachineBase;
import io.github.chlorojoules.item.CJItemBlockMachineBase;
import io.github.chlorojoules.item.CJItemDescriptionModTag;
import io.github.chlorojoules.machine.CJMachineBlockSideMode;
import io.github.chlorojoules.machine.CJMachineTier;
import io.github.chlorojoules.machine.CJMachineBuilder;

import net.minecraft.client.Minecraft;

import net.minecraft.common.block.children.BlockContainer;
import net.minecraft.common.block.fluid.Fluid;
import net.minecraft.common.block.fluid.Fluids;
import net.minecraft.common.block.texture.Face;
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.entity.EntityLiving;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.entity.player.InventoryPlayer;
import net.minecraft.common.item.*;
import net.minecraft.common.item.block.ItemBlock;
import net.minecraft.common.item.children.ItemBucket;
import net.minecraft.common.util.math.MathHelper;
import net.minecraft.common.world.World;

import static io.github.chlorojoules.CJRarityInfo.getRarityColor;
import static io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase.machineEntity;

public class CJBlockMachineBase extends BlockContainer {
	public CJMachineBuilder machineBuilder;

	public CJBlockMachineBase(CJMachineBuilder machineBuilder) {
		super(machineBuilder.name, machineBuilder.tier.getMaterial());

		this.machineBuilder = machineBuilder;

		super.setBlockName(machineBuilder.name);
		super.addDescription(new CJItemDescriptionModTag());
		super.setTooltipColor(getRarityColor(machineBuilder.rarity));

		super.setHardness(machineBuilder.tier.getHardness());
		super.setResistance(machineBuilder.tier.getResistance());
		super.setSound(machineBuilder.tier.getStepSound());
		super.setEffectiveTool(machineBuilder.tier.getEffectiveTool());
	}

	@Override
	public int idPicked(World world, int x, int y, int z) {
		return super.idPicked(world, x, y, z);
	}

	@Override
	public void onBlockPlacedBy(
			World world, int x, int y, int z, EntityLiving player) {

		if(!machineBuilder.iconDefault) {
			super.onBlockPlacedBy(world, x, y, z, player);
			return;
		}

		world.setBlockMetadataWithNotify(
				x, y, z,
				(MathHelper.floor_double(
						player.rotationYaw * 4.0F / 360.0F + 0.5) + 2) & 3);
	}

	@Override
	public boolean doWrenchRotation(
			World world, int x, int y, int z, int metadata, int facing,
			EntityLiving player) {

		if(!machineBuilder.iconDefault) return false;

		if(metadata >= 3) { // SOUTH
			world.setBlockMetadata(x, y, z, 0); // WEST
			return true;
		}

		world.setBlockMetadata(x, y, z, metadata + 1);
		return true;
	}

	private int getEmptyBucketTank(CJTileEntityMachineBase machineEntity) {
		int size = machineBuilder.tanks.size();
		for(int i = 0; i < size * 2; ++i) {
			int tankIndex = i % size;
			CJTank tank = machineBuilder.tanks.get(tankIndex);

			if(!tank.matchesDamageExclusive(machineEntity)) continue;
			if(machineBuilder.fuelTankIndex == tankIndex) continue;
			if(i < size && !tank.output && !tank.bidirectional) continue;

			CJTankVolume volume = machineEntity.tanks.get(tankIndex);
			if(volume.fluidID == 0 || volume.current < CJTank.BUCKET) {
				continue;
			}

			return tankIndex;
		}

		return -1;
	}

	private boolean tryFillBucket(
			World world, int x, int y, int z, EntityPlayer player) {

		CJTileEntityMachineBase machineEntity = machineEntity(world, x, y, z);

		ItemStack heldItem = player.inventory.getCurrentItem();
		if(heldItem == null) return false;

		int heldID = heldItem.getItemID();

		// If the bucket is empty -- fill from an available output tank.
		if(heldID == Items.EMPTY_BUCKET.itemID) {
			int index = getEmptyBucketTank(machineEntity);
			if(index == -1) return false;

			CJTankVolume volume = machineEntity.tanks.get(index);

			int bucketID = -1;
			int fluidID = -1;

			for(int i = 0; i < CJMod.bucketFluids.size(); i++) {
				fluidID = CJMod.bucketFluids.get(i);

				if(fluidID != volume.fluidID) continue;

				bucketID = CJMod.buckets.get(i).itemID;
			}

			if(bucketID == -1) return false;

			if(volume.removeFluid(0, CJTank.BUCKET, true) == CJTank.BUCKET) {
				InventoryPlayer inventory = player.inventory;
				ItemStack stack =
						inventory.mainInventory[inventory.currentItem];

				stack.setItemID(bucketID);

				Fluid fluid = Fluids.getFluidFromBlock(fluidID);
				fluid.playFluidPickUpSound(world, x, y, z);

				return true;
			}

			return false;
		}

		int fluidID = -1;
		for(int i = 0; i < CJMod.buckets.size(); i++) {
			ItemBucket bucket = CJMod.buckets.get(i);

			if(bucket.itemID != heldID) continue;

			fluidID = CJMod.bucketFluids.get(i);
			break;
		}
		if(fluidID == -1) return false;

		int tankIndex = -1;
		for(int i = 0; i < machineBuilder.tanks.size(); ++i) {
			CJTank tank = machineBuilder.tanks.get(i);
			CJTankVolume volume = machineEntity.tanks.get(i);

			if(!tank.matchesDamageExclusive(machineEntity)) continue;
			if(tank.output && !tank.bidirectional) continue;
			if(volume.fluidID != 0 && volume.fluidID != fluidID) continue;
			if(volume.max - volume.current < CJTank.BUCKET) continue;
			if(i != machineBuilder.fuelTankIndex &&
					machineBuilder.fuelTankIndex != -1 &&
					fluidID == CJMod.fuelFluid) {

				continue;
			}

			tankIndex = i;
			break;
		}

		if(tankIndex == -1) return false;

		CJTankVolume tankVolume = machineEntity.tanks.get(tankIndex);

		int filled = tankVolume.addFluid(fluidID, CJTank.BUCKET, true);
		if(filled == CJTank.BUCKET) {
			InventoryPlayer inventory = player.inventory;
			ItemStack stack =
					inventory.mainInventory[inventory.currentItem];

			stack.setItemID(Items.EMPTY_BUCKET.itemID);

			Fluid fluid = Fluids.getFluidFromBlock(fluidID);
			fluid.playFluidDropOutSound(world, x, y, z);

			return true;
		}

		return false;
	}

	@Override
	protected ItemBlock initializeItemBlock() {
		return new CJItemBlockMachineBase(this, !machineBuilder.iconDefault);
	}

	@Override
	public boolean blockActivated(
			World world, int x, int y, int z, EntityPlayer player) {

		CJTileEntityMachineBase machineEntity = machineEntity(world, x, y, z);

		if(tryFillBucket(world, x, y, z, player)) {
			return true;
		}

		CJGuiMachineBase gui = new CJGuiMachineBase(
				player.inventory, machineEntity);

		Minecraft.theMinecraft.displayGuiScreen(gui);

		return true;
	}

	@Override
	public void onBlockRemoval(World world, int x, int y, int z) {
		CJTileEntityMachineBase machine = machineEntity(world, x, y, z);

		machine.onBreak(world, x, y, z);

		super.onBlockRemoval(world, x, y, z);
	}

	@Override
	public void onNeighborBlockChange(
			World world, int x, int y, int z, int ext) {

		CJTileEntityMachineBase machine = machineEntity(world, x, y, z);

		if(machine.impl != null) {
			machine.impl.onNeighbourChange(world, x, y, z);
		}

		super.onNeighborBlockChange(world, x, y, z, ext);
	}

	@Override
	public TileEntity getBlockEntity() {
		return new CJTileEntityMachineBase(this);
	}

	@Override
	protected void allocateTextures() {
		String side = "cj_machine_side";
		String top = "cj_machine_top";
		String base = "cj_machine_base";

		if(machineBuilder.tier == CJMachineTier.PRIMITIVE) {
			side += "_primitive";
			top += "_primitive";
			base += "_primitive";
		}

		if(machineBuilder.iconDefault &&
				machineBuilder.sideMode == CJMachineBlockSideMode.FRONT_FACE) {

			this.addTexture(side, Face.ALL);
			this.addTexture(top, Face.TOP);
			this.addTexture(base, Face.BOTTOM);

			this.addTexture(machineBuilder.iconNames[0], Face.WEST, 0);
			this.addTexture(machineBuilder.iconNames[0], Face.NORTH, 1);
			this.addTexture(machineBuilder.iconNames[0], Face.EAST, 2);
			this.addTexture(machineBuilder.iconNames[0], Face.SOUTH, 3);

			return;
		}

		for(int i = 0; i < machineBuilder.iconNames.length; i++) {
			if(machineBuilder.sideMode != CJMachineBlockSideMode.ALL_FACES) {
				this.addTexture(
						machineBuilder.sideMode ==
								CJMachineBlockSideMode.ALL_SIDES ?
								machineBuilder.iconNames[i] : side,
						Face.ALL, i);

				this.addTexture(top, Face.TOP, i);
				this.addTexture(base, Face.BOTTOM, i);
			}

			this.addTexture(
					machineBuilder.iconNames[i],
					machineBuilder.sideMode ==
							CJMachineBlockSideMode.ALL_FACES ?
							Face.ALL : Face.WEST, i);
		}
	}

	@Override
	protected int damageDropped(int metadata) {
		return machineBuilder.doDropMeta ? metadata : 0;
	}

	public String getIconName(String itemName, int metadata) {
		return itemName;
	}
}
