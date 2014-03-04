package resonantinduction.mechanical.process.crusher;

import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.energy.network.TileMechanical;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.MovementUtility;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class TileMechanicalPiston extends TileMechanical
{
	public TileMechanicalPiston()
	{
		mechanicalNode = new PacketMechanicalNode(this)
		{
			@Override
			protected void revolve()
			{
				if (!worldObj.isRemote)
				{
					Vector3 movePosition = new Vector3(TileMechanicalPiston.this).translate(getDirection());
					Vector3 moveNewPosition = movePosition.clone().translate(getDirection());

					if (canMove(movePosition, moveNewPosition))
						move(movePosition, moveNewPosition);
				}
			}

			@Override
			public boolean canConnect(ForgeDirection from, Object source)
			{
				return from != getDirection();
			}

		}.setLoad(0.5f);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		Vector3 movePosition = new Vector3(this).translate(getDirection());
		Vector3 moveNewPosition = movePosition.clone().translate(getDirection());

		if (!canMove(movePosition, moveNewPosition))
			mechanicalNode.angle = 0;
	}

	public boolean canMove(Vector3 from, Vector3 to)
	{
		TileEntity tileEntity = from.getTileEntity(worldObj);

		if (to.getTileEntity(worldObj) == this)
		{
			return false;
		}

		/** Check Target */
		int targetBlockID = to.getBlockID(worldObj);

		if (!(worldObj.isAirBlock(to.intX(), to.intY(), to.intZ()) || (targetBlockID > 0 && (Block.blocksList[targetBlockID].isBlockReplaceable(worldObj, to.intX(), to.intY(), to.intZ())))))
		{
			return false;
		}

		return true;
	}

	public void move(Vector3 from, Vector3 to)
	{
		int blockID = from.getBlockID(worldObj);
		int blockMetadata = from.getBlockMetadata(worldObj);

		TileEntity tileEntity = from.getTileEntity(worldObj);

		NBTTagCompound tileData = new NBTTagCompound();

		if (tileEntity != null)
		{
			tileEntity.writeToNBT(tileData);
		}

		MovementUtility.setBlockSneaky(worldObj, from, 0, 0, null);

		if (tileEntity != null && tileData != null)
		{
			/**
			 * Forge Multipart Support. Use FMP's custom TE creator.
			 */
			boolean isMultipart = tileData.getString("id").equals("savedMultipart");

			TileEntity newTile = null;

			if (isMultipart)
			{
				try
				{
					Class multipart = Class.forName("codechicken.multipart.MultipartHelper");
					Method m = multipart.getMethod("createTileFromNBT", World.class, NBTTagCompound.class);
					newTile = (TileEntity) m.invoke(null, worldObj, tileData);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				newTile = TileEntity.createAndLoadEntity(tileData);
			}

			MovementUtility.setBlockSneaky(worldObj, to, blockID, blockMetadata, newTile);

			if (newTile != null && isMultipart)
			{
				try
				{
					// Send the description packet of the TE after moving it.
					Class multipart = Class.forName("codechicken.multipart.MultipartHelper");
					multipart.getMethod("sendDescPacket", World.class, TileEntity.class).invoke(null, worldObj, newTile);

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
			MovementUtility.setBlockSneaky(worldObj, to, blockID, blockMetadata, null);
		}

		notifyChanges(from);
		notifyChanges(to);
	}

	public void notifyChanges(Vector3 pos)
	{
		worldObj.notifyBlocksOfNeighborChange(pos.intX(), pos.intY(), pos.intZ(), pos.getBlockID(worldObj));

		TileEntity newTile = pos.getTileEntity(worldObj);

		if (newTile != null)
		{
			if (Loader.isModLoaded("BuildCraft|Factory"))
			{
				/**
				 * Special quarry compatibility code.
				 */
				try
				{
					Class clazz = Class.forName("buildcraft.factory.TileQuarry");

					if (clazz == newTile.getClass())
					{
						ReflectionHelper.setPrivateValue(clazz, newTile, true, "isAlive");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
