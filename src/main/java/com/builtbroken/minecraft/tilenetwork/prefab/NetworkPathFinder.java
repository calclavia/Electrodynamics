package com.builtbroken.minecraft.tilenetwork.prefab;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.tilenetwork.INetworkPart;
import com.builtbroken.minecraft.tilenetwork.IPathCallBack;
import com.builtbroken.minecraft.tilenetwork.Pathfinder;

/** Check if a conductor connects with another. */
public class NetworkPathFinder extends Pathfinder
{
	public NetworkPathFinder(final World world, final INetworkPart targetPoint, final INetworkPart... ignoredTiles)
	{
		super(new IPathCallBack()
		{
			@Override
			public Set<Vector3> getConnectedNodes(Pathfinder finder, Vector3 currentNode)
			{
				Set<Vector3> neighbors = new HashSet<Vector3>();
				TileEntity tile = currentNode.getTileEntity(world);
				if (tile instanceof INetworkPart)
				{
					for (TileEntity ent : ((INetworkPart) tile).getNetworkConnections())
					{
						if (ent instanceof INetworkPart)
						{
							neighbors.add(new Vector3(ent));
						}
					}
				}

				return neighbors;
			}

			@Override
			public boolean onSearch(Pathfinder finder, Vector3 node)
			{
				if (node.getTileEntity(world) == targetPoint)
				{
					finder.results.add(node);
					return true;
				}

				return false;
			}
		});
	}
}
