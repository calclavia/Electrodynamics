package mffs.tileentity;

import mffs.ModularForceFieldSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.tile.TileEntityAdvanced;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityForceField extends TileEntityAdvanced implements IPacketReceiver
{
	private Vector3 projector = null;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (this.getProjector() != null)
		{
			return PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, this, this.projector.intX(), this.projector.intY(), this.projector.intZ());
		}

		return null;
	}

	@Override
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try
		{
			this.setZhuYao(new Vector3(dataStream.readInt(), dataStream.readInt(), dataStream.readInt()));
			this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setZhuYao(Vector3 position)
	{
		this.projector = position;
	}

	/**
	 * @return Gets the projector block controlling this force field. Removes the force field if no
	 * projector can be found.
	 */
	public TileEntityForceFieldProjector getProjector()
	{
		if (this.getProjectorSafe() != null)
		{
			return getProjectorSafe();
		}

		if (!this.worldObj.isRemote)
		{
			this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, 0);
		}

		return null;
	}

	public TileEntityForceFieldProjector getProjectorSafe()
	{
		if (this.projector != null)
		{
			if (this.projector.getTileEntity(this.worldObj) instanceof TileEntityForceFieldProjector)
			{
				if (this.worldObj.isRemote || ((TileEntityForceFieldProjector) this.projector.getTileEntity(this.worldObj)).getCalculatedField().contains(new Vector3(this)))
				{
					return (TileEntityForceFieldProjector) this.projector.getTileEntity(this.worldObj);
				}
			}
		}

		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.projector = Vector3.readFromNBT(nbt.getCompoundTag("projector"));

	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (this.getProjector() != null)
		{
			nbt.setCompoundTag("projector", this.projector.writeToNBT(new NBTTagCompound()));
		}
	}
}