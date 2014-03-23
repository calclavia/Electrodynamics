package resonantinduction.mechanical.energy.gear;

import java.util.HashMap;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.WorldUtility;

/**
 * Used for testing. NO-OP
 * 
 * @author Calclavia
 * 
 */
@Deprecated
public abstract class ConnectionLogic
{
	protected final Vector3 self;

	/**
	 * Relative coordinates of connections allowed.
	 */
	protected HashMap<Vector3, ForgeDirection> connections = new HashMap<Vector3, ForgeDirection>();

	public ConnectionLogic(Vector3 self)
	{
		this.self = self;
	}

	/**
	 * 
	 * @param rotation
	 * @param other
	 * @param from - Incoming direction
	 * @return
	 */
	public boolean canConnect(ForgeDirection rotation, Vector3 other, ForgeDirection from)
	{
		Vector3 relative = other.clone().subtract(self);
		Vector3 rotated = relative.clone();
		WorldUtility.rotateVectorFromDirection(rotated, rotation);
		rotated = rotated.round();

		Vector3 fromDir = new Vector3(from);
		WorldUtility.rotateVectorFromDirection(fromDir, rotation);
		fromDir = fromDir.round();

		return connections.get(rotated) == fromDir.toForgeDirection();
	}

	/**
	 * By default, gears are facing UP, on the DOWN face.
	 */
	public static class ConnectionGearSmall extends ConnectionLogic
	{
		public ConnectionGearSmall(Vector3 self)
		{
			super(self);

			// Flat connection
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != ForgeDirection.UP)
				{
					connections.put(new Vector3(dir), dir);
					connections.put(new Vector3(), dir.getOpposite());
				}
			}
		}
	}

	public static class ConnectionGearLarge extends ConnectionLogic
	{
		public ConnectionGearLarge(Vector3 self)
		{
			super(self);

			// Flat connection and side connections
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != ForgeDirection.UP)
					connections.put(new Vector3(dir), dir);
			}
		}
	}
}
