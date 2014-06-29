package mffs.event;

import mffs.tile.TileForceMobilizer;
import resonant.api.mffs.EventForceManipulate.EventPreForceManipulate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import resonant.lib.utility.MovementUtility;
import universalelectricity.core.transform.vector.Vector3;
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
			if (((TileForceMobilizer) this.handler).canMove(new VectorWorld(world, position), newPosition))
			{
				TileEntity tileEntity = this.position.getTileEntity(this.world);

				EventPreForceManipulate evt = new EventPreForceManipulate(this.world, this.position.xi(), this.position.yi(), this.position.zi(), this.newPosition.xi(), this.newPosition.yi(), this.newPosition.zi());
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
				((TileForceMobilizer) this.handler).markFailMove = true;
			}
		}
	}
}
