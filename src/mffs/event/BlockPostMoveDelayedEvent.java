package mffs.event;

import java.lang.reflect.Method;

import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import mffs.ManipulatorHelper;
import mffs.api.EventForceManipulate.EventPostForceManipulate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
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
						/**
						 * Forge Multipart Support.
						 */
						boolean isMultipart = this.tileData.getString("id").equals("savedMultipart");

						TileEntity newTile = null;

						if (isMultipart)
						{
							try
							{
								Class multipart = Class.forName("codechicken.multipart.MultipartHelper");
								Method m = multipart.getMethod("createTileFromNBT", World.class, NBTTagCompound.class);
								newTile = (TileEntity) m.invoke(null, this.world, this.tileData);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						else
						{
							newTile = TileEntity.createAndLoadEntity(this.tileData);
						}

						ManipulatorHelper.setBlockSneaky(this.world, this.newPosition, this.blockID, this.blockMetadata, newTile);

						if (newTile != null && isMultipart)
						{
							try
							{
								Class multipart = Class.forName("codechicken.multipart.MultipartHelper");
								newTile = (TileEntity) multipart.getMethod("sendDescPacket", World.class, TileEntity.class).invoke(null, this.world, newTile);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else
					{
						ManipulatorHelper.setBlockSneaky(this.world, this.newPosition, this.blockID, this.blockMetadata, null);
					}

					this.handler.getQuedDelayedEvents().add(new BlockNotifyDelayedEvent(this.handler, 0, this.world, this.originalPosition));
					this.handler.getQuedDelayedEvents().add(new BlockNotifyDelayedEvent(this.handler, 0, this.world, this.newPosition));

					MinecraftForge.EVENT_BUS.post(new EventPostForceManipulate(this.world, this.originalPosition.intX(), this.originalPosition.intY(), this.originalPosition.intZ(), this.newPosition.intX(), this.newPosition.intY(), this.newPosition.intZ()));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
