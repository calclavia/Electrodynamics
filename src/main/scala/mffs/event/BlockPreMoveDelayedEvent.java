package mffs.event;

import calclavia.api.mffs.EventForceManipulate.EventPreForceManipulate;
import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import mffs.tile.TileForceManipulator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import resonant.lib.utility.MovementUtility;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

/**
 * Removes the TileEntity
 *
 * @author Calclavia
 */
public class BlockPreMoveDelayedEvent extends DelayedEvent
{
	private World world;
	private Vector3 position;
	private VectorWorld newPosition;

	public BlockPreMoveDelayedEvent(IDelayedEventHandler handler, int ticks, World world, Vector3 position, VectorWorld newPosition)
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
			// Do a final check before actually moving.
			if (((TileForceManipulator) this.handler).canMove(new VectorWorld(world, position), newPosition))
			{
				TileEntity tileEntity = this.position.getTileEntity(this.world);

				EventPreForceManipulate evt = new EventPreForceManipulate(this.world, this.position.intX(), this.position.intY(), this.position.intZ(), this.newPosition.intX(), this.newPosition.intY(), this.newPosition.intZ());
				MinecraftForge.EVENT_BUS.post(evt);

				if (!evt.isCanceled())
				{
					int blockID = this.position.getBlockID(this.world);
					int blockMetadata = this.position.getBlockMetadata(this.world);

					MovementUtility.setBlockSneaky(this.world, this.position, 0, 0, null);

					NBTTagCompound tileData = new NBTTagCompound();

					if (tileEntity != null)
					{
						tileEntity.writeToNBT(tileData);
					}

					this.handler.queueEvent(new BlockPostMoveDelayedEvent(this.handler, 0, this.world, this.position, this.newPosition, blockID, blockMetadata, tileEntity, tileData));
				}
			}
			else
			{
				((TileForceManipulator) this.handler).markFailMove = true;
			}
		}
	}
}
