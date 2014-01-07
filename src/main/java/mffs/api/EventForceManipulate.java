/**
 * 
 */
package mffs.api;

import net.minecraft.world.World;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Events for the Force Manipulator
 * 
 * @author Calclavia
 * 
 */
public abstract class EventForceManipulate extends WorldEvent
{
	public int beforeX, beforeY, beforeZ, afterX, afterY, afterZ;

	public EventForceManipulate(World world, int beforeX, int beforeY, int beforeZ, int afterX, int afterY, int afterZ)
	{
		super(world);
		this.beforeX = beforeX;
		this.beforeY = beforeY;
		this.beforeZ = beforeZ;
		this.afterX = afterX;
		this.afterY = afterY;
		this.afterZ = afterZ;
	}

	/**
	 * Called every single time a block is checked if it can be manipulated by the Force
	 * Manipulator.
	 * 
	 * @author Calclavia
	 * 
	 */
	@Cancelable
	public static class EventCheckForceManipulate extends EventForceManipulate
	{
		public EventCheckForceManipulate(World world, int beforeX, int beforeY, int beforeZ, int afterX, int afterY, int afterZ)
		{
			super(world, beforeX, beforeY, beforeZ, afterX, afterY, afterZ);
		}

	}

	/**
	 * Called right before the TileEntity is moved. After this function is called, the force
	 * manipulator will write all TileEntity data into NBT and remove the TileEntity block. A new
	 * TileEntity class will be instantiated after words in the new position. This can be canceled
	 * and the
	 * block will then not move at all.
	 * 
	 */
	@Cancelable
	public static class EventPreForceManipulate extends EventForceManipulate
	{
		public EventPreForceManipulate(World world, int beforeX, int beforeY, int beforeZ, int afterX, int afterY, int afterZ)
		{
			super(world, beforeX, beforeY, beforeZ, afterX, afterY, afterZ);
		}

	}

	/**
	 * Called after a block is moved by the Force Manipulator and when all move operations are
	 * completed. This is called before the placed block get notified of neighborBlockChange.
	 * 
	 * @author Calclavia
	 * 
	 */
	public static class EventPostForceManipulate extends EventForceManipulate
	{
		public EventPostForceManipulate(World world, int beforeX, int beforeY, int beforeZ, int afterX, int afterY, int afterZ)
		{
			super(world, beforeX, beforeY, beforeZ, afterX, afterY, afterZ);
		}
	}
}
