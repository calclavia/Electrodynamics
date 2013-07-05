package mffs.event;

import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import mffs.ManipulatorHelper;
import mffs.api.ISpecialForceManipulation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

/**
 * Removes the TileEntity
 * 
 * @author Calclavia
 * 
 */
public class BlockPreMoveDelayedEvent extends DelayedEvent
{
	private World world;
	private Vector3 position;
	private Vector3 newPosition;

	public BlockPreMoveDelayedEvent(IDelayedEventHandler handler, int ticks, World world, Vector3 position, Vector3 newPosition)
	{
		super(handler, ticks);
		this.world = world;
		this.position = position;
		this.newPosition = newPosition;
	}

	@Override
	protected void onEvent()
	{
		if (!this.world.isRemote)
		{
			TileEntity tileEntity = this.position.getTileEntity(this.world);

			if (tileEntity instanceof ISpecialForceManipulation)
			{
				((ISpecialForceManipulation) tileEntity).move(newPosition.intX(), newPosition.intY(), newPosition.intZ());
			}

			int blockID = this.position.getBlockID(this.world);
			int blockMetadata = this.position.getBlockMetadata(this.world);

			NBTTagCompound tileData = new NBTTagCompound();

			if (tileEntity != null)
			{
				tileEntity.writeToNBT(tileData);
			}

			ManipulatorHelper.setBlockSneaky(this.world, this.position, 0, 0, null);
			this.handler.getQuedDelayedEvents().add(new BlockPostMoveDelayedEvent(this.handler, 0, this.world, this.position, this.newPosition, blockID, blockMetadata, tileEntity, tileData));
		}
	}
}
