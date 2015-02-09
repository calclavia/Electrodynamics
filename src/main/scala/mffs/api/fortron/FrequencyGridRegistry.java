package mffs.api.fortron;

import com.resonant.wrapper.core.api.tile.BlockFrequency;
import net.minecraft.world.World;
import nova.core.util.transform.Cuboid;
import nova.core.util.transform.Vector3d;

import java.util.Set;

/**
 * A grid MFFS uses to search for machines with frequencies that can be linked and spread Fortron
 * energy.
 * @author Calclavia
 */
public class FrequencyGridRegistry {
	public static IFrequencyGrid CLIENT_INSTANCE;
	public static IFrequencyGrid SERVER_INSTANCE;

	public static IFrequencyGrid instance() {
		Thread thr = Thread.currentThread();

		if (thr.getName().equals("Server thread") || thr instanceof IServerThread) {
			return SERVER_INSTANCE;
		}

		return CLIENT_INSTANCE;
	}

	public static interface IFrequencyGrid {
		void add(BlockFrequency tileEntity);

		void remove(BlockFrequency tileEntity);

		Set<BlockFrequency> getNodes();

		<C extends BlockFrequency> Set<C> getNodes(Class<C> clazz);

		/**
		 * Gets a list of TileEntities that has a specific frequency.
		 */
		Set<BlockFrequency> getNodes(int frequency);

		<C extends BlockFrequency> Set<C> getNodes(Class<C> clazz, int frequency);

		/**
		 * Gets a list of TileEntities that has a specific frequency, within a radius around a position.
		 */
		Set<BlockFrequency> getNodes(World world, Vector3d position, int radius, int frequency);

		<C extends BlockFrequency> Set<C> getNodes(Class<C> clazz, World world, Vector3d position, int radius, int frequency);

		Set<BlockFrequency> getNodes(World world, Cuboid region, int frequency);

		<C extends BlockFrequency> Set<C> getNodes(Class<C> clazz, World world, Cuboid region, int frequency);
	}
}
