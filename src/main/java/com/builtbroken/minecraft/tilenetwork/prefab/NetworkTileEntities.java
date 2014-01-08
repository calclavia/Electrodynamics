package com.builtbroken.minecraft.tilenetwork.prefab;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;

import com.builtbroken.minecraft.tilenetwork.INetworkPart;
import com.builtbroken.minecraft.tilenetwork.ITileNetwork;
import com.builtbroken.minecraft.tilenetwork.Pathfinder;

public class NetworkTileEntities implements ITileNetwork
{
	protected Set<INetworkPart> networkMembers = new HashSet<INetworkPart>();

	public NetworkTileEntities()
	{
		NetworkUpdateHandler.instance().registerNetwork(this);
	}

	public NetworkTileEntities(INetworkPart... parts)
	{
		this();
		if (parts != null)
		{
			for (INetworkPart part : parts)
			{
				if (this.isValidMember(part))
				{
					part.setTileNetwork(this);
					networkMembers.add(part);
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return "TileNetwork";
	}

	@Override
	public Set<INetworkPart> getMembers()
	{
		if (this.networkMembers == null)
		{
			this.networkMembers = new HashSet<INetworkPart>();
		}
		return networkMembers;
	}

	@Override
	public void onCreated()
	{
		this.load();
		this.cleanUpMembers();
	}

	@Override
	public int getUpdateRate()
	{
		return -1;
	}

	@Override
	public void updateTick()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshTick()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addTile(TileEntity ent, boolean member)
	{
		if (ent == null || ent.isInvalid())
		{
			return false;
		}
		else if (ent instanceof INetworkPart && this.isValidMember((INetworkPart) ent) && member)
		{
			((INetworkPart) ent).setTileNetwork(this);
			if (this.networkMembers.contains(ent))
			{
				return true;
			}
			return this.networkMembers.add((INetworkPart) ent);
		}
		return false;
	}

	@Override
	public boolean removeTile(TileEntity ent)
	{
		return this.networkMembers.remove(ent);
	}

	/** Cleans the list of networkMembers and remove those that no longer belong */
	public void cleanUpMembers()
	{
		Iterator<INetworkPart> it = this.networkMembers.iterator();

		while (it.hasNext())
		{
			INetworkPart part = it.next();
			if (!this.isValidMember(part))
			{
				it.remove();
			}
			else
			{
				part.setTileNetwork(this);
			}
		}

	}

	/** Is this part a valid member of the network */
	public boolean isValidMember(INetworkPart part)
	{
		return part != null && part instanceof TileEntity && !((TileEntity) part).isInvalid();
	}

	@Override
	public void save()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void load()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mergeNetwork(ITileNetwork network, INetworkPart mergePoint)
	{
		if (network != null && network != this && network.getClass().equals(this.getClass()))
		{
			if (this.preMergeProcessing(network, mergePoint))
			{
				this.mergeDo(network);
			}
		}
	}

	/**
	 * Processing that needs too be done before the network merges. Use this to do final network
	 * merge calculations and to cause network merge failure
	 * 
	 * @param network the network that is to merge with this one
	 * @param part the part at which started the network merge. Use this to cause damage if two
	 * networks merge with real world style failures
	 * 
	 * @return false if the merge needs to be canceled.
	 * 
	 * Cases in which the network should fail to merge are were the two networks merge with error.
	 * Or, in the case of pipes the two networks merge and the merge point was destroyed by
	 * combination of liquids.
	 * 
	 * Ex Lava and water
	 */
	public boolean preMergeProcessing(ITileNetwork network, INetworkPart part)
	{
		this.save();
		return true;
	}

	/** Merges the two networks together */
	protected void mergeDo(ITileNetwork network)
	{
		ITileNetwork newNetwork = NetworkUpdateHandler.createNewNetwork(NetworkUpdateHandler.getID(this.getClass()));
		if (newNetwork != null)
		{
			newNetwork.getMembers().addAll(this.getMembers());
			newNetwork.getMembers().addAll(network.getMembers());
			newNetwork.onCreated();
			this.invalidate();
		}
		else
		{
			System.out.println("[CoreMachine]NetworkTileEntities: Failed to merge network due to network creation failure");
		}
	}

	@Override
	public void splitNetwork(INetworkPart splitPoint)
	{
		this.getMembers().remove(splitPoint);
		if (splitPoint instanceof TileEntity)
		{
			List<TileEntity> connections = splitPoint.getNetworkConnections();

			for (final TileEntity connectionStart : connections)
			{
				if (connectionStart instanceof INetworkPart)
				{
					for (final TileEntity connectionEnd : connections)
					{
						if (connectionStart != connectionEnd && connectionEnd instanceof INetworkPart)
						{
							Pathfinder finder = new NetworkPathFinder(connectionEnd.worldObj, (INetworkPart) connectionEnd, splitPoint);
							finder.init(new Vector3(connectionStart));

							if (finder.results.size() <= 0)
							{
								this.save();
								/* NO LONGER CONNECTED ELSE WHERE SO SPLIT AND REFRESH */
								ITileNetwork newNetwork = NetworkUpdateHandler.createNewNetwork(NetworkUpdateHandler.getID(this.getClass()));
								if (newNetwork != null)
								{
									for (Vector3 node : finder.closedSet)
									{
										TileEntity entity = node.getTileEntity(connectionEnd.worldObj);
										if (entity instanceof INetworkPart)
										{
											if (node != splitPoint)
											{
												newNetwork.getMembers().add((INetworkPart) entity);
											}
										}
									}
									newNetwork.onCreated();
								}
							}
						}
					}
				}
			}

		}

	}

	@Override
	public String toString()
	{
		return this.getName() + "[" + this.hashCode() + "| Parts:" + this.networkMembers.size() + "]";
	}

	@Override
	public boolean isInvalid()
	{
		return this.networkMembers.isEmpty();
	}

	@Override
	public void invalidate()
	{
		this.networkMembers.clear();
	}

	public static void invalidate(TileEntity tileEntity)
	{
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity checkTile = VectorHelper.getConnectorFromSide(tileEntity.worldObj, new Vector3(tileEntity), direction);

			if (checkTile instanceof INetworkPart && ((INetworkPart) checkTile).getTileNetwork() != null)
			{
				((INetworkPart) checkTile).getTileNetwork().removeTile(tileEntity);
			}
		}
	}

}
