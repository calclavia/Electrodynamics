package mffs.event;

import java.util.ArrayList;

import mffs.IDelayedEventHandler;
import mffs.base.TileEntityInventory;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

public class BlockInventoryDropDelayedEvent extends BlockDropDelayedEvent
{
	private TileEntityInventory projector;

	public BlockInventoryDropDelayedEvent(IDelayedEventHandler handler, int ticks, Block block, World world, Vector3 position, TileEntityInventory projector)
	{
		super(handler, ticks, block, world, position);
		this.projector = projector;
	}

	@Override
	protected void onEvent()
	{
		if (!this.world.isRemote)
		{
			if (this.position.getBlockID(this.world) == this.block.blockID)
			{
				ArrayList<ItemStack> itemStacks = this.block.getBlockDropped(this.world, this.position.intX(), this.position.intY(), this.position.intZ(), this.position.getBlockMetadata(world), 0);

				for (ItemStack itemStack : itemStacks)
				{
					this.projector.mergeIntoInventory(itemStack);
				}

				this.position.setBlock(this.world, 0);
			}
		}
	}
}
