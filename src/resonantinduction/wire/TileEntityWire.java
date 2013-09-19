package resonantinduction.wire;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.PacketHandler;
import resonantinduction.base.IPacketReceiver;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.compatibility.TileEntityUniversalConductor;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.PowerHandler;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityWire extends TileEntityUniversalConductor implements IPacketReceiver, IInsulatedMaterial
{
	public static final int DEFAULT_COLOR = 16;
	public int dyeID = DEFAULT_COLOR;
	public boolean isInsulated = false;

	/** Client Side Connection Check */
	public boolean isTick = false;

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
		{
			return false;
		}

		Vector3 connectPos = new Vector3(this).modifyPositionFromSide(direction);
		TileEntity connectTile = connectPos.getTileEntity(this.worldObj);
		if (connectTile instanceof IWireMaterial)
		{
			IWireMaterial wireTile = (IWireMaterial) connectTile;
			
			if (wireTile.getMaterial() != this.getMaterial())
			{
				return false;
			}
		}

		if (this.isInsulated() && connectTile instanceof IInsulation)
		{
			IInsulation insulatedTile = (IInsulation) connectTile;

			if ((insulatedTile.isInsulated() && insulatedTile.getInsulationColor() != this.getInsulationColor() && this.getInsulationColor() != DEFAULT_COLOR && insulatedTile.getInsulationColor() != DEFAULT_COLOR))
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
						if (/*tileEntity.getClass().isInstance(this) && */tileEntity instanceof INetworkProvider)
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

	public void setInsulated()
	{
		this.isInsulated = true;
		this.refresh();
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketHandler.getTileEntityPacket(this, this.isInsulated, this.dyeID, this instanceof TileEntityTickWire);
	}

	@Override
	public void handle(ByteArrayDataInput input)
	{
		try
		{
			this.isInsulated = input.readBoolean();
			this.dyeID = input.readInt();
			this.isTick = input.readBoolean();
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
		this.isInsulated = nbt.getBoolean("isInsulated");
	}

	/**
	 * Furnace connection for tick wires
	 */
	@Override
	public TileEntity[] getAdjacentConnections()
	{
		super.getAdjacentConnections();

		if (this.isTick)
		{
			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), side);

				if (tileEntity instanceof TileEntityFurnace)
				{
					this.adjacentConnections[i] = tileEntity;
				}
			}
		}
		return this.adjacentConnections;
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("dyeID", this.dyeID);
		nbt.setBoolean("isInsulated", this.isInsulated);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		return null;
	}

	@Override
	public void doWork(PowerHandler workProvider)
	{
		this.buildcraftBuffer = Compatibility.BC3_RATIO * 25 * this.getMaterial().maxAmps;
		this.powerHandler.configure(0, this.buildcraftBuffer, this.buildcraftBuffer, this.buildcraftBuffer * 2);
		super.doWork(workProvider);
	}

	@Override
	public boolean isInsulated()
	{
		return isInsulated;
	}

	@Override
	public void setInsulated(boolean insulated)
	{
		if (insulated && !isInsulated())
			setInsulated();
		
	}

	@Override
	public int getInsulationColor()
	{
		return dyeID;
	}

	@Override
	public void setInsulationColor(int dyeID)
	{
		setDye(dyeID);		
	}
}
