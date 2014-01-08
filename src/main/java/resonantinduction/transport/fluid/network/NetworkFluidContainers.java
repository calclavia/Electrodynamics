package resonantinduction.transport.fluid.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.INetworkFluidPart;

import com.builtbroken.minecraft.FluidHelper;
import com.builtbroken.minecraft.tilenetwork.INetworkPart;
import com.builtbroken.minecraft.tilenetwork.prefab.NetworkUpdateHandler;

/**
 * Basically the same as network Fluid tiles class with the only difference being in how it stores
 * the fluid. When it goes to sort the fluid it will use the fluid properties to adjust its position
 * in the over all tank. Eg water goes down air goes up.
 * 
 * @author DarkGuardsman
 */
public class NetworkFluidContainers extends NetworkFluidTiles
{
	static
	{
		NetworkUpdateHandler.registerNetworkClass("FluidContainers", NetworkFluidContainers.class);
	}

	public NetworkFluidContainers()
	{
		super();
	}

	public NetworkFluidContainers(INetworkPart... parts)
	{
		super(parts);
	}

	@Override
	public void save()
	{
		this.cleanUpMembers();

		if (this.getNetworkTank() == null || this.getNetworkTank().getFluid() == null)
		{
			super.save();
			return;
		}
		FluidStack fillStack = this.getNetworkTank().getFluid().copy();

		int lowestY = 255, highestY = 0;

		if (this.getNetworkTank().getFluid() != null && this.getMembers().size() > 0)
		{
			for (INetworkPart part : this.getMembers())
			{
				if (part instanceof IFluidHandler)
				{
					((INetworkFluidPart) part).drainTankContent(0, Integer.MAX_VALUE, true);
				}
				if (part instanceof TileEntity && ((TileEntity) part).yCoord < lowestY)
				{
					lowestY = ((TileEntity) part).yCoord;
				}
				if (part instanceof TileEntity && ((TileEntity) part).yCoord > highestY)
				{
					highestY = ((TileEntity) part).yCoord;
				}
			}

			// TODO change this to use hydraulics to not only place fluid at the lowest but as well
			// not move it to another side if there is no path there threw fluid
			for (int y = lowestY; y <= highestY; y++)
			{
				/** List of parts for this Y level */
				List<INetworkFluidPart> parts = new ArrayList<INetworkFluidPart>();

				/* Grab all parts that share this Y level */
				for (INetworkPart part : this.getMembers())
				{
					if (part instanceof INetworkFluidPart && ((TileEntity) part).yCoord == y)
					{
						parts.add((INetworkFluidPart) part);
					}
				}
				if (!parts.isEmpty())
				{
					/*
					 * Div out the volume for this level. TODO change this to use a percent system
					 * for even filling
					 */
					int fillvolume = Math.abs(fillStack.amount / parts.size());

					/* Fill all tanks on this level */
					for (INetworkFluidPart part : parts)
					{
						part.drainTankContent(0, Integer.MAX_VALUE, true);
						fillStack.amount -= part.fillTankContent(0, FluidHelper.getStack(fillStack, fillvolume), true);
					}
				}

				if (fillStack == null || fillStack.amount <= 0)
				{
					break;
				}
			}
		}

	}
}
