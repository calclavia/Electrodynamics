package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.INetworkPipe;
import resonantinduction.core.tilenetwork.ITileConnector;
import resonantinduction.core.tilenetwork.ITileNetwork;
import resonantinduction.mechanical.fluid.network.NetworkPipes;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetwork;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidHelper;
import dark.lib.helpers.ColorCode;
import dark.lib.helpers.ColorCode.IColorCoded;

public class TilePipe extends TileFluidNetwork implements IColorCoded, INetworkPipe
{
	/** gets the current color mark of the pipe */
	@Override
	public ColorCode getColor()
	{
		return EnumPipeType.getColorCode(this.colorID);
	}

	/** sets the current color mark of the pipe */
	@Override
	public boolean setColor(Object cc)
	{
		if (!worldObj.isRemote)
		{
			int p = this.colorID;
			this.colorID = EnumPipeType.getUpdatedID(colorID, ColorCode.get(cc));
			return p != this.colorID;
		}
		return false;
	}

	@Override
	public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
	{
		int meta = new Vector3(this).getBlockMetadata(this.worldObj);
		if (meta < FluidContainerMaterial.values().length)
		{
			FluidContainerMaterial pipeMat = FluidContainerMaterial.values()[meta];
			if (pipeMat == FluidContainerMaterial.WOOD || pipeMat == FluidContainerMaterial.STONE)
			{
				if (side == ForgeDirection.UP)
				{
					return;
				}
			}
		}
		if (tileEntity instanceof TilePipe)
		{
			int metaOther = new Vector3(tileEntity).getBlockMetadata(this.worldObj);
			if (meta < FluidContainerMaterial.values().length && metaOther < FluidContainerMaterial.values().length)
			{
				FluidContainerMaterial pipeMat = FluidContainerMaterial.values()[meta];
				FluidContainerMaterial pipeMatOther = FluidContainerMaterial.values()[metaOther];
				// Same pipe types can connect
				if (pipeMat == pipeMatOther)
				{
					this.getTileNetwork().mergeNetwork(((INetworkPipe) tileEntity).getTileNetwork(), this);
					connectedBlocks.add(tileEntity);
					setRenderSide(side, true);
				}
				else if ((pipeMat == FluidContainerMaterial.WOOD || pipeMat == FluidContainerMaterial.STONE) && (pipeMatOther == FluidContainerMaterial.WOOD || pipeMatOther == FluidContainerMaterial.STONE))
				{
					// Wood and stone pipes can connect to each other but not other pipe types since
					// they are more like a trough than a pipe
					this.getTileNetwork().mergeNetwork(((INetworkPipe) tileEntity).getTileNetwork(), this);
					connectedBlocks.add(tileEntity);
					setRenderSide(side, true);
				}
				else if (pipeMat != FluidContainerMaterial.WOOD && pipeMat != FluidContainerMaterial.STONE && pipeMatOther != FluidContainerMaterial.WOOD && pipeMatOther != FluidContainerMaterial.STONE && pipeMat != FluidContainerMaterial.GLASS && pipeMatOther != FluidContainerMaterial.GLASS)
				{
					/*
					 * Any other pipe can connect to each other as long as the color matches except
					 * for glass which only works with itself at the moment
					 */
					this.getTileNetwork().mergeNetwork(((INetworkPipe) tileEntity).getTileNetwork(), this);
					connectedBlocks.add(tileEntity);
					setRenderSide(side, true);
				}
			}
		}
		else if (tileEntity instanceof IFluidHandler)
		{
			connectedBlocks.add(tileEntity);
			setRenderSide(side, true);
			this.getTileNetwork().addTank(side.getOpposite(), (IFluidHandler) tileEntity);
		}
		else if (tileEntity instanceof ITileConnector && ((ITileConnector) tileEntity).canTileConnect(Connection.FLUIDS, side.getOpposite()))
		{
			connectedBlocks.add(tileEntity);
			setRenderSide(side, true);
		}

	}

	@Override
	public boolean onPassThrew(FluidStack fluid, ForgeDirection from, ForgeDirection to)
	{
		// TODO do checks for molten pipe so that fluids like water turn into steam, oils and fuels
		// burn
		return super.onPassThrew(fluid, from, to);
	}

	@Override
	public double getMaxPressure(ForgeDirection side)
	{
		int meta = this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if (meta < FluidContainerMaterial.values().length)
		{
			return FluidContainerMaterial.values()[meta].maxPressure;
		}
		return 350;
	}

	@Override
	public NetworkPipes getTileNetwork()
	{
		if (!(this.network instanceof NetworkPipes))
		{
			this.setTileNetwork(new NetworkPipes(this));
		}
		return (NetworkPipes) this.network;
	}

	@Override
	public void setTileNetwork(ITileNetwork network)
	{
		if (network instanceof NetworkPipes)
		{
			this.network = (NetworkPipes) network;
		}
	}

	@Override
	public int getMaxFlowRate(FluidStack stack, ForgeDirection side)
	{
		if (stack != null)
		{
			return this.calculateFlowRate(stack, 40, 20);
		}
		return BlockPipe.waterFlowRate;
	}

	/**
	 * Calculates flow rate based on viscosity & temp of the fluid as all other factors are know
	 * 
	 * @param fluid - fluidStack
	 * @param temp = tempature of the fluid
	 * @param pressure - pressure difference of were the fluid is flowing too.
	 * @return flow rate in mili-Buckets
	 */
	public int calculateFlowRate(FluidStack fluid, float pressure, float temp)
	{
		// TODO recalculate this based on pipe material for friction
		if (fluid != null & fluid.getFluid() != null)
		{
			float f = .012772f * pressure;
			f = f / (8 * (fluid.getFluid().getViscosity() / 1000));
			return (int) (f * 1000);
		}
		return BlockPipe.waterFlowRate;
	}

	@Override
	public boolean onOverPressure(Boolean damageAllowed)
	{
		if (damageAllowed)
		{
			if (this.tank.getFluid() != null && this.tank.getFluid() != null)
			{
				this.getTileNetwork().drainNetworkTank(this.worldObj, FluidHelper.fillBlock(this.worldObj, new Vector3(this), this.tank.getFluid(), true), true);
			}
			else
			{
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, yCoord, 0, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public void sendTankUpdate(int index)
	{
		if (this.getBlockMetadata() == FluidContainerMaterial.WOOD.ordinal() || this.getBlockMetadata() == FluidContainerMaterial.STONE.ordinal())
		{
			super.sendTankUpdate(index);
		}
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}
}
