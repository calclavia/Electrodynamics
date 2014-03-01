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
public abstract class TilePressurizedNode extends TileFluidNode implements IPressurizedNode, IPacketReceiverWithID
{
	protected Object[] connectedBlocks = new Object[6];

	/** Network used to link all parts together */
	protected PressureNetwork network;

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
		int fill = getInternalTank().fill(resource, doFill);
		onFluidChanged();
		return fill;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return drain(from, resource.amount, doDrain);
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
		if (!this.worldObj.isRemote)
		{
			byte previousConnections = renderSides;
			connectedBlocks = new Object[6];
			renderSides = 0;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				validateConnectionSide(new Vector3(this).translate(dir).getTileEntity(worldObj), dir);
			}

			/** Only send packet updates if visuallyConnected changed. */
			if (previousConnections != renderSides)
			{
				sendRenderUpdate();
			}

			getNetwork().reconstruct();
		}

	}

	/**
	 * Checks to make sure the connection is valid to the tileEntity
	 * 
	 * @param tileEntity - the tileEntity being checked
	 * @param side - side the connection is too
	 */
	public abstract void validateConnectionSide(TileEntity tileEntity, ForgeDirection side);

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
