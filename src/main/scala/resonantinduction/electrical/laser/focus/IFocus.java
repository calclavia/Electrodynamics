package resonantinduction.electrical.laser.focus;


import universalelectricity.core.transform.vector.Vector3;

import java.util.List;

/**
 * Devices that can focus on specific angles
 *
 * @author Calclavia
 */
public interface IFocus
{
	/**
	 * Tells the block to look at a specific position
	 * @param position
	 */
	public void focus(Vector3 position);

	public Vector3 getFocus();

	public void setFocus(Vector3 focus);

	public List<Vector3> getCacheDirections();
}
