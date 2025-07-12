package io.github.chlorojoules.block;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.gui.CJGuiMachineBase;
import io.github.chlorojoules.item.CJItemBlockMachineBase;
import io.github.chlorojoules.machine.CJMachineBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.common.block.Block;
import net.minecraft.common.block.children.BlockContainer;
import net.minecraft.common.block.data.Material;
import net.minecraft.common.block.data.Materials;
import net.minecraft.common.block.fluid.Fluid;
import net.minecraft.common.block.fluid.Fluids;
import net.minecraft.common.block.sound.StepSound;
import net.minecraft.common.block.sound.StepSounds;
import net.minecraft.common.block.texture.Face;
import net.minecraft.common.block.tileentity.TileEntity;
import net.minecraft.common.entity.EntityLiving;
import net.minecraft.common.entity.player.EntityPlayer;
import net.minecraft.common.entity.player.InventoryPlayer;
import net.minecraft.common.item.*;
import net.minecraft.common.item.block.ItemBlock;
import net.minecraft.common.item.children.ItemBucket;
import net.minecraft.common.item.data.EnumTools;
import net.minecraft.common.util.math.MathHelper;
import net.minecraft.common.world.World;

import static io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase.machineEntity;

public class CJBlockMachineBase extends BlockContainer {
	public static final int FRONT_FACE = 0;
	public static final int ALL_SIDES = 1;
	public static final int ALL_FACES = 2;

	private final String[] iconNames;
	private final boolean iconDefault;
	private final CJMachineBuilder machineBuilder;
	private final int sideMode;

	public CJBlockMachineBase(
			String id, CJMachineBuilder machineBuilder,
			String[] iconNames, int maxDamage, int sideMode) {

		super(id, Materials.ROCK);

		this.machineBuilder = machineBuilder;
		this.maxMetadata = maxDamage;
		this.sideMode = sideMode;

		this.iconDefault = (iconNames == null);
		if(this.iconDefault) this.iconNames = new String[] { id };
		else this.iconNames = iconNames;

		// Machines have the same basic block properties by default.
		super.setHardness(1.5F);
		super.setResistance(10.0F);
		super.setSound(StepSounds.SOUND_STONE);
		super.setEffectiveTool(EnumTools.PICKAXE);
	}

	// TODO: Override the following for rotation.
	// TODO: `doWrenchRotation' should also allow to wrench machines to
	// 		 Pick them up more easily.
	/*
	doWrenchRotation
	onBlockPlacedBy
	allocateTextures
	 */

	private boolean tryFillBucket(
			World world, int x, int y, int z, EntityPlayer player) {

		CJTileEntityMachineBase machineEntity = machineEntity(world, x, y, z);

		ItemStack heldItem = player.inventory.getCurrentItem();
		if(heldItem == null) return false;

		int heldID = heldItem.getItemID();

		// If the bucket is empty -- fill from an available output tank.
		if(heldID == Items.EMPTY_BUCKET.itemID) {
			int index = -1;
			for(int i = 0; i < machineBuilder.tanks.size(); ++i) {
				CJTank tank = machineBuilder.tanks.get(i);

				if(!tank.output && !tank.bidirectional) continue;

				CJTankVolume volume = machineEntity.tanks.get(i);
				if(volume.fluidID == 0 || volume.current < CJTank.BUCKET) {
					continue;
				}

				index = i;
				break;
			}
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

			if(tank.output && !tank.bidirectional) continue;
			if(volume.fluidID != 0 && volume.fluidID != fluidID) continue;
			if(volume.max - volume.current < CJTank.BUCKET) continue;

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
		return new CJItemBlockMachineBase(this);
	}

	@Override
	public boolean blockActivated(
			World world, int x, int y, int z, EntityPlayer player) {

		CJTileEntityMachineBase machineEntity = machineEntity(world, x, y, z);

		if(tryFillBucket(world, x, y, z, player)) {
			return true;
		}

		// TODO: Allow tools to query device without opening GUI.
		CJGuiMachineBase gui = new CJGuiMachineBase(
				player.inventory, machineEntity, machineBuilder);

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
		return new CJTileEntityMachineBase(machineBuilder);
	}

	@Override
	protected void allocateTextures() {
		for(int i = 0; i <= maxMetadata; i++) {
			if(sideMode != ALL_FACES) {
				this.addTexture(
						sideMode == ALL_SIDES ?
								iconNames[i] : "cj_machine_side",
						Face.ALL, i);

				this.addTexture("cj_machine_top", Face.TOP, i);
				this.addTexture("cj_machine_base", Face.BOTTOM, i);
			}

			this.addTexture(
					iconNames[i],
					sideMode == ALL_FACES ? Face.ALL : Face.WEST, i);
		}
	}

	@Override
	protected int damageDropped(int metadata) {
		return machineBuilder.doDropMeta ? metadata : 0;
	}

	public String getIconName(String itemName, int metadata) {
		if(this.iconDefault) return itemName;
		else return itemName + "." + iconNames[metadata];
	}
}
