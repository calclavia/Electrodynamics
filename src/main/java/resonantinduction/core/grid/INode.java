package resonantinduction.core.grid;

import java.util.AbstractMap;

public interface INode
{
	public void update();

	/**
	 * @return A map consisting of the connected object and a ForgeDirection.
	 */
	public AbstractMap getConnections();
}
