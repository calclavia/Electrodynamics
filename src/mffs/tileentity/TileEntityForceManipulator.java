package mffs.tileentity;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import mffs.BlockSetDelayedEvent;
import mffs.BlockSneakySetDelayedEvent;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.ForceManipulatorBlacklist;
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
	public Vector3 anchor = null;

	public boolean isCalculatingManipulation = false;
	public Set<Vector3> manipulationVectors = null;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.anchor == null)
		{
			this.anchor = new Vector3(this);
		}

		if (!this.worldObj.isRemote && this.getMode() != null)
		{
			if (this.manipulationVectors != null && !this.isCalculatingManipulation)
			{
				ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);

				for (Vector3 position : this.manipulationVectors)
				{
					this.moveBlock(position, dir);
				}

				this.updatePushedObjects(5);

				this.manipulationVectors = null;
			}

			if (this.isActive() && this.ticks % 20 == 0 && this.requestFortron(this.getFortronCost() * 100, false) > 0)
			{
				this.requestFortron(this.getFortronCost() * 100, true);
				// Start multi-threading calculations
				(new ManipulatorCalculationThread(this)).start();

				this.setActive(false);
			}

			if (!this.isCalculated)
			{
				this.calculateForceField();
			}

			// Manipulation area preview
			if (this.ticks % 120 == 0 && !this.isCalculating && Settings.HIGH_GRAPHICS)
			{
				for (Vector3 position : this.getInteriorPoints())
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

	/**
	 * Scan target area...
	 */
	protected boolean canMove()
	{
		Set<Vector3> mobilizationPoints = this.getInteriorPoints();
		ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);

		loop:
		for (Vector3 position : mobilizationPoints)
		{
			if (position.getBlockID(this.worldObj) > 0)
			{
				if (ForceManipulatorBlacklist.blackList.contains(Block.blocksList[position.getBlockID(this.worldObj)]) || Block.blocksList[position.getBlockID(this.worldObj)].getBlockHardness(this.worldObj, position.intX(), position.intY(), position.intZ()) == -1)
				{
					return false;
				}

				Vector3 targetPosition = position.clone().modifyPositionFromSide(dir);

				for (Vector3 checkPos : mobilizationPoints)
				{
					if (checkPos.equals(targetPosition))
					{
						continue loop;
					}
				}

				int blockID = targetPosition.getBlockID(this.worldObj);

				if (!(blockID == 0 || (blockID > 0 && Block.blocksList[blockID].isBlockReplaceable(this.worldObj, targetPosition.intX(), targetPosition.intY(), targetPosition.intZ()))))
				{
					return false;
				}
			}
		}

		return true;
	}

	protected void moveBlock(Vector3 position, ForgeDirection direction)
	{
		if (!this.worldObj.isRemote)
		{
			Vector3 newPosition = position.clone().modifyPositionFromSide(direction);

			TileEntity tileEntity = position.getTileEntity(this.worldObj);
			int blockID = position.getBlockID(this.worldObj);

			if (blockID > 0)
			{
				if (Block.blocksList[blockID].getBlockHardness(this.worldObj, position.intX(), position.intY(), position.intZ()) != -1 && tileEntity != this)
				{
					this.getDelayedEvents().add(new BlockSneakySetDelayedEvent(ANIMATION_TIME - 1, this.worldObj, position, 0, 0));
					this.getDelayedEvents().add(new BlockSetDelayedEvent(ANIMATION_TIME, this.worldObj, position, newPosition));
					PacketManager.sendPacketToClients(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, this, TilePacketType.FXS.ordinal(), position.intX(), position.intY(), position.intZ()), worldObj, position, 50);
				}
			}
		}
	}

	public void updatePushedObjects(float amount)
	{
		ForgeDirection dir = this.getDirection(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		AxisAlignedBB axisalignedbb = this.getSearchAxisAlignedBB();

		if (axisalignedbb != null)
		{
			@SuppressWarnings("unchecked")
			List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, axisalignedbb);
			System.out.println(entities.size() + " vs " + axisalignedbb);
			for (Entity entity : entities)
			{
				entity.moveEntity(amount * dir.offsetX, amount * dir.offsetY, amount * dir.offsetZ);
			}
		}
	}

	public AxisAlignedBB getSearchAxisAlignedBB()
	{
		Vector3 positiveScale = new Vector3(this).add(this.getTranslation()).add(this.getPositiveScale());
		Vector3 negativeScale = new Vector3(this).add(this.getTranslation()).subtract(this.getTranslation());

		Vector3 minScale = new Vector3(Math.min(positiveScale.x, negativeScale.x), Math.min(positiveScale.y, negativeScale.y), Math.min(positiveScale.z, negativeScale.z));
		Vector3 maxScale = new Vector3(Math.max(positiveScale.x, negativeScale.x), Math.max(positiveScale.y, negativeScale.y), Math.max(positiveScale.z, negativeScale.z));

		return AxisAlignedBB.getAABBPool().getAABB(minScale.intX(), minScale.intY(), minScale.intZ(), maxScale.intX(), maxScale.intY(), maxScale.intZ());
	}

	@Override
	public int getSizeInventory()
	{
		return 3 + 18;
	}

}
