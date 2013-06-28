package mffs.event;

import mffs.DelayedEvent;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public class BlockSneakySetDelayedEvent extends DelayedEvent
{
	private World world;
	private Vector3 position;
	private int blockID;
	private int blockMetadata;

	public BlockSneakySetDelayedEvent(int ticks, World world, Vector3 position, int blockID, int blockMetadata)
	{
		super(ticks);
		this.world = world;
		this.position = position;
		this.blockID = blockID;
		this.blockMetadata = blockMetadata;
	}

	@Override
	protected void onEvent()
	{
		if (!this.world.isRemote)
		{
			this.world.removeBlockTileEntity(this.position.intX(), this.position.intY(), this.position.intZ());
			this.position.setBlock(this.world, this.blockID, this.blockMetadata);
		}
	}
}
