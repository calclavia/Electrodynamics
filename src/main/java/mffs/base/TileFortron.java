package mffs.base;

import mffs.MFFSHelper;
import mffs.Settings;
import mffs.TransferMode;
import mffs.api.ISpecialForceManipulation;
import mffs.api.card.ICard;
import mffs.api.fortron.FrequencyGrid;
import mffs.api.fortron.IFortronFrequency;
import mffs.fortron.FortronHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.PacketHandler;

/**
 * A TileEntity that is powered by FortronHelper.
 * 
 * @author Calclavia
 * 
 */
public abstract class TileFortron extends TileFrequency implements IFluidHandler, IFortronFrequency, ISpecialForceManipulation
{
	protected FluidTank fortronTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	private boolean markSendFortron = true;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		/**
		 * Packet Update for Client only when GUI is open.
		 */
		if (!Settings.CONSERVE_PACKETS && this.ticks % 60 == 0)
		{
			PacketHandler.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 30);
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

	@Override
	public boolean preMove(int x, int y, int z)
	{
		return true;
	}

	@Override
	public void move(int x, int y, int z)
	{
		this.markSendFortron = false;
	}

	@Override
	public void postMove()
	{

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
	public void setFortronEnergy(int joules)
	{
		this.fortronTank.setFluid(FortronHelper.getFortron(joules));
	}

	@Override
	public int getFortronEnergy()
	{
		return FortronHelper.getAmount(this.fortronTank);
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
