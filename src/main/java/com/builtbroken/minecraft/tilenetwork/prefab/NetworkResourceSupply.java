package com.builtbroken.minecraft.tilenetwork.prefab;

import java.util.HashMap;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.builtbroken.minecraft.tilenetwork.INetworkPart;

/**
 * Network that supplies resources to tiles that demand a set resource
 * 
 * @param C - Storage class used to handle what the network transports
 * @param I - Base acceptor class
 * @author DarkGuardsman
 */
public class NetworkResourceSupply<C, I> extends NetworkTileEntities
{
	protected C storage;
	protected HashMap<I, List<ForgeDirection>> acceptors = new HashMap();

	public NetworkResourceSupply(INetworkPart... parts)
	{
		super(parts);
	}

	public boolean isValidAcceptor(TileEntity entity)
	{
		return entity != null && !entity.isInvalid();
	}
}
