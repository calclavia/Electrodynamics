package resonantinduction.core.resource;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import resonantinduction.core.ResonantInduction;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.prefab.tile.TileAdvanced;

import com.google.common.io.ByteArrayDataInput;

/**
 * A tile that stores the material name.
 * 
 * @author Calclavia
 * 
 */
public class TileMaterial extends TileAdvanced implements IPacketReceiver
{
	public String name;
	public int clientColor;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		name = data.readUTF();
		clientColor = ResourceGenerator.materialColors.get(name);
		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (name != null)
		{
			return ResonantInduction.PACKET_TILE.getPacket(this, name);
		}

		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		name = nbt.getString("name");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setString("name", name);
	}
}
