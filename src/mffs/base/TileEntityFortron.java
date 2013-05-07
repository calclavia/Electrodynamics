package mffs.base;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import mffs.MFFSHelper;
import mffs.TransferMode;
import mffs.api.card.ICard;
import mffs.api.fortron.IFortronFrequency;
import mffs.fortron.FortronHelper;
import mffs.fortron.FrequencyGrid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

/**
 * A TileEntity that is powered by FortronHelper.
 * 
 * @author Calclavia
 * 
 */
public abstract class TileEntityFortron extends TileEntityFrequency implements ITankContainer, IFortronFrequency
{
	protected LiquidTank fortronTank = new LiquidTank(FortronHelper.LIQUID_FORTRON.copy(), LiquidContainerRegistry.BUCKET_VOLUME, this);

	@Override
	public void invalidate()
	{
		// Let remaining Fortron escape.
		MFFSHelper.transferFortron(this, FrequencyGrid.instance().getFortronTiles(this.worldObj, new Vector3(this), 100, this.getFrequency()), TransferMode.DRAIN, Integer.MAX_VALUE);
		super.invalidate();
	}

	/**
	 * Packet Methods
	 */
	@Override
	public List getPacketUpdate()
	{
		List objects = new LinkedList();
		objects.addAll(super.getPacketUpdate());
		objects.add(FortronHelper.getAmount(this.fortronTank.getLiquid()));
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.DESCRIPTION.ordinal())
		{
			this.fortronTank.setLiquid(FortronHelper.getFortron(dataStream.readInt()));
		}
	}

	/**
	 * NBT Methods
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.fortronTank.setLiquid(LiquidStack.loadLiquidStackFromNBT(nbt.getCompoundTag("fortron")));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (this.fortronTank.getLiquid() != null)
		{
			NBTTagCompound fortronCompound = new NBTTagCompound();
			this.fortronTank.getLiquid().writeToNBT(fortronCompound);
			nbt.setTag("fortron", fortronCompound);
		}

	}

	/**
	 * Liquid Functions.
	 */
	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
	{
		if (resource.isLiquidEqual(FortronHelper.LIQUID_FORTRON))
		{
			return this.fortronTank.fill(resource, doFill);
		}

		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill)
	{
		return this.fill(ForgeDirection.getOrientation(tankIndex), resource, doFill);
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return this.fortronTank.drain(maxDrain, doDrain);
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
		return this.drain(ForgeDirection.getOrientation(tankIndex), maxDrain, doDrain);
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction)
	{
		return new ILiquidTank[] { this.fortronTank };
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		if (type.isLiquidEqual(FortronHelper.LIQUID_FORTRON))
		{
			return this.fortronTank;
		}

		return null;
	}

	@Override
	public void setFortronEnergy(int joules)
	{
		this.fortronTank.setLiquid(FortronHelper.getFortron(joules));
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
	 * Gets the frequency card that's in this machine.
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
