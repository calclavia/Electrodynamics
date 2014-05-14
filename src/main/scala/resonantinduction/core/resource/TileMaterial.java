package resonantinduction.core.resource;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import resonant.lib.content.module.TileBase;
import resonant.lib.network.IPacketReceiver;
import resonantinduction.core.ResonantInduction;

import com.google.common.io.ByteArrayDataInput;

/**
 * A tile that stores the material name.
 *
 * @author Calclavia
 */
public abstract class TileMaterial extends TileBase implements IPacketReceiver
{
	public String name;

	public TileMaterial()
	{
		super(null);
	}

	public TileMaterial(Material material)
	{
		super(material);
	}

	public int getColor()
	{
		return ResourceGenerator.getColor(name);
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		name = data.readUTF();
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
		if (name != null)
		{
			nbt.setString("name", name);
		}
	}
}
