package resonantinduction.mechanical.network;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.net.IConnector;

public interface IMechanical extends IConnector<IMechanicalNetwork>
{
	/**
	 * The percentage of resistance caused by this connector.
	 * 
	 * @return A small value, most likely less than one.
	 */
	public float getResistance();

	/**
	 * 
	 * @return int[4]: x,y,z,direction
	 */
	public int[] getLocation();

	/**
	 * @return Is the mechanical machine going clockwise currently?
	 */
	public boolean isClockwise();

	public void setClockwise(boolean isClockwise);

	/**
	 * @return Return true if the mechanical block should have its rotation set inveresed.
	 */
	public boolean isRotationInversed();
}
