package resonantinduction.wire;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.PacketHandler;
import resonantinduction.base.IPacketReceiver;
import universalelectricity.compatibility.TileEntityUniversalConductor;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityWire extends TileEntityUniversalConductor implements IPacketReceiver
{
	public int dyeID = -1;
	public boolean isInsulated = false;

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
		{
			return false;
		}

		Vector3 connectPos = new Vector3(this).modifyPositionFromSide(direction);

		if (connectPos.getTileEntity(this.worldObj) instanceof TileEntityWire)
		{
			TileEntityWire tileWire = (TileEntityWire) connectPos.getTileEntity(this.worldObj);

			if ((tileWire.isInsulated && this.isInsulated && tileWire.dyeID != this.dyeID && this.dyeID != -1) || connectPos.getBlockMetadata(this.worldObj) != this.getTypeID())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void refresh()
	{
		if (!this.worldObj.isRemote)
		{
			this.adjacentConnections = null;

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if (this.canConnect(side.getOpposite()))
				{
					TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.worldObj, new Vector3(this), side);

					if (tileEntity != null)
					{
						if (tileEntity.getClass() == this.getClass() && tileEntity instanceof INetworkProvider)
						{
							this.getNetwork().merge(((INetworkProvider) tileEntity).getNetwork());
						}
					}
				}
			}

			this.getNetwork().refresh();
		}
	}

	@Override
	public float getResistance()
	{
		return getMaterial().resistance;
	}

	@Override
	public float getCurrentCapacity()
	{
		return getMaterial().maxAmps;
	}

	public EnumWireMaterial getMaterial()
	{
		return EnumWireMaterial.values()[this.getTypeID()];
	}

	public int getTypeID()
	{
		return this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
	}

	/**
	 * @param dyeID
	 */
	public void setDye(int dyeID)
	{
		this.dyeID = dyeID;
		this.refresh();
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketHandler.getTileEntityPacket(this, this.dyeID);
	}

	@Override
	public void handle(ByteArrayDataInput input)
	{
		try
		{
			this.dyeID = input.readInt();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.dyeID = nbt.getInteger("dyeID");
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("dyeID", this.dyeID);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		return null;
	}

}
