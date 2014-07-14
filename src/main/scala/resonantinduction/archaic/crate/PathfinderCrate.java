package resonantinduction.archaic.crate;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;

/**
 * A class that allows flexible path finding in Minecraft Blocks. Back Ported from UE 1.3.0.
 * 
 * TODO: Will need to change when MC 1.5 comes out.
 * 
 * @author Calclavia
 */
public class PathfinderCrate
{
	public interface IPathCallBack
	{
		/**
		 * Is this a valid node to search for?
		 * 
		 * @return
		 */
		public boolean isValidNode(PathfinderCrate finder, ForgeDirection direction, TileEntity provider, TileEntity node);

		/**
		 * Called when looping through nodes.
		 * 
		 * @param finder
		 * @param provider
		 * @return True to stop the path finding operation.
		 */
		public boolean onSearch(PathfinderCrate finder, TileEntity provider);
	}

	/** A pathfinding call back interface used to call back on paths. */
	public IPathCallBack callBackCheck;

	/** A list of nodes that the pathfinder went through. */
	public List<TileEntity> iteratedNodes;

	/** The results and findings found by the pathfinder. */
	public List results;

	public PathfinderCrate()
	{
		this.callBackCheck = new IPathCallBack()
		{
			@Override
			public boolean isValidNode(PathfinderCrate finder, ForgeDirection direction, TileEntity provider, TileEntity node)
			{
				return node instanceof TileCrate;
			}

			@Override
			public boolean onSearch(PathfinderCrate finder, TileEntity provider)
			{
				return false;
			}
		};
		this.clear();
	}

	public boolean findNodes(TileEntity provider)
	{
		if (provider != null)
		{
			this.iteratedNodes.add(provider);

			if (this.callBackCheck.onSearch(this, provider))
			{
				return false;
			}

			for (int i = 0; i < 6; i++)
			{
				TileEntity connectedTile = VectorHelper.getTileEntityFromSide(provider.worldObj, new Vector3(provider), ForgeDirection.getOrientation(i));

				if (!iteratedNodes.contains(connectedTile))
				{
					if (this.callBackCheck.isValidNode(this, ForgeDirection.getOrientation(i), provider, connectedTile))
					{
						if (!this.findNodes(connectedTile))
						{
							return false;
						}

					}

				}
			}
		}

		return true;
	}

	/** Called to execute the pathfinding operation. */
	public PathfinderCrate init(TileEntity provider)
	{
		this.findNodes(provider);
		return this;
	}

	public PathfinderCrate clear()
	{
		this.iteratedNodes = new ArrayList<TileEntity>();
		this.results = new ArrayList();
		return this;
	}
}