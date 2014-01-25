package resonantinduction.mechanical.belt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.IBelt;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.IMechanicalNetwork;
import resonantinduction.mechanical.network.TileMechanical;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiverWithID;
import calclavia.lib.prefab.tile.IRotatable;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * Conveyer belt TileEntity that allows entities of all kinds to be moved
 * 
 * @author DarkGuardsman
 */
public class TileConveyorBelt extends TileMechanical implements IBelt, IRotatable, IPacketReceiverWithID
{
	public enum SlantType
	{
		NONE, UP, DOWN, TOP
	}

	/**
	 * Static constants.
	 */
	public static final int MAX_FRAME = 13;
	public static final int MAX_SLANT_FRAME = 23;
	public static final int PACKET_SLANT = Mechanical.contentRegistry.getNextPacketID();
	public static final int PACKET_REFRESH = Mechanical.contentRegistry.getNextPacketID();
	/** Acceleration of entities on the belt */
	public static final float ACCELERATION = 0.01f;

	/** Frame count for texture animation from 0 - maxFrame */
	private int animationFrame = 0;

	private SlantType slantType = SlantType.NONE;

	/** Entities that are ignored allowing for other tiles to interact with them */
	public List<Entity> ignoreList = new ArrayList<Entity>();

	private boolean markRefresh = true;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		/* PROCESSES IGNORE LIST AND REMOVES UNNEED ENTRIES */
		Iterator<Entity> it = this.ignoreList.iterator();
		while (it.hasNext())
		{
			if (!this.getAffectedEntities().contains(it.next()))
			{
				it.remove();
			}
		}

		if (this.worldObj.isRemote)
		{
			if (this.ticks % 10 == 0 && this.worldObj.isRemote && this.worldObj.getBlockId(this.xCoord - 1, this.yCoord, this.zCoord) != Mechanical.blockConveyorBelt.blockID && this.worldObj.getBlockId(xCoord, yCoord, zCoord - 1) != Mechanical.blockConveyorBelt.blockID)
			{
				this.worldObj.playSound(this.xCoord, this.yCoord, this.zCoord, Reference.PREFIX + "conveyor", 0.5f, 0.7f, true);
			}

			angle = getNetwork().getRotation(getMoveVelocity());
			// (float) ((angle + getMoveVelocity() / 20) % Math.PI);
			double beltPercentage = angle / (2 * Math.PI);

			// Sync the animation. Slant belts are slower.
			if (this.getSlant() == SlantType.NONE || this.getSlant() == SlantType.TOP)
			{
				this.animationFrame = (int) (beltPercentage * MAX_FRAME);
				if (this.animationFrame < 0)
					this.animationFrame = 0;
				if (this.animationFrame > MAX_FRAME)
					this.animationFrame = MAX_FRAME;
			}
			else
			{
				this.animationFrame = (int) (beltPercentage * MAX_SLANT_FRAME);
				if (this.animationFrame < 0)
					this.animationFrame = 0;
				if (this.animationFrame > MAX_SLANT_FRAME)
					this.animationFrame = MAX_SLANT_FRAME;
			}
		}
		else
		{
			if (markRefresh)
			{
				sendRefreshPacket();
				markRefresh = false;
			}
		}

	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (this.slantType != SlantType.NONE)
		{
			return ResonantInduction.PACKET_TILE.getPacket(this, PACKET_SLANT, this.slantType.ordinal());
		}
		return super.getDescriptionPacket();
	}

	public void sendRefreshPacket()
	{
		PacketDispatcher.sendPacketToAllPlayers(ResonantInduction.PACKET_TILE.getPacket(this, PACKET_REFRESH));
	}

	@Override
	public boolean onReceivePacket(int id, ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		if (this.worldObj.isRemote)
		{
			try
			{
				if (id == PACKET_SLANT)
				{
					this.slantType = SlantType.values()[data.readInt()];
					return true;
				}
				else if (id == PACKET_REFRESH)
				{
					refresh();
					return true;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	public SlantType getSlant()
	{
		return slantType;
	}

	public void setSlant(SlantType slantType)
	{
		if (slantType == null)
		{
			slantType = SlantType.NONE;
		}
		this.slantType = slantType;
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void setDirection(ForgeDirection facingDirection)
	{
		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, facingDirection.ordinal(), 3);
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata());
	}

	@Override
	public List<Entity> getAffectedEntities()
	{
		return worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1));
	}

	public int getAnimationFrame()
	{
		return this.animationFrame;
	}

	/** NBT Data */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.slantType = SlantType.values()[nbt.getByte("slant")];
	}

	/** Writes a tile entity to NBT. */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setByte("slant", (byte) this.slantType.ordinal());
	}

	@Override
	public void ignoreEntity(Entity entity)
	{
		if (!this.ignoreList.contains(entity))
		{
			this.ignoreList.add(entity);
		}
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return direction != getDirection() || direction != getDirection().getOpposite();
	}

	public void refresh()
	{
		boolean didRefresh = false;

		for (int i = 2; i < 6; i++)
		{
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			Vector3 pos = new Vector3(this).translate(dir);
			TileEntity tile = pos.getTileEntity(this.worldObj);

			if (dir == this.getDirection() || dir == this.getDirection().getOpposite())
			{
				if (dir == this.getDirection())
				{
					if (this.slantType == SlantType.DOWN)
					{
						pos.translate(new Vector3(0, -1, 0));
					}
					else if (this.slantType == SlantType.UP)
					{
						pos.translate(new Vector3(0, 1, 0));
					}
				}
				else if (dir == this.getDirection().getOpposite())
				{
					if (this.slantType == SlantType.DOWN)
					{
						pos.translate(new Vector3(0, 1, 0));
					}
					else if (this.slantType == SlantType.UP)
					{
						pos.translate(new Vector3(0, -1, 0));
					}
				}

				tile = pos.getTileEntity(this.worldObj);

				if (tile instanceof IBelt)
				{
					connections[dir.ordinal()] = tile;
					getNetwork().merge(((IBelt) tile).getNetwork());
					didRefresh = true;
				}
			}
			else if (tile instanceof IMechanical)
			{
				IMechanical mechanical = (IMechanical) ((IMechanical) tile).getInstance(dir.getOpposite());

				if (mechanical != null)
				{
					connections[dir.ordinal()] = mechanical;
					getNetwork().merge(mechanical.getNetwork());
				}
			}
		}

		if (didRefresh)
		{
			if (!worldObj.isRemote)
			{
				markRefresh = true;
			}
		}
	}

	@Override
	public void invalidate()
	{
		getNetwork().split(this);
		super.invalidate();
	}

	public float getMoveVelocity()
	{
		return Math.abs(angularVelocity);
	}
}
