package mffs.fortron;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mffs.api.fortron.IFortronFrequency;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * A grid MFFS uses to search for machines with frequencies that can be linked and spread Fortron
 * energy.
 * 
 * @author Calclavia
 * 
 */
public class FortronGrid
{
	private static FortronGrid CLIENT_INSTANCE = new FortronGrid();
	private static FortronGrid SERVER_INSTANCE = new FortronGrid();

	private final Set<IFortronFrequency> frequencyGrid = new HashSet<IFortronFrequency>();

	public void register(IFortronFrequency tileEntity)
	{
		this.cleanUp();
		this.frequencyGrid.add(tileEntity);
	}

	public void unregister(IFortronFrequency tileEntity)
	{
		this.frequencyGrid.remove(tileEntity);
		this.cleanUp();
	}

	public Set<IFortronFrequency> get()
	{
		return this.frequencyGrid;
	}

	/**
	 * Gets a list of TileEntities that has a specific frequency.
	 * 
	 * @param frequency - The Frequency
	 * */
	public Set<IFortronFrequency> get(int frequency)
	{
		Set<IFortronFrequency> set = new HashSet<IFortronFrequency>();

		for (IFortronFrequency tile : this.get())
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
		try
		{
			Iterator<IFortronFrequency> it = this.frequencyGrid.iterator();

			while (it.hasNext())
			{
				IFortronFrequency frequency = it.next();

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

				if (((TileEntity) frequency).worldObj.getBlockTileEntity(((TileEntity) frequency).xCoord, ((TileEntity) frequency).yCoord, ((TileEntity) frequency).zCoord) != ((TileEntity) frequency))
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
	}

	public Set<IFortronFrequency> get(World world, Vector3 position, int radius, int frequency)
	{
		Set<IFortronFrequency> set = new HashSet<IFortronFrequency>();

		for (IFortronFrequency tileEntity : this.get(frequency))
		{
			if (Vector3.distance(new Vector3((TileEntity) tileEntity), position) <= radius)
			{
				set.add(tileEntity);
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
		CLIENT_INSTANCE = new FortronGrid();
		SERVER_INSTANCE = new FortronGrid();
	}

	public static FortronGrid instance()
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			return SERVER_INSTANCE;
		}

		return CLIENT_INSTANCE;
	}
}
