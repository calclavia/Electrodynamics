package resonantinduction.mechanical.fluid.prefab;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import resonantinduction.api.IReadOut;
import resonantinduction.api.fluid.IFluidNetwork;
import resonantinduction.api.fluid.IFluidConnector;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.fluid.network.FluidNetwork;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiverWithID;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.utility.FluidUtility;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A prefab class for tiles that use the fluid network.
 * 
 * @author DarkCow
 * 
 */
public abstract class TileFluidNetwork<N extends FluidNetwork> extends TileEntityFluidDevice implements IFluidConnector, IPacketReceiverWithID, IReadOut
{
	public static int refreshRate = 10;
	protected FluidTank tank = new FluidTank(1 * FluidContainerRegistry.BUCKET_VOLUME);
	protected Object[] connectedBlocks = new Object[6];
	protected int colorID = 0;

	/** Copy of the tank's content last time it updated */
	protected FluidStack prevStack = null;

	/** Network used to link all parts together */
	protected N network;

	public static final int PACKET_DESCRIPTION = Mechanical.contentRegistry.getNextPacketID();
	public static final int PACKET_RENDER = Mechanical.contentRegistry.getNextPacketID();
	public static final int PACKET_TANK = Mechanical.contentRegistry.getNextPacketID();

	/** Bitmask that handles connections for the renderer **/
	public byte renderSides = 0b0;

	/** Tells the tank that on next update to check if it should update the client render data */
	public boolean updateFluidRender = false;

	@Override
	public void initiate()
	{
		super.initiate();
		this.refresh();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!worldObj.isRemote)
		{
			if (this.updateFluidRender && ticks % TileFluidNetwork.refreshRate == 0)
			{
				if (!FluidUtility.matchExact(prevStack, this.getInternalTank().getFluid()))
				{
					this.sendTankUpdate();
				}

				this.prevStack = this.tank.getFluid();
				this.updateFluidRender = false;
			}
		}
	}

	@Override
	public void onFluidChanged()
	{
		this.updateFluidRender = true;
	}

	@Override
	public void invalidate()
	{
		this.getNetwork().split(this);
		super.invalidate();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (this.getNetwork() != null && resource != null)
		{
			return this.getNetwork().fill(this, from, resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (this.getNetwork() != null && resource != null)
		{
			return this.getNetwork().drain(this, from, resource, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if (this.getNetwork() != null)
		{
			return this.getNetwork().drain(this, from, maxDrain, doDrain);
		}
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return this.getNetwork().getTankInfo();
	}

	@Override
	public Object[] getConnections()
	{
		return this.connectedBlocks;
	}

	public void refresh()
	{
		if (this.worldObj != null && !this.worldObj.isRemote)
		{
			byte previousConnections = renderSides;
			this.connectedBlocks = new Object[6];
			this.renderSides = 0;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				this.validateConnectionSide(new Vector3(this).modifyPositionFromSide(dir).getTileEntity(this.worldObj), dir);

			}
			/** Only send packet updates if visuallyConnected changed. */
			if (previousConnections != renderSides)
			{
				this.sendRenderUpdate();
				this.getNetwork().reconstruct();
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
			if (tileEntity instanceof IFluidConnector)
			{
				this.getNetwork().merge(((IFluidConnector) tileEntity).getNetwork());
				this.setRenderSide(side, true);
				connectedBlocks[side.ordinal()] = tileEntity;
			}
		}
	}

	public void setRenderSide(ForgeDirection direction, boolean doRender)
	{
		if (doRender)
		{
			renderSides = (byte) (renderSides | (1 << direction.ordinal()));
		}
		else
		{
			renderSides = (byte) (renderSides & ~(1 << direction.ordinal()));

		}
	}

	public boolean canRenderSide(ForgeDirection direction)
	{
		return (renderSides & (1 << direction.ordinal())) != 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.colorID = nbt.getInteger("subID");
		if (nbt.hasKey("stored"))
		{
			NBTTagCompound tag = nbt.getCompoundTag("stored");
			String name = tag.getString("LiquidName");
			int amount = nbt.getInteger("Amount");
			Fluid fluid = FluidRegistry.getFluid(name);
			if (fluid != null)
			{
				FluidStack liquid = new FluidStack(fluid, amount);
				this.getInternalTank().setFluid(liquid);
			}
		}
		else
		{
			this.getInternalTank().readFromNBT(nbt.getCompoundTag("FluidTank"));
		}
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
					this.tank = new FluidTank(data.readInt());
					this.getInternalTank().readFromNBT(PacketHandler.readNBTTagCompound(data));
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
		PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacketWithID(PACKET_RENDER, this, this.colorID, this.renderSides));
	}

	public void sendTankUpdate()
	{
		if (this.getInternalTank() != null)
		{
			PacketHandler.sendPacketToClients(ResonantInduction.PACKET_TILE.getPacketWithID(PACKET_TANK, this, this.getInternalTank().getCapacity(), this.getInternalTank().writeToNBT(new NBTTagCompound())), this.worldObj, new Vector3(this), 60);
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

	public static boolean canRenderSide(byte renderSides, ForgeDirection direction)
	{
		return (renderSides & (1 << direction.ordinal())) != 0;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
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
	public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool)
	{
		if (tool == EnumTools.PIPE_GUAGE)
		{
			return this.getNetwork().toString();
		}
		return null;
	}

}
