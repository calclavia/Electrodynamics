package mffs.event;

import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import mffs.api.EventForceManipulate.EventPreForceManipulate;
import mffs.tile.TileForceManipulator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import calclavia.lib.utility.MovementUtility;

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

					NBTTagCompound tileData = new NBTTagCompound();

					if (tileEntity != null)
					{
						tileEntity.writeToNBT(tileData);
					}

					MovementUtility.setBlockSneaky(this.world, this.position, 0, 0, null);
					this.handler.getQuedDelayedEvents().add(new BlockPostMoveDelayedEvent(this.handler, 0, this.world, this.position, this.newPosition, blockID, blockMetadata, tileEntity, tileData));
				}
			}
			else
			{
				((TileForceManipulator) this.handler).markFailMove = true;
			}
		}
	}
}
