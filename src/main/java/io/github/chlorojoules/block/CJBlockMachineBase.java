package io.github.chlorojoules.block;

import com.fox2code.foxloader.energy.FoxPowerBlock;
import com.fox2code.foxloader.energy.FoxPowerInterface;
import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.CJTank;
import io.github.chlorojoules.CJTankVolume;
import io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase;
import io.github.chlorojoules.container.CJContainerMachineBase;
import io.github.chlorojoules.gui.CJGuiMachineBase;
import io.github.chlorojoules.item.CJItemBlockMachineBase;
import io.github.chlorojoules.machine.CJMachineBlockSideMode;
import io.github.chlorojoules.machine.CJMachineTier;
import io.github.chlorojoules.machine.CJMachineBuilder;

import net.minecraft.client.Minecraft;

import net.minecraft.client.player.EntityPlayerSP;
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
import net.minecraft.common.item.children.ItemBucketBase;
import net.minecraft.common.item.children.ItemGoldenBucket;
import net.minecraft.common.networking.Packet;
import net.minecraft.common.networking.Packet100OpenWindow;
import net.minecraft.common.stats.Achievement;
import net.minecraft.common.util.math.MathHelper;
import net.minecraft.common.world.BlockAccess;
import net.minecraft.common.world.World;
import net.minecraft.server.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.Nullable;

import static io.github.chlorojoules.CJRarityInfo.getRarityColor;
import static io.github.chlorojoules.block.tileentity.CJTileEntityMachineBase.machineEntity;

public class CJBlockMachineBase
		extends BlockContainer implements FoxPowerBlock {

	private Achievement achievement;

	public CJMachineBuilder machineBuilder;

	public CJBlockMachineBase(CJMachineBuilder machineBuilder) {
		super(machineBuilder.name, machineBuilder.tier.getMaterial());

		this.machineBuilder = machineBuilder;

		super.setBlockName(machineBuilder.name);
		//super.addDescription(new CJItemDescriptionModTag());
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

			if(tank.checkDamageExclusive(machineEntity)) continue;
			// TODO: Add a third pass for fuel.
			if(tank.id.equals("fuel")) continue;
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

		// If the bucket is empty -- fill from an available output tank.
		boolean isEmpty = heldItem.getItem() == Items.EMPTY_BUCKET;
		boolean isGoldEmpty = heldItem.getItem() == Items.GOLDEN_EMPTY_BUCKET;
		if(isEmpty || isGoldEmpty) {
			int index = getEmptyBucketTank(machineEntity);
			if(index == -1) return false;

			CJTankVolume volume = machineEntity.tanks.get(index);

			int bucketID = ItemBucket.getFluidBucketMapping(volume.fluidID);
			if(isGoldEmpty) {
				bucketID =
						ItemGoldenBucket.getFluidBucketMapping(volume.fluidID);
			}

			if(volume.removeFluid(0, CJTank.BUCKET, true) == CJTank.BUCKET) {
				InventoryPlayer inventory = player.inventory;
				ItemStack stack =
						inventory.mainInventory[inventory.currentItem];

				stack.setItemID(bucketID);

				Fluid fluid = Fluids.getFluidFromBlock(volume.fluidID);
				if(fluid == null) {
					Fluids.WATER.playFluidPickUpSound(world, x, y, z);
				}
				else {
					fluid.playFluidPickUpSound(world, x, y, z);
				}

				stack.onCrafting(world, player);

				return true;
			}

			return false;
		}

		if(heldItem.getItem() instanceof ItemBucketBase bucket) {
			int fluidID = bucket.getHeldLiquid();

			int tankIndex = -1;
			for(int i = 0; i < machineBuilder.tanks.size(); ++i) {
				CJTank tank = machineBuilder.tanks.get(i);
				CJTankVolume volume = machineEntity.tanks.get(i);

				if(tank.checkDamageExclusive(machineEntity)) continue;
				if(tank.output && !tank.bidirectional) continue;
				if(volume.fluidID != 0 && volume.fluidID != fluidID) continue;
				if(volume.max - volume.current < CJTank.BUCKET) continue;
				if(!tank.id.equals("fuel") &&
						machineBuilder.hasNamedTank("fuel") &&
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

				if(!player.capabilities.isCreativeMode) {
					if(stack.getItem() instanceof ItemGoldenBucket) {
						stack.setItemID(Items.GOLDEN_EMPTY_BUCKET.itemID);
					}
					else {
						stack.setItemID(Items.EMPTY_BUCKET.itemID);
					}
				}

				Fluid fluid = Fluids.getFluidFromBlock(fluidID);
				fluid.playFluidDropOutSound(world, x, y, z);

				return true;
			}
		}

		return false;
	}

	@Override
	protected ItemBlock initializeItemBlock() {
		return new CJItemBlockMachineBase(this, !machineBuilder.iconDefault);
	}

	public void displayGUIMP(
			EntityPlayerMP player, CJTileEntityMachineBase machineEntity) {

		player.getNextWindowId();

		Packet packet = new Packet100OpenWindow(
				player.currentWindowId,
				0, machineEntity.getInvName(),
				machineEntity.getSizeInventory());

		player.playerNetServerHandler.sendPacket(packet);

		player.currentContainer = new CJContainerMachineBase(
				player.inventory, machineEntity);

		player.currentContainer.windowId = player.currentWindowId;
		player.currentContainer.onCraftGuiOpened(player);
		player.mcServer.getPlayerInteractionHandler().blockActivation(
				machineEntity.getBuilder().name, player);
	}

	public void displayGUISP(
			EntityPlayerSP player, CJTileEntityMachineBase machineEntity) {

		CJGuiMachineBase gui = new CJGuiMachineBase(
				player.inventory, machineEntity);

		Minecraft.getInstance().displayGuiScreen(gui);
	}

	public boolean blockActivated(
			World world, int x, int y, int z, EntityPlayer player) {

		CJTileEntityMachineBase machineEntity = machineEntity(world, x, y, z);

		if(tryFillBucket(world, x, y, z, player)) {
			return true;
		}

		if(!world.isRemote) {
			if(player instanceof EntityPlayerMP mp) {
				displayGUIMP(mp, machineEntity);
			}
			else if(player instanceof EntityPlayerSP sp) {
				displayGUISP(sp, machineEntity);
			}
		}

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

		machine.onNeighbourChange();

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

	public void setAchievement(Achievement achievement) {
		this.achievement = achievement;
	}

	@Override
	public void onCrafting(
			ItemStack itemstack, World world, EntityPlayer player) {

		if(this.achievement != null) {
			player.triggerAchievement(this.achievement);
		}

		super.onCrafting(itemstack, world, player);
	}

	@Override
	public @Nullable FoxPowerInterface getIntrinsicPowerInterface(
			BlockAccess blockAccess, int x, int y, int z) {

		CJTileEntityMachineBase machineEntity =
				(CJTileEntityMachineBase) blockAccess.getBlockTileEntity(
						x, y, z);

		return machineEntity.powerInterface;
	}
}
