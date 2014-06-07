package resonantinduction.mechanical.energy.turbine;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INodeProvider;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.mechanical.energy.grid.MechanicalNode;

/** Turbine's Mechanical node
 * 
 * @author Calclavia, Darkguardsman */
public class TurbineNode extends MechanicalNode
{
    public TurbineNode(TileTurbine tileTurbineBase)
    {
        super(tileTurbineBase);
    }

    public TileTurbine turbine()
    {
        return (TileTurbine) getParent();
    }

    @Override
    public boolean canConnect(ForgeDirection from, Object source)
    {
        if (source instanceof MechanicalNode && !(source instanceof TileTurbine))
        {
            /** Face to face stick connection. */
            TileEntity sourceTile = position().translate(from).getTileEntity(turbine().getWorld());

            if (sourceTile instanceof INodeProvider)
            {
                MechanicalNode sourceInstance = (MechanicalNode) ((INodeProvider) sourceTile).getNode(MechanicalNode.class, from.getOpposite());
                return sourceInstance == source && from == turbine().getDirection().getOpposite();
            }
        }

        return false;
    }

    @Override
    public boolean inverseRotation(ForgeDirection dir, IMechanicalNode with)
    {
        return dir == turbine().getDirection().getOpposite();
    }

    @Override
    public float getRatio(ForgeDirection dir, IMechanicalNode with)
    {
        return turbine().getMultiBlock().isConstructed() ? turbine().multiBlockRadius - 0.5f : 0.5f;
    }
}
