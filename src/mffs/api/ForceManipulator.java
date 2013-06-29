package mffs.api;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;

/**
 * Adds blocks to this black list if you do not wish them to be moved by the force manipulator.
 * 
 * @author User
 * 
 */
public class ForceManipulator
{
	public static final Set<Block> blackList = new HashSet<Block>();

	/**
	 * Applied to TileEntities who would like to handle movement by the force manipulation in a
	 * better way.
	 * 
	 * @author Calclavia
	 * 
	 */
	public static interface ISpecialForceManipulation
	{
		/**
		 * Called before the TileEntity is moved.
		 * 
		 * @param Coords - X, Y, Z (Target location to be moved)
		 * @return True if it can be moved.
		 */
		public boolean preMove(int x, int y, int z);

		public void move(int x, int y, int z);

		/**
		 * Called after the TileEntity is moved.
		 */
		public void postMove();
	}
}
