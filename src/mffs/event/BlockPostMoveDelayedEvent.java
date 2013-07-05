package mffs.event;

import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import mffs.ManipulatorHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

/**
 * Sets the new position into the original TileEntities' block.
 * 
 * @author Calclavia
 * 
 */
public class BlockPostMoveDelayedEvent extends DelayedEvent
{
	private World world;
	private Vector3 originalPosition;
	private Vector3 newPosition;

	private int blockID = 0;
	private int blockMetadata = 0;
	private TileEntity tileEntity;
	private NBTTagCompound tileData;

	public BlockPostMoveDelayedEvent(IDelayedEventHandler handler, int ticks, World world, Vector3 originalPosition, Vector3 newPosition, int blockID, int blockMetadata, TileEntity tileEntity, NBTTagCompound tileData)
	{
		super(handler, ticks);
		this.world = world;
		this.originalPosition = originalPosition;
		this.newPosition = newPosition;
		this.blockID = blockID;
		this.blockMetadata = blockMetadata;
		this.tileEntity = tileEntity;
		this.tileData = tileData;
	}

	@Override
	protected void onEvent()
	{
		if (!this.world.isRemote)
		{
			if (this.blockID > 0)
			{
				try
				{
					if (this.tileEntity != null && this.tileData != null)
					{
						ManipulatorHelper.setBlockSneaky(this.world, this.newPosition, this.blockID, this.blockMetadata, TileEntity.createAndLoadEntity(this.tileData));
					}
					else
					{
						ManipulatorHelper.setBlockSneaky(this.world, this.newPosition, this.blockID, this.blockMetadata, null);
					}

					this.handler.getQuedDelayedEvents().add(new BlockNotifyDelayedEvent(this.handler, 0, this.world, this.originalPosition));
					this.handler.getQuedDelayedEvents().add(new BlockNotifyDelayedEvent(this.handler, 0, this.world, this.newPosition));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
