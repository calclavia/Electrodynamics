package mffs.tileentity;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import mffs.BlockSetDelayedEvent;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.PacketManager;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityForceManipulator extends TileEntityFieldInteraction
{
	private static final int ANIMATION_TIME = 20;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote && this.getMode() != null)
		{
			if (this.isActive() && this.ticks % 20 == 0)
			{

				/**
				 * Move
				 */
				ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
				boolean canMove = true;

				/**
				 * Scan target area...
				 */
				for (Vector3 position : this.getInteriorPoints())
				{
					if (position.getBlockID(this.worldObj) > 0)
					{
						Vector3 targetPosition = position.clone().modifyPositionFromSide(dir);

						if (!this.getInteriorPoints().contains(targetPosition))
						{
							int blockID = targetPosition.getBlockID(this.worldObj);

							if (!(blockID == 0 || (blockID > 0 && Block.blocksList[blockID].isBlockReplaceable(this.worldObj, targetPosition.intX(), targetPosition.intY(), targetPosition.intZ()))))
							{
								System.out.println("BREAK AT " + position + " vs " + position.getBlockID(this.worldObj) + " in " + targetPosition + ":" + blockID);
								canMove = false;
								break;
							}
						}
					}
				}

				if (canMove)
				{
					this.updatePushedObjects(1, 0.25f);

					for (Vector3 position : this.getInteriorPoints())
					{
						this.moveBlock(position, dir);
					}
				}

				this.setActive(false);
			}

			if (!this.isCalculated)
			{
				this.calculateForceField();
			}

			// Manipulation area preview
			if (this.ticks % 120 == 0 && !this.isCalculating && Settings.HIGH_GRAPHICS)
			{
				for (Vector3 position : this.getCalculatedField())
				{
					if (position.getBlockID(this.worldObj) > 0)
					{
						PacketManager.sendPacketToClients(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, this, TilePacketType.FXS.ordinal(), position.intX(), position.intY(), position.intZ()), worldObj, position, 50);
					}
				}
			}
		}
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		/**
		 * Holographic FXs
		 */
		if (packetID == TilePacketType.FXS.ordinal() && this.worldObj.isRemote)
		{
			Vector3 vector = new Vector3(dataStream.readInt(), dataStream.readInt(), dataStream.readInt()).add(0.5);
			ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 1, 1, 1, 30, vector.clone().modifyPositionFromSide(this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord)));

		}
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		this.isCalculated = false;
	}

	protected void moveBlock(Vector3 position, ForgeDirection direction)
	{
		if (!this.worldObj.isRemote)
		{
			Vector3 newPosition = position.clone().modifyPositionFromSide(direction);

			TileEntity tileEntity = position.getTileEntity(this.worldObj);
			int blockID = position.getBlockID(this.worldObj);

			if (blockID > 0 && newPosition.getBlockID(this.worldObj) == 0)
			{
				if (Block.blocksList[blockID].getBlockHardness(this.worldObj, position.intX(), position.intY(), position.intZ()) != -1 && tileEntity != this)
				{
					this.getDelayedEvents().add(new BlockSetDelayedEvent(ANIMATION_TIME, this.worldObj, position, newPosition));
					PacketManager.sendPacketToClients(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, this, TilePacketType.FXS.ordinal(), position.intX(), position.intY(), position.intZ()), worldObj, position, 50);
				}
			}
		}
	}

	private void updatePushedObjects(float distance, float amount)
	{
		ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		AxisAlignedBB axisalignedbb = this.getSearchAxisAlignedBB(distance, dir.ordinal());

		if (axisalignedbb != null)
		{
			List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);

			Iterator<Entity> iterator = list.iterator();

			while (iterator.hasNext())
			{
				Entity entity = iterator.next();
				entity.moveEntity(amount * dir.offsetX, amount * dir.offsetY, amount * dir.offsetZ);
			}
		}
	}

	public AxisAlignedBB getSearchAxisAlignedBB(float distance, int direction)
	{
		AxisAlignedBB axisalignedbb = this.getBlockType().getCollisionBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord);

		if (axisalignedbb == null)
		{
			return null;
		}
		else
		{
			axisalignedbb.maxY += distance;
			return axisalignedbb;
		}

	}

	@Override
	public int getSizeInventory()
	{
		return 3 + 18;
	}

}
