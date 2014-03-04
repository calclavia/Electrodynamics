package resonantinduction.core.grid;

import java.util.AbstractMap;

import net.minecraftforge.common.ForgeDirection;

public interface INode<G extends IGrid>
{
	/**
	 * Updates the node. This may be called on a different thread.
	 * 
	 * @param deltaTime - The time in seconds that has passed between the successive updates.
	 */
	void update(float deltaTime);

	/**
	 * @return A map consisting of the connected object and a ForgeDirection.
	 */
	AbstractMap<?, ForgeDirection> getConnections();

	G getGrid();

	void setGrid(G grid);

	/**
	 * Called whenever the node changes to update its cached connections and network.
	 */
	void reconstruct();
}
