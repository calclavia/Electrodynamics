package resonantinduction.core.prefab.tile;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiverWithID;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.TileElectrical;

import com.google.common.io.ByteArrayDataInput;

/**
 * Prefab for general machines
 * 
 * @author Darkguardsman
 */
public class TileMachine extends TileElectrical implements IPacketReceiverWithID
{
	/** Is the machine functioning normally */
	protected boolean functioning = false;
	/** Prev state of function of last update */
	protected boolean prevFunctioning = false;

	protected long joulesPerTick = 0;

	public static final int IS_RUN_PACKET_ID = 0;
	public static final int NBT_PACKET_ID = 1;
	public static final int ENERGY_PACKET_ID = 2;

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
				this.sendRunningPacket();
			}
			if (this.isFunctioning())
			{
				this.consumePower(true);
			}
		}
	}

	public boolean consumePower(boolean doConsume)
	{
		return this.consumePower(this.joulesPerTick, doConsume);
	}

	public boolean consumePower(long joules, boolean doConsume)
	{
		return this.energy.extractEnergy(joules, doConsume) >= joules;
	}

	/** Can this tile function, or run threw normal processes */
	public boolean canFunction()
	{
		return this.consumePower(false);
	}

	/** Called too see if the machine is functioning, server side it redirects to canFunction */
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

	@Override
	public boolean onReceivePacket(int id, ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			if (this.worldObj.isRemote)
			{
				if (id == IS_RUN_PACKET_ID)
				{
					this.functioning = data.readBoolean();
					return true;
				}
				if (id == NBT_PACKET_ID)
				{
					this.readFromNBT(PacketHandler.readNBTTagCompound(data));
					return true;
				}
				if (id == ENERGY_PACKET_ID)
				{
					this.energy.readFromNBT(PacketHandler.readNBTTagCompound(data));
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
			PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacket(this, NBT_PACKET_ID, this, tag), worldObj, new Vector3(this), 64);
		}
	}

	/** Sends a simple true/false am running power update */
	public void sendRunningPacket()
	{
		if (!this.worldObj.isRemote)
		{
			PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacket(this, IS_RUN_PACKET_ID, this, this.functioning), worldObj, new Vector3(this), 64);
		}
	}

	public void sendPowerPacket()
	{
		if (!this.worldObj.isRemote)
		{
			PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacket(this, ENERGY_PACKET_ID, this, this.energy.writeToNBT(new NBTTagCompound())), worldObj, new Vector3(this), 64);
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantInduction.PACKET_TILE.getPacket(this, this.functioning);
	}
}
