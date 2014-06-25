package resonantinduction.mechanical.gearshaft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INodeProvider;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.gear.PartGear;
import universalelectricity.api.vector.Vector3;

public class GearShaftNode extends MechanicalNode
{
	public GearShaftNode(PartGearShaft parent)
	{
		super(parent);
	}

	@Override
	public double getTorqueLoad()
	{
		// Decelerate the gear based on tier.
		switch (shaft().tier)
		{
			default:
				return 0.03;
			case 1:
				return 0.02;
			case 2:
				return 0.01;
		}
	}

	@Override
	public double getAngularVelocityLoad()
	{
		return 0;
	}

	@Override
	public void recache()
	{
		synchronized (this)
		{
			getConnections().clear();
			List<ForgeDirection> dirs = new ArrayList<ForgeDirection>();
			dirs.add(shaft().placementSide);
			dirs.add(shaft().placementSide.getOpposite());
			/** Check for internal connections, the FRONT and BACK. */
			Iterator<ForgeDirection> it = dirs.iterator();
			while (it.hasNext())
			{
				ForgeDirection checkDir = it.next();
				if (checkDir == shaft().placementSide || checkDir == shaft().placementSide.getOpposite())
				{
					if (shaft().tile() instanceof INodeProvider)
					{
						MechanicalNode instance = (MechanicalNode) ((INodeProvider) shaft().tile()).getNode(MechanicalNode.class, checkDir);

						if (instance != null && instance != this && instance.canConnect(checkDir.getOpposite(), this))
						{
							getConnections().put(instance, checkDir);
							it.remove();
						}
					}
				}
			}

			/** Look for connections outside this block space, the relative FRONT and BACK */
			if (!dirs.isEmpty())
				for (ForgeDirection checkDir : dirs)
				{
					if (!getConnections().containsValue(checkDir) && (checkDir == shaft().placementSide || checkDir == shaft().placementSide.getOpposite()))
					{
						TileEntity checkTile = new Vector3(shaft().tile()).translate(checkDir).getTileEntity(world());

						if (checkTile instanceof INodeProvider)
						{
							MechanicalNode instance = (MechanicalNode) ((INodeProvider) checkTile).getNode(MechanicalNode.class, checkDir.getOpposite());

							// Only connect to shafts outside of this block space.
							if (instance != null && instance != this && instance.getParent() instanceof PartGearShaft && instance.canConnect(checkDir.getOpposite(), this))
							{
								getConnections().put(instance, checkDir);
							}
						}
					}
				}
		}
	}

	@Override
	public boolean canConnect(ForgeDirection from, Object source)
	{
		if (source instanceof MechanicalNode)
		{
			if (((MechanicalNode) source).getParent() instanceof PartGear)
			{
				PartGear gear = (PartGear) ((MechanicalNode) source).getParent();

				if (!(Math.abs(gear.placementSide.offsetX) == Math.abs(shaft().placementSide.offsetX) && Math.abs(gear.placementSide.offsetY) == Math.abs(shaft().placementSide.offsetY) && Math.abs(gear.placementSide.offsetZ) == Math.abs(shaft().placementSide.offsetZ)))
				{
					return false;
				}
			}
		}

		return from == shaft().placementSide || from == shaft().placementSide.getOpposite();
	}

	@Override
	public boolean inverseRotation(ForgeDirection dir, IMechanicalNode with)
	{
		if (shaft().placementSide.offsetY != 0 || shaft().placementSide.offsetZ != 0)
		{
			return dir == shaft().placementSide.getOpposite();
		}

		return dir == shaft().placementSide;
	}

	public PartGearShaft shaft()
	{
		return (PartGearShaft) this.getParent();
	}
}
