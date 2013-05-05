package mffs;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public class BlockDropDelayedEvent extends DelayedEvent
{
	private Block block;
	private World world;
	private Vector3 position;

	public BlockDropDelayedEvent(int ticks, Block block, World world, Vector3 position)
	{
		super(ticks);
		this.block = block;
		this.world = world;
		this.position = position;
	}

	protected void onEvent()
	{
		this.block.dropBlockAsItem(this.world, this.position.intX(), this.position.intY(), this.position.intZ(), this.position.getBlockMetadata(world), 0);
		this.position.setBlock(this.world, 0);
	}
}
