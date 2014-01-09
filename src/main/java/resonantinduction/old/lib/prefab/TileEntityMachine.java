package resonantinduction.old.lib.prefab;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.network.ISimplePacketReceiver;
import resonantinduction.old.lib.IExtraInfo.IExtraTileEntityInfo;
import resonantinduction.old.lib.interfaces.IExternalInv;
import resonantinduction.old.lib.interfaces.IInvBox;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.PacketHandler;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public abstract class TileEntityMachine extends TileEntityInv implements ISidedInventory, IExternalInv, ISimplePacketReceiver, IExtraTileEntityInfo
{
	/** Number of players with the machine's gui container open */
	protected int playersUsingMachine = 0;
	/** Is the machine functioning normally */
	protected boolean functioning = false;
	/** Prev state of function of last update */
	protected boolean prevFunctioning = false;
	/** Does the machine have a gui */
	protected boolean hasGUI = false;
	/** Does teh machine rotate in meta groups of four */
	protected boolean rotateByMetaGroup = false;
	/** Can the machine be temp disabled */
	protected boolean canBeDisabled = false;
	/** Is the machine enabled by the player */
	protected boolean enabled = true;
	/** Is the machine locked by the player */
	protected boolean locked = false;

	/** Inventory manager used by this machine */
	protected IInvBox inventory;

	/** Default generic packet types used by all machines */
	public static enum SimplePacketTypes
	{
		/** Normal packet data of any kind */
		GENERIC("generic"),
		/** Power updates */
		RUNNING("isRunning"),
		/** GUI display data update */
		GUI("guiGeneral"),
		/** Full tile read/write data from tile NBT */
		NBT("nbtAll"), GUI_EVENT("clientGui"), GUI_COMMAND("clientCommand"),
		TERMINAL_OUTPUT("serverTerminal");

		public String name;

		private SimplePacketTypes(String name)
		{
			this.name = name;
		}
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (!this.worldObj.isRemote)
		{
			this.prevFunctioning = this.functioning;
			this.functioning = this.isFunctioning();

			if (prevFunctioning != this.functioning)
			{
				this.sendPowerUpdate();
			}
			this.sendGUIPacket();
		}
	}

	/** Can this tile function, or run threw normal processes */
	public boolean canFunction()
	{
		return this.enabled;
	}

	public boolean isFunctioning()
	{
		if (this.worldObj.isRemote)
		{
			return this.functioning;
		}
		else
		{
			return this.canFunction();
		}
	}

	public void doRunningDebug()
	{
		System.out.println("\n  CanRun: " + this.canFunction());
		System.out.println("  RedPower: " + this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord));
		System.out.println("  IsRunning: " + this.functioning);
	}

	/** Called every tick while this tile entity is disabled. */
	protected void whileDisable()
	{

	}

	public ForgeDirection getDirection()
	{
		if (this.rotateByMetaGroup)
		{
			switch (this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) % 4)
			{
				case 0:
					return ForgeDirection.NORTH;
				case 1:
					return ForgeDirection.SOUTH;
				case 2:
					return ForgeDirection.SOUTH;
				default:
					return ForgeDirection.WEST;
			}
		}
		return ForgeDirection.UNKNOWN;
	}

	public void setDirection(ForgeDirection direction)
	{
		if (this.rotateByMetaGroup)
		{
			switch (direction)
			{
				case NORTH:
					this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) / 4, 3);
				case WEST:
					this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) / 4) + 1, 3);
				case SOUTH:
					this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) / 4) + 2, 3);
				default:
					this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) / 4) + 3, 3);
			}
		}
	}

	@Override
	public boolean simplePacket(String id, ByteArrayDataInput dis, Player player)
	{
		try
		{
			if (this.worldObj.isRemote)
			{
				if (id.equalsIgnoreCase(SimplePacketTypes.RUNNING.name))
				{
					this.functioning = dis.readBoolean();
					return true;
				}
				if (id.equalsIgnoreCase(SimplePacketTypes.NBT.name))
				{
					this.readFromNBT(PacketHandler.readNBTTagCompound(dis));
					return true;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/** Sends the tileEntity save data to the client */
	public void sendNBTPacket()
	{
		if (!this.worldObj.isRemote)
		{
			NBTTagCompound tag = new NBTTagCompound();
			this.writeToNBT(tag);
			PacketHandler.sendPacketToClients(PacketHandler.getTilePacket(this.getChannel(), SimplePacketTypes.NBT.name, this, tag), worldObj, new Vector3(this), 64);
		}
	}

	/** Sends a simple true/false am running power update */
	public void sendPowerUpdate()
	{
		if (!this.worldObj.isRemote)
		{
			PacketHandler.instance().sendPacketToClients(PacketHandler.instance().getTilePacket(this.getChannel(), SimplePacketTypes.RUNNING.name, this, this.functioning), worldObj, new Vector3(this), 64);
		}
	}

	/** Sends a gui packet only to the given player */
	public Packet getGUIPacket()
	{
		return null;
	}

	public void sendGUIPacket()
	{
		Packet packet = this.getGUIPacket();
		if (this.hasGUI && this.getContainer() != null && packet != null)
		{
			this.playersUsingMachine = 0;
			for (Object entity : this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1).expand(10, 10, 10)))
			{
				if (entity instanceof EntityPlayer && ((EntityPlayer) entity).openContainer != null)
				{
					if (((EntityPlayer) entity).openContainer.getClass().isAssignableFrom(this.getContainer()))
					{
						this.playersUsingMachine += 1;
						PacketDispatcher.sendPacketToPlayer(packet, (Player) entity);
					}
				}
			}
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketHandler.instance().getTilePacket(this.getChannel(), SimplePacketTypes.RUNNING.name, this, this.functioning);
	}

	@Override
	public boolean hasExtraConfigs()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadExtraConfigs(Configuration config)
	{
		// TODO Auto-generated method stub

	}

}
