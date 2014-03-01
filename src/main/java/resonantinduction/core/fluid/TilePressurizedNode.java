package resonantinduction.core.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiverWithID;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.utility.FluidUtility;
import calclavia.lib.utility.WorldUtility;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A prefab class for tiles that use the fluid network.
 * 
 * @author DarkGuardsman
 */
public abstract class TilePressurizedNode extends TileAdvanced implements IPressurizedNode, IPacketReceiverWithID
{
	protected int pressure;

	protected FluidTank tank;
	protected Object[] connectedBlocks = new Object[6];
	protected int colorID = 0;

	/** Copy of the tank's content last time it updated */
	protected FluidStack prevStack = null;

	/** Network used to link all parts together */
	protected PressureNetwork network;

	public static final int PACKET_DESCRIPTION = 0;
	public static final int PACKET_RENDER = 1;
	public static final int PACKET_TANK = 2;

	/** Bitmask that handles connections for the renderer **/
	public byte renderSides = 0;

	public TilePressurizedNode()
	{
		getInternalTank().setCapacity(FluidContainerRegistry.BUCKET_VOLUME);
	}

	@Override
	public void initiate()
	{
		super.initiate();
		refresh();
		getNetwork().reconstruct();
	}

	@Override
	public void invalidate()
	{
		this.getNetwork().split(this);
		super.invalidate();
	}

	@Override
	public void setPressure(int amount)
	{
		pressure = amount;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (!resource.getFluid().isGaseous())
		{
			int fill = getInternalTank().fill(resource, doFill);
			onFluidChanged();
			return fill;
		}

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		FluidStack drain = getInternalTank().drain(resource.amount, doDrain);
		onFluidChanged();
		return drain;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		FluidStack drain = getInternalTank().drain(maxDrain, doDrain);
		onFluidChanged();
		return drain;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { getInternalTank().getInfo() };
	}

	@Override
	public Object[] getConnections()
	{
		return connectedBlocks;
	}

	public void refresh()
	{
		if (this.worldObj != null && !this.worldObj.isRemote)
		{
			byte previousConnections = renderSides;
			connectedBlocks = new Object[6];
			renderSides = 0;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				this.validateConnectionSide(new Vector3(this).translate(dir).getTileEntity(worldObj), dir);
			}

			/** Only send packet updates if visuallyConnected changed. */
			if (previousConnections != renderSides)
			{
				sendRenderUpdate();
				getNetwork().reconstruct();
			}
		}

	}

	/**
	 * Checks to make sure the connection is valid to the tileEntity
	 * 
	 * @param tileEntity - the tileEntity being checked
	 * @param side - side the connection is too
	 */
	public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
	{
		if (!this.worldObj.isRemote)
		{
			if (tileEntity instanceof IFluidDistribution)
			{
				this.getNetwork().merge(((IPressurizedNode) tileEntity).getNetwork());
				renderSides = WorldUtility.setEnableSide(renderSides, side, true);
				connectedBlocks[side.ordinal()] = tileEntity;
			}
		}
	}

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
		nbt.setCompoundTag("FluidTank", this.getInternalTank().writeToNBT(new NBTTagCompound()));
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

	@Override
	public void onFluidChanged()
	{
		if (!worldObj.isRemote)
		{
			if (!FluidUtility.matchExact(prevStack, this.getInternalTank().getFluid()))
			{
				sendTankUpdate();
			}

			prevStack = tank.getFluid();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getAABBPool().getAABB(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
	}

	public int getSubID()
	{
		return this.colorID;
	}

	public void setSubID(int id)
	{
		this.colorID = id;
	}

	@Override
	public boolean canConnect(ForgeDirection direction, Object obj)
	{
		return true;
	}

	@Override
	public FluidTank getInternalTank()
	{
		if (this.tank == null)
		{
			this.tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
		}
		return this.tank;
	}

	@Override
	public IPressurizedNode getInstance(ForgeDirection from)
	{
		return this;
	}

	@Override
	public boolean canFlow()
	{
		return true;
	}

	@Override
	public PressureNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new PressureNetwork();
			this.network.addConnector(this);
		}

		return this.network;
	}

	@Override
	public void setNetwork(PressureNetwork network)
	{
		this.network = network;
	}

}
