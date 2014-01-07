package mffs.event;

import java.lang.reflect.Method;

import calclavia.lib.utility.MovementUtility;
import mffs.DelayedEvent;
import mffs.IDelayedEventHandler;
import mffs.api.EventForceManipulate.EventPostForceManipulate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

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
	private VectorWorld newPosition;

	private int blockID = 0;
	private int blockMetadata = 0;
	private TileEntity tileEntity;
	private NBTTagCompound tileData;

	public BlockPostMoveDelayedEvent(IDelayedEventHandler handler, int ticks, World world, Vector3 originalPosition, VectorWorld newPosition, int blockID, int blockMetadata, TileEntity tileEntity, NBTTagCompound tileData)
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
						 * Forge Multipart Support. Use FMP's custom TE creator.
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

						MovementUtility.setBlockSneaky(this.newPosition.world, this.newPosition, this.blockID, this.blockMetadata, newTile);

						if (newTile != null && isMultipart)
						{
							try
							{
								// Send the description packet of the TE after moving it.
								Class multipart = Class.forName("codechicken.multipart.MultipartHelper");
								multipart.getMethod("sendDescPacket", World.class, TileEntity.class).invoke(null, this.world, newTile);

								// Call onMoved event.
								Class tileMultipart = Class.forName("codechicken.multipart.TileMultipart");
								tileMultipart.getMethod("onMoved").invoke(newTile);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else
					{
						MovementUtility.setBlockSneaky(this.newPosition.world, this.newPosition, this.blockID, this.blockMetadata, null);
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
