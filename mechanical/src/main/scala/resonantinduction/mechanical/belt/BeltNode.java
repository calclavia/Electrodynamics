package resonantinduction.mechanical.belt;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.belt.TileConveyorBelt.BeltType;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import calclavia.lib.grid.INodeProvider;

/** @author Darkguardsman */
public class BeltNode extends MechanicalNode
{
    public BeltNode(INodeProvider parent)
    {
        super(parent);
    }

    @Override
    public void doRecache()
    {
        connections.clear();
        TileConveyorBelt belt = ((TileConveyorBelt) parent);

        for (int side = 2; side < 6; side++)
        {
            ForgeDirection dir = ForgeDirection.getOrientation(side);
            VectorWorld pos = (VectorWorld) new VectorWorld(belt).translate(dir);
            TileEntity tile = pos.getTileEntity();

            if (dir == belt.getDirection() || dir == belt.getDirection().getOpposite())
            {
                if (dir == belt.getDirection())
                {
                    if (belt.getBeltType() == BeltType.SLANT_DOWN)
                    {
                        pos.translate(new Vector3(0, -1, 0));
                    }
                    else if (belt.getBeltType() == BeltType.SLANT_UP)
                    {
                        pos.translate(new Vector3(0, 1, 0));
                    }
                }
                else if (dir == belt.getDirection().getOpposite())
                {
                    if (belt.getBeltType() == BeltType.SLANT_DOWN)
                    {
                        pos.translate(new Vector3(0, 1, 0));
                    }
                    else if (belt.getBeltType() == BeltType.SLANT_UP)
                    {
                        pos.translate(new Vector3(0, -1, 0));
                    }
                }

                tile = pos.getTileEntity(belt.worldObj);

                if (tile instanceof TileConveyorBelt)
                {
                    connections.put(((TileConveyorBelt) tile).getNode(BeltNode.class, dir.getOpposite()), dir);
                }
            }
        }
    }

    @Override
    public boolean canConnect(ForgeDirection from, Object source)
    {
        return source instanceof TileConveyorBelt && (from == ((TileConveyorBelt) parent).getDirection() || from == ((TileConveyorBelt) parent).getDirection().getOpposite());
    }
}
