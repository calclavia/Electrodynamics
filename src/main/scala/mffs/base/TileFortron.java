package mffs.base;

import calclavia.api.mffs.card.ICard;
import calclavia.api.mffs.fortron.FrequencyGrid;
import calclavia.api.mffs.fortron.IFortronFrequency;
import com.google.common.io.ByteArrayDataInput;
import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.TransferMode;
import mffs.fortron.FortronHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.*;
import resonant.lib.network.PacketHandler;
import universalelectricity.api.vector.Vector3;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A TileEntity that is powered by FortronHelper.
 *
 * @author Calclavia
 */
public abstract class TileFortron extends TileFrequency implements IFluidHandler, IFortronFrequency
{
	public boolean markSendFortron = true;
	protected FluidTank fortronTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		/**
		 * Packet Update for Client only when GUI is open.
		 */
		if (!worldObj.isRemote && ticks % 60 == 0)
		{
			sendFortronToClients(25);
		}
	}

	@Override
	public void invalidate()
	{
		if (this.markSendFortron)
		{
			// Let remaining Fortron escape.
			MFFSHelper.transferFortron(this, FrequencyGrid.instance().getFortronTiles(this.worldObj, new Vector3(this), 100, this.getFrequency()), TransferMode.DRAIN, Integer.MAX_VALUE);
		}

		super.invalidate();
	}

	/**
	 * Packets
	 */
	@Override
	public ArrayList getPacketData(int packetID)
	{
		if (packetID == TilePacketType.FORTRON.ordinal())
		{
			NBTTagCompound nbt = new NBTTagCompound();

			if (this.fortronTank.getFluid() != null)
			{
				nbt.setTag("fortron", this.fortronTank.getFluid().writeToNBT(new NBTTagCompound()));
			}

			ArrayList list = new ArrayList();
			list.add(TilePacketType.FORTRON.ordinal());
			list.add(nbt);

			return list;
		}

		return super.getPacketData(packetID);
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.FORTRON.ordinal())
		{
			NBTTagCompound nbt = PacketHandler.readNBTTagCompound(dataStream);

			if (nbt != null)
			{
				this.fortronTank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fortron")));
			}
		}
	}

	public void sendFortronToClients(int range)
	{
		PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, this.getPacketData(TilePacketType.FORTRON.ordinal()).toArray()), this.worldObj, new Vector3(this), range);
	}

	/**
	 * NBT Methods
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.fortronTank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fortron")));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (this.fortronTank.getFluid() != null)
		{
			nbt.setTag("fortron", this.fortronTank.getFluid().writeToNBT(new NBTTagCompound()));
		}
	}

	/**
	 * Fluid Functions.
	 */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (resource.isFluidEqual(FortronHelper.FLUIDSTACK_FORTRON))
		{
			return this.fortronTank.fill(resource, doFill);
		}

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource == null || !resource.isFluidEqual(fortronTank.getFluid()))
		{
			return null;
		}
		return fortronTank.drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return fortronTank.drain(maxDrain, doDrain);
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
		return new FluidTankInfo[] { this.fortronTank.getInfo() };
	}

	@Override
	public int getFortronEnergy()
	{
		return FortronHelper.getAmount(this.fortronTank);
	}

	@Override
	public void setFortronEnergy(int joules)
	{
		this.fortronTank.setFluid(FortronHelper.getFortron(joules));
	}

	@Override
	public int getFortronCapacity()
	{
		return this.fortronTank.getCapacity();
	}

	@Override
	public int requestFortron(int joules, boolean doUse)
	{
		return FortronHelper.getAmount(this.fortronTank.drain(joules, doUse));
	}

	@Override
	public int provideFortron(int joules, boolean doUse)
	{
		return this.fortronTank.fill(FortronHelper.getFortron(joules), doUse);
	}

	/**
	 * Gets the card that's in this machine.
	 *
	 * @return
	 */
	public ItemStack getCard()
	{
		ItemStack itemStack = this.getStackInSlot(0);

		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof ICard)
			{
				return itemStack;
			}
		}

		return null;
	}

}
