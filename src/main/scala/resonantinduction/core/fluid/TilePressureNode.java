package resonantinduction.core.fluid;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import resonant.api.grid.INode;
import resonant.lib.network.IPacketReceiverWithID;
import resonantinduction.core.grid.fluid.FluidPressureNode;
import resonantinduction.core.grid.fluid.IPressureNodeProvider;

/**
 * A prefab class for tiles that use the fluid network.
 * 
 * @author DarkGuardsman
 */
public abstract class TilePressureNode extends TileFluidNode implements IPressureNodeProvider, IPacketReceiverWithID
{
	protected FluidPressureNode node;

	public TilePressureNode(Material material)
	{
		super(material, 1000);
	}

	@Override
	public void initiate()
	{
		super.initiate();
		node.reconstruct();
	}

	@Override
	public void invalidate()
	{
		node.deconstruct();
		super.invalidate();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		int fill = getInternalTank().fill(resource, doFill);
		onFluidChanged();
		return fill;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return drain(from, resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		FluidStack drain = getInternalTank().drain(maxDrain, doDrain);
		onFluidChanged();
		return drain;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { getInternalTank().getInfo() };
	}

	public int getSubID()
	{
		return this.colorID;
	}

	public void setSubID(int id)
	{
		this.colorID = id;
	}

	@Override
	public FluidTank getInternalTank()
	{
		if (this.tank == null)
		{
			this.tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
		}

		return this.tank;
	}

	@Override
	public FluidTank getPressureTank()
	{
		return getInternalTank();
	}

	@Override
	public <N extends INode> N getNode(Class<? super N> nodeType, ForgeDirection from)
	{
		if (nodeType.isAssignableFrom(node.getClass()))
			return (N) node;
		return null;
	}
}
