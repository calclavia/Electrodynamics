package resonantinduction.mechanical.energy.gearshaft;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import calclavia.api.resonantinduction.IMechanicalNode;
import calclavia.lib.grid.INodeProvider;
import resonantinduction.mechanical.energy.gear.PartGear;
import resonantinduction.mechanical.energy.gear.PartGearShaft;
import resonantinduction.mechanical.energy.grid.MechanicalNode;

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
    public void doRecache()
    {
        connections.clear();

        /** Check for internal connections, the FRONT and BACK. */
        for (int i = 0; i < 6; i++)
        {
            ForgeDirection checkDir = ForgeDirection.getOrientation(i);

            if (checkDir == shaft().placementSide || checkDir == shaft().placementSide.getOpposite())
            {
                MechanicalNode instance = ((INodeProvider) shaft().tile()).getNode(MechanicalNode.class, checkDir);

                if (instance != null && instance != this && instance.canConnect(checkDir.getOpposite(), this))
                {
                    connections.put(instance, checkDir);
                }
            }
        }

        /** Look for connections outside this block space, the relative FRONT and BACK */
        for (int i = 0; i < 6; i++)
        {
            ForgeDirection checkDir = ForgeDirection.getOrientation(i);

            if (!connections.containsValue(checkDir) && (checkDir == shaft().placementSide || checkDir == shaft().placementSide.getOpposite()))
            {
                TileEntity checkTile = new universalelectricity.api.vector.Vector3(shaft().tile()).translate(checkDir).getTileEntity(world());

                if (checkTile instanceof INodeProvider)
                {
                    MechanicalNode instance = ((INodeProvider) checkTile).getNode(MechanicalNode.class, checkDir.getOpposite());

                    // Only connect to shafts outside of this block space.
                    if (instance != null && instance != this && instance.parent instanceof PartGearShaft && instance.canConnect(checkDir.getOpposite(), this))
                    {
                        connections.put(instance, checkDir);
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
            if (((MechanicalNode) source).parent instanceof PartGear)
            {
                PartGear gear = (PartGear) ((MechanicalNode) source).parent;

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
        return (PartGearShaft) this.parent;
    }
}
