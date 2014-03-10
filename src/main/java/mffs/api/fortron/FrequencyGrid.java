package mffs.api.fortron;

import icbm.api.IBlockFrequency;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.server.ServerListenThread;
import net.minecraft.server.ThreadMinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

/**
 * A grid MFFS uses to search for machines with frequencies that can be linked and spread Fortron
 * energy.
 * 
 * @author Calclavia
 * 
 */
public class FrequencyGrid
{
	private static FrequencyGrid CLIENT_INSTANCE = new FrequencyGrid();
	private static FrequencyGrid SERVER_INSTANCE = new FrequencyGrid();

	private final Set<IBlockFrequency> frequencyGrid = Collections.newSetFromMap(new WeakHashMap<IBlockFrequency, Boolean>());

	public void register(IBlockFrequency tileEntity)
	{
		synchronized (frequencyGrid)
		{
			try
			{
				Iterator<IBlockFrequency> it = this.frequencyGrid.iterator();

				while (it.hasNext())
				{
					IBlockFrequency frequency = it.next();

					if (frequency == null)
					{
						it.remove();
						continue;
					}

					if (((TileEntity) frequency).isInvalid())
					{
						it.remove();
						continue;
					}

					if (new Vector3((TileEntity) frequency).equals(new Vector3((TileEntity) tileEntity)))
					{
						it.remove();
						continue;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			frequencyGrid.add(tileEntity);
		}
	}

	public void unregister(IBlockFrequency tileEntity)
	{
		synchronized (frequencyGrid)
		{
			frequencyGrid.remove(tileEntity);
			cleanUp();
		}
	}

	public Set<IBlockFrequency> get()
	{
		return frequencyGrid;
	}

	/**
	 * Gets a list of TileEntities that has a specific frequency.
	 * 
	 * @param frequency - The Frequency
	 * */
	public Set<IBlockFrequency> get(int frequency)
	{
		Set<IBlockFrequency> set = new HashSet<IBlockFrequency>();

		for (IBlockFrequency tile : this.get())
		{
			if (tile != null && !((TileEntity) tile).isInvalid())
			{
				if (tile.getFrequency() == frequency)
				{
					set.add(tile);
				}
			}
		}

		return set;
	}

	public void cleanUp()
	{
		Set<IBlockFrequency> tilesToRemove = new HashSet<IBlockFrequency>();
		Iterator<IBlockFrequency> it = this.frequencyGrid.iterator();

		while (it.hasNext())
		{
			IBlockFrequency frequency = it.next();

			if (frequency == null)
			{
				tilesToRemove.add(frequency);
				continue;
			}

			if (((TileEntity) frequency).isInvalid())
			{
				tilesToRemove.add(frequency);
				continue;
			}

			if (((TileEntity) frequency).worldObj.getBlockTileEntity(((TileEntity) frequency).xCoord, ((TileEntity) frequency).yCoord, ((TileEntity) frequency).zCoord) != ((TileEntity) frequency))
			{
				tilesToRemove.add(frequency);
				continue;
			}
		}

		for (IBlockFrequency tile : tilesToRemove)
		{
			this.unregister(tile);
		}
	}

	public Set<IBlockFrequency> get(World world, Vector3 position, int radius, int frequency)
	{
		Set<IBlockFrequency> set = new HashSet<IBlockFrequency>();

		for (IBlockFrequency tileEntity : this.get(frequency))
		{
			if (((TileEntity) tileEntity).worldObj == world)
			{
				if (Vector3.distance(new Vector3((TileEntity) tileEntity), position) <= radius)
				{
					set.add(tileEntity);
				}
			}
		}
		return set;

	}

	public Set<IFortronFrequency> getFortronTiles(World world)
	{
		Set<IFortronFrequency> set = new HashSet<IFortronFrequency>();

		for (IBlockFrequency tileEntity : this.get())
		{
			if (((TileEntity) tileEntity).worldObj == world && tileEntity instanceof IFortronFrequency)
			{
				set.add((IFortronFrequency) tileEntity);
			}
		}
		return set;
	}

	public Set<IFortronFrequency> getFortronTiles(World world, Vector3 position, int radius, int frequency)
	{
		Set<IFortronFrequency> set = new HashSet<IFortronFrequency>();

		for (IBlockFrequency tileEntity : this.get(frequency))
		{
			if (((TileEntity) tileEntity).worldObj == world && tileEntity instanceof IFortronFrequency)
			{
				if (Vector3.distance(new Vector3((TileEntity) tileEntity), position) <= radius)
				{
					set.add((IFortronFrequency) tileEntity);
				}
			}
		}
		return set;
	}

	/**
	 * Called to re-initiate the grid. Used when server restarts or when player rejoins a world to
	 * clean up previously registered objects.
	 */
	public static void reinitiate()
	{
		CLIENT_INSTANCE = new FrequencyGrid();
		SERVER_INSTANCE = new FrequencyGrid();
	}

	public static FrequencyGrid instance()
	{
		Thread thr = Thread.currentThread();

		if ((thr instanceof ThreadMinecraftServer) || (thr instanceof ServerListenThread) || (thr instanceof IServerThread))
		{
			return SERVER_INSTANCE;
		}

		return CLIENT_INSTANCE;
	}
}
