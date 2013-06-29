package mffs.event;

import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

/**
 * Removes the TileEntity
 * 
 * @author Calclavia
 * 
 */
public class BlockNotifyDelayedEvent extends DelayedEvent
{
	private World world;
	private Vector3 position;

	public BlockNotifyDelayedEvent(IDelayedEventHandler handler, int ticks, World world, Vector3 position)
	{
		super(handler, ticks);
		this.world = world;
		this.position = position;
	}

	@Override
	protected void onEvent()
	{
		if (!this.world.isRemote)
		{
			this.world.notifyBlocksOfNeighborChange(this.position.intX(), this.position.intY(), this.position.intZ(), this.position.getBlockID(this.world));
		}
	}
}
