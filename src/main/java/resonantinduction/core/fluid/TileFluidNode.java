package resonantinduction.core.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiverWithID;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.utility.FluidUtility;

import com.google.common.io.ByteArrayDataInput;

/**
 * A prefab class for tiles that use the fluid network.
 * 
 * @author DarkGuardsman
 */
public abstract class TileFluidNode extends TileAdvanced implements IPacketReceiverWithID
{
	protected int pressure;

	protected FluidTank tank;

	protected int colorID = 0;

	/** Copy of the tank's content last time it updated */
	protected FluidStack prevStack = null;

	public static final int PACKET_DESCRIPTION = 0;
	public static final int PACKET_RENDER = 1;
	public static final int PACKET_TANK = 2;

	/** Bitmask that handles connections for the renderer **/
	public byte renderSides = 0;

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.colorID = nbt.getInteger("subID");
		getInternalTank().readFromNBT(nbt.getCompoundTag("FluidTank"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("subID", this.colorID);
		nbt.setCompoundTag("FluidTank", getInternalTank().writeToNBT(new NBTTagCompound()));
	}

	@Override
	public boolean onReceivePacket(int id, ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			if (this.worldObj.isRemote)
			{
				if (id == PACKET_DESCRIPTION)
				{
					this.colorID = data.readInt();
					this.renderSides = data.readByte();
					this.tank = new FluidTank(data.readInt());
					this.getInternalTank().readFromNBT(PacketHandler.readNBTTagCompound(data));
					return true;
				}
				else if (id == PACKET_RENDER)
				{
					this.colorID = data.readInt();
					this.renderSides = data.readByte();
					return true;
				}
				else if (id == PACKET_TANK)
				{
					tank = new FluidTank(data.readInt()).readFromNBT(PacketHandler.readNBTTagCompound(data));
					pressure = data.readInt();
					return true;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return true;
		}
		return false;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantInduction.PACKET_TILE.getPacketWithID(PACKET_DESCRIPTION, this, this.colorID, this.renderSides, this.getInternalTank().getCapacity(), this.getInternalTank().writeToNBT(new NBTTagCompound()));
	}

	public void sendRenderUpdate()
	{
		if (!this.worldObj.isRemote)
			PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacketWithID(PACKET_RENDER, this, this.colorID, this.renderSides));
	}

	public void sendTankUpdate()
	{
		if (!this.worldObj.isRemote)
			PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacketWithID(PACKET_TANK, this, getInternalTank().getCapacity(), getInternalTank().writeToNBT(new NBTTagCompound()), pressure), this.worldObj, new Vector3(this), 60);
	}

	public void onFluidChanged()
	{
		if (!worldObj.isRemote)
		{
			if (!FluidUtility.matchExact(prevStack, getInternalTank().getFluid()))
			{
				sendTankUpdate();
				prevStack = tank.getFluid() != null ? tank.getFluid().copy() : null;
			}
		}
	}

	public FluidTank getInternalTank()
	{
		if (this.tank == null)
		{
			this.tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
		}
		return this.tank;
	}
}
