package resonantinduction.electrical.charger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.IEnergyInterface;
import calclavia.lib.network.IPacketReceiverWithID;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.prefab.tile.TileExternalInventory;
import calclavia.lib.utility.inventory.ExternalInventory;

import com.google.common.io.ByteArrayDataInput;

/** @author Darkguardsman */
public class TileCharger extends TileExternalInventory implements IRotatable, IEnergyInterface, IPacketReceiverWithID
{
	public ChargerMode currentMode = ChargerMode.SINGLE;
	private long lastPacket = 0;

	public static enum ChargerMode
	{
		SINGLE(1), DUAL(2), MULTI(4);
		public final int limit;

		private ChargerMode(int limit)
		{
			this.limit = limit;
		}
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canConnect(ForgeDirection direction, Object obj)
	{
		return obj instanceof IEnergyInterface && direction == this.getDirection().getOpposite();
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		long energyUsed = 0;
		long energyLeft = receive;
		for (int i = 0; i < this.getSizeInventory(); i++)
		{
			long input = CompatibilityModule.chargeItem(this.getStackInSlot(i), energyLeft, true);
			energyUsed += input;
			energyLeft -= input;
			if (energyLeft <= 0)
				break;
		}
		if (energyUsed > 0 && System.currentTimeMillis() - this.lastPacket >= 50)
		{
			this.lastPacket = System.currentTimeMillis();
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		return energyUsed;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		return 0;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return ResonantInduction.PACKET_TILE.getPacketWithID(0, this, nbt);
	}

	@Override
	public boolean onReceivePacket(int id, ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			if (id == 0)
			{
				this.readFromNBT(PacketHandler.readNBTTagCompound(data));
				return true;
			}
			else if (id == 1)
			{

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
	public void readFromNBT(NBTTagCompound nbt)
	{
		this.currentMode = ChargerMode.values()[nbt.getInteger("chargerMode")];
		this.inventory = new ExternalInventory(this, this.currentMode.limit);
		super.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("chargerMode", this.currentMode.ordinal());
	}

}
