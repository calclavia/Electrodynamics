package resonantinduction.mechanical.energy.turbine;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.mechanical.energy.grid.MechanicalNode;

/**
 * Turbine's Mechanical node
 * Turbines always face forward and connect from behind.
 *
 * @author Calclavia, Darkguardsman
 */
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
		return turbine().getMultiBlock().isPrimary() && source instanceof MechanicalNode && !(source instanceof TurbineNode) && from == turbine().getDirection().getOpposite();
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
