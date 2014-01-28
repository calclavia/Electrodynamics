package resonantinduction.core.resource;

import resonantinduction.core.ResonantInduction;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.prefab.tile.TileAdvanced;

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
		clientColor = data.readInt();
		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (name != null)
		{
			return ResonantInduction.PACKET_TILE.getPacket(this, ResourceGenerator.materialColors.get(name));
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
