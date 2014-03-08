package resonantinduction.core.grid;

import java.util.AbstractMap;
import java.util.WeakHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public abstract class Node<P extends INodeProvider, G extends Grid, N>
{
	public final P parent;

	protected final AbstractMap<N, ForgeDirection> connections = new WeakHashMap<N, ForgeDirection>();

	public G grid = null;

	public Node(P parent)
	{
		this.parent = parent;
	}

	public final G getGrid()
	{
		if (grid == null)
			grid = newGrid();

		return grid;
	}

	protected abstract G newGrid();

	public final void setGrid(G grid)
	{
		this.grid = grid;
	}

	public void update(float deltaTime)
	{

	}

	/**
	 * TODO: Try inject tile validate and invalidate events so this does not have to be called.
	 * This constructs the node. It should be called whenever the connections of the node are
	 * updated OR when the node is first initiated and can access its connections.
	 */
	public void reconstruct()
	{
		synchronized (connections)
		{
			recache();
			getGrid().add(this);
			getGrid().reconstruct();
		}
	}

	/**
	 * This destroys the node, removing it from the grid and also destroying all references to it.
	 */
	public void deconstruct()
	{
		synchronized (connections)
		{
			/**
			 * Remove self from all connections.
			 */
			for (N connection : connections.keySet())
			{
				if (getGrid().isValidNode(connection))
				{
					((Node) connection).getConnections().remove(this);
				}
			}

			getGrid().remove(this);
			getGrid().deconstruct();
		}
	}

	/**
	 * Called for a node to recache all its connections.
	 */
	public void recache()
	{

	}

	/**
	 * Returns all the connections in this node.
	 * 
	 * @return
	 */
	public AbstractMap<N, ForgeDirection> getConnections()
	{
		return connections;
	}

	/**
	 * Must be called to load the node's data.
	 */
	public void load(NBTTagCompound nbt)
	{

	}

	/**
	 * Must be called to save the node's data.
	 */
	public void save(NBTTagCompound nbt)
	{

	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + hashCode() + ", Connections: " + connections.size() + ", Grid:" + getGrid() + "]";
	}
}
