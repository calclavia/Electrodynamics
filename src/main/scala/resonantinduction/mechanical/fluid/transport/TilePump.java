package resonantinduction.mechanical.fluid.transport;

import net.minecraft.block.material.Material;
import resonantinduction.core.prefab.node.NodePressure;
import resonantinduction.mechanical.energy.grid.TileMechanical;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.IRotatable;
import universalelectricity.api.core.grid.INode;
import universalelectricity.core.transform.vector.Vector3;

public class TilePump extends TileMechanical implements IRotatable, IFluidHandler
{
	private final NodePressure pressureNode;

	public TilePump()
	{
		super(Material.iron);

        normalRender(false);
        isOpaqueCube(false);
        customItemRender(true);
        setTextureName("material_steel");
		pressureNode = new NodePressure(this)
		{
			@Override
			public int getPressure(ForgeDirection dir)
			{
				if (mechanicalNode.getPower() > 0)
				{
					if (dir == getDirection())
					{
						return (int) Math.max(Math.abs(mechanicalNode.getTorque() / 8000d), 2);
					}
					else if (dir == getDirection().getOpposite())
					{
						return (int) -Math.max(Math.abs(mechanicalNode.getTorque() / 8000d), 2);
					}
				}

				return 0;
			}

			@Override
			public boolean canConnect(ForgeDirection from, Object source)
			{
				return super.canConnect(from, source) && (from == getDirection() || from == getDirection().getOpposite());
			}

		};
	}

	@Override
	public void start()
	{
		pressureNode.reconstruct();
		super.start();
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		pressureNode.deconstruct();
	}

	@Override
	public void update()
	{
		super.update();

		if (!worldObj.isRemote && mechanicalNode.getPower() > 0)
		{
			/**
			 * Try to suck fluid in
			 */
			TileEntity tileIn = new Vector3(this).add(getDirection().getOpposite()).getTileEntity(this.worldObj);

			if (tileIn instanceof IFluidHandler)
			{
				FluidStack drain = ((IFluidHandler) tileIn).drain(getDirection(), pressureNode.maxOutput(), false);

				if (drain != null)
				{
					((IFluidHandler) tileIn).drain(getDirection(), fill(getDirection().getOpposite(), drain, true), true);
				}
			}
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (from == getDirection().getOpposite())
		{
			TileEntity tileOut = new Vector3(this).add(from.getOpposite()).getTileEntity(this.worldObj);

			if (tileOut instanceof IFluidHandler)
				return ((IFluidHandler) tileOut).fill(from, resource, doFill);
		}

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return from == this.getDirection().getOpposite();
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return from == this.getDirection();
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return null;
	}

	@Override
	public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
	{
		if (nodeType.isAssignableFrom(pressureNode.getClass()))
			return pressureNode;

		return super.getNode(nodeType, from);
	}

    @Override
    public ForgeDirection getDirection() {
        return null;
    }

    @Override
    public void setDirection(ForgeDirection direction) {

    }
}
