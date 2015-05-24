package com.calclavia.edx.core.prefab.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import resonantengine.core.Reference;

import java.util.List;

/**
 * Basic Item that acts like a bucket but stores its fluid inside NBT allowing it to contain any fluid
 *
 * @author Darkgurdsman
 */
public class ItemFluidBucket extends ItemFluidContainer
{
	//TODO implement materials for bucket body allowing for molten fluids to more realistically be stored
	@SideOnly(Side.CLIENT)
	IIcon icon_bucket;

	@SideOnly(Side.CLIENT)
	IIcon icon_fluid;

	public ItemFluidBucket()
	{
		super(0, FluidContainerRegistry.BUCKET_VOLUME);
		this.setUnlocalizedName(Items.bucket.getUnlocalizedName());
		this.setContainerItem(Items.bucket);
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg)
	{
		this.icon_bucket = reg.registerIcon(Reference.prefix() + "bucket_pass");
		this.icon_fluid = reg.registerIcon(Reference.prefix() + "bucket_fluid_pass");
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass)
	{
		FluidStack fluidStack = this.getFluid(stack);
		if (fluidStack != null && fluidStack.getFluid() != null && pass == 1)
		{
			return fluidStack.getFluid().getColor();
		}
		return super.getColorFromItemStack(stack, pass);
	}

	@SideOnly(Side.CLIENT) @Override
	public IIcon getIcon(ItemStack stack, int pass)
	{
		FluidStack fluidStack = this.getFluid(stack);
		if (fluidStack != null && fluidStack.getFluid() != null)
		{
			Fluid fluid = fluidStack.getFluid();
			if (fluid == FluidRegistry.LAVA)
			{
				return Items.lava_bucket.getIcon(stack, pass);
			}
			else if (fluid == FluidRegistry.WATER)
			{
				return Items.water_bucket.getIcon(stack, pass);
			}
			else if (pass == 0)
			{
				return icon_bucket;
			}
			else if (pass == 1)
			{
				return icon_fluid;
			}
		}
		return Items.bucket.getIcon(stack, pass);
	}

	@SideOnly(Side.CLIENT) @Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool)
	{
		FluidStack fluidStack = this.getFluid(stack);
		if (fluidStack != null && fluidStack.getFluid() != null)
		{
			list.add("F: " + fluidStack.getFluid());
			list.add("V: " + fluidStack.amount + "mL");
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (this.getFluid(stack) == null)
		{
			return new ItemStack(Items.bucket);
		}
		else
		{
			MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, false);
			if (movingobjectposition == null)
			{
				return stack;
			}
			if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				int i = movingobjectposition.blockX;
				int j = movingobjectposition.blockY;
				int k = movingobjectposition.blockZ;

				if (!world.canMineBlock(player, i, j, k))
				{
					return stack;
				}

				if (movingobjectposition.sideHit == 0)
				{
					--j;
				}

				if (movingobjectposition.sideHit == 1)
				{
					++j;
				}

				if (movingobjectposition.sideHit == 2)
				{
					--k;
				}

				if (movingobjectposition.sideHit == 3)
				{
					++k;
				}

				if (movingobjectposition.sideHit == 4)
				{
					--i;
				}

				if (movingobjectposition.sideHit == 5)
				{
					++i;
				}

				if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, stack))
				{
					return stack;
				}

				//TODO if bored fix so that stacked buckets work correctly
				if (this.tryPlaceContainedLiquid(stack, world, i, j, k) && !player.capabilities.isCreativeMode)
				{
					return new ItemStack(Items.bucket);
				}
			}

			return stack;
		}
	}

	/**
	 * Attempts to place the liquid contained inside the bucket.
	 */
	public boolean tryPlaceContainedLiquid(ItemStack stack, World world, int x, int y, int z)
	{
		FluidStack fluidStack = this.getFluid(stack);
		if (fluidStack != null && fluidStack.getFluid() != null && fluidStack.getFluid().getBlock() != null)
		{
			Material material = world.getBlock(x, y, z).getMaterial();
			boolean flag = !material.isSolid();

			if (!world.isAirBlock(x, y, z) && !flag)
			{
				return false;
			}
			else
			{

				if (!world.isRemote && flag && !material.isLiquid())
				{
					world.func_147480_a(x, y, z, true);
				}

				world.setBlock(x, y, z, fluidStack.getFluid().getBlock(), 0, 3);

				return true;
			}
		}
		return false;
	}
}
