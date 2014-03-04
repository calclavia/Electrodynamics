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
import resonantinduction.api.mechanical.IBelt;
import resonantinduction.api.mechanical.IMechanical;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.energy.network.IMechanicalNodeProvider;
import resonantinduction.mechanical.energy.network.MechanicalNode;
import resonantinduction.mechanical.energy.network.TileMechanical;
import resonantinduction.mechanical.energy.network.TileMechanical.PacketMechanicalNode;
import resonantinduction.mechanical.process.crusher.TileMechanicalPiston;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.IRotatable;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * Conveyer belt TileEntity that allows entities of all kinds to be moved
 * 
 * @author DarkGuardsman
 */
public class TileConveyorBelt extends TileMechanical implements IBelt, IRotatable
{
	public enum SlantType
	{
		NONE, UP, DOWN, TOP
	}

	public TileConveyorBelt()
	{
		mechanicalNode = new PacketMechanicalNode(this)
		{
			@Override
			public void recache()
			{
				synchronized (connections)
				{
					connections.clear();

					boolean didRefresh = false;

					for (int i = 2; i < 6; i++)
					{
						ForgeDirection dir = ForgeDirection.getOrientation(i);
						Vector3 pos = new Vector3(TileConveyorBelt.this).translate(dir);
						TileEntity tile = pos.getTileEntity(TileConveyorBelt.this.worldObj);

						if (dir == TileConveyorBelt.this.getDirection() || dir == TileConveyorBelt.this.getDirection().getOpposite())
						{
							if (dir == TileConveyorBelt.this.getDirection())
							{
								if (TileConveyorBelt.this.slantType == SlantType.DOWN)
								{
									pos.translate(new Vector3(0, -1, 0));
								}
								else if (TileConveyorBelt.this.slantType == SlantType.UP)
								{
									pos.translate(new Vector3(0, 1, 0));
								}
							}
							else if (dir == TileConveyorBelt.this.getDirection().getOpposite())
							{
								if (TileConveyorBelt.this.slantType == SlantType.DOWN)
								{
									pos.translate(new Vector3(0, 1, 0));
								}
								else if (TileConveyorBelt.this.slantType == SlantType.UP)
								{
									pos.translate(new Vector3(0, -1, 0));
								}
							}

							tile = pos.getTileEntity(worldObj);

							if (tile instanceof TileConveyorBelt)
							{
								connections.put(((TileConveyorBelt) tile).getNode(dir.getOpposite()), dir);
								didRefresh = true;
							}
						}
						else if (tile instanceof IMechanicalNodeProvider)
						{
							MechanicalNode mechanical = ((IMechanicalNodeProvider) tile).getNode(dir.getOpposite());

							if (mechanical != null)
							{
								connections.put(mechanical, dir);
							}
						}
					}

					if (!worldObj.isRemote)
					{
						markRefresh = true;
					}
				}
			}

			@Override
			public boolean canConnect(ForgeDirection from, Object source)
			{
				return from != getDirection() || from != getDirection().getOpposite();
			}
		}.setLoad(0.5f);
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
				worldObj.playSound(this.xCoord, this.yCoord, this.zCoord, Reference.PREFIX + "conveyor", 0.5f, 0.5f + 0.15f * (float) getMoveVelocity(), true);
			}

			double beltPercentage = mechanicalNode.angle / (2 * Math.PI);

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
	public void onReceivePacket(int id, ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		super.onReceivePacket(id, data, player, extra);

		if (id == PACKET_SLANT)
			this.slantType = SlantType.values()[data.readInt()];
		else if (id == PACKET_REFRESH)
			mechanicalNode.reconstruct();
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
		mechanicalNode.reconstruct();
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

	public double getMoveVelocity()
	{
		return Math.abs(mechanicalNode.getAngularVelocity());
	}
}
