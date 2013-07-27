package mffs.event;

import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public class BlockDropDelayedEvent extends DelayedEvent
{
	protected Block block;
	protected World world;
	protected Vector3 position;

	public BlockDropDelayedEvent(IDelayedEventHandler handler, int ticks, Block block, World world, Vector3 position)
	{
		super(handler, ticks);
		this.block = block;
		this.world = world;
		this.position = position;
	}

	@Override
	protected void onEvent()
	{
		if (!this.world.isRemote)
		{
			if (this.position.getBlockID(this.world) == this.block.blockID)
			{
				this.block.dropBlockAsItem(this.world, this.position.intX(), this.position.intY(), this.position.intZ(), this.position.getBlockMetadata(world), 0);
				this.position.setBlock(this.world, 0);
			}
		}
	}
}
