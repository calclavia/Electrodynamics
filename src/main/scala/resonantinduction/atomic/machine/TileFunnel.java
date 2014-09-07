package resonantinduction.atomic.machine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import resonant.api.IBoilHandler;
import resonant.content.prefab.java.TileAdvanced;

/**
 * Funnel for gas.
 */
public class TileFunnel extends TileAdvanced implements IBoilHandler
{
	private static IIcon iconTop;
	private final FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 16);

	public TileFunnel()
	{
		super(Material.iron);

	}

	@Override
	public IIcon getIcon(int side, int meta)
	{
		return side == 1 || side == 0 ? iconTop : super.getIcon(side, meta);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		iconTop = iconRegister.registerIcon(getTextureName() + "_top");
	}

	@Override
	public void update()
	{
		super.update();

		if (tank.getFluidAmount() > 0)
		{
			TileEntity tileEntity = this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);

			if (tileEntity instanceof IFluidHandler)
			{
				IFluidHandler handler = (IFluidHandler) tileEntity;

				if (handler.canFill(ForgeDirection.DOWN, tank.getFluid().getFluid()))
				{
					FluidStack drainedStack = tank.drain(tank.getCapacity(), false);

					if (drainedStack != null)
					{
						tank.drain(handler.fill(ForgeDirection.DOWN, drainedStack, true), true);
					}
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		tank.writeToNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tank.readFromNBT(tag);
	}

	/**
	 * Tank Methods
	 */

    /* IFluidHandler */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return this.tank.drain(maxDrain, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource == null || !resource.isFluidEqual(tank.getFluid()))
		{
			return null;
		}
		return tank.drain(resource.amount, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		if (fluid.isGaseous() && from == ForgeDirection.DOWN)
		{
			return true;
		}

		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if (fluid.isGaseous() && from == ForgeDirection.UP)
		{
			return true;
		}

		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[]
				{ tank.getInfo() };
	}
}
