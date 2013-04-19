package mffs.base;

import icbm.api.IBlockFrequency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mffs.api.card.ICardLink;
import mffs.api.fortron.IFortronFrequency;
import mffs.api.security.IBiometricIdentifier;
import mffs.fortron.FortronGrid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileEntityFrequency extends TileEntityInventory implements IBlockFrequency
{
	private int frequency;

	@Override
	public List getPacketUpdate()
	{
		List objects = new LinkedList();
		objects.addAll(super.getPacketUpdate());
		objects.add(this.getFrequency());
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.DESCRIPTION.ordinal())
		{
			this.setFrequency(dataStream.readInt());
		}
		else if (packetID == TilePacketType.FREQUENCY.ordinal())
		{
			this.setFrequency(dataStream.readInt());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.setFrequency(nbt.getInteger("frequency"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("frequency", this.getFrequency());
	}

	@Override
	public int getFrequency()
	{
		return this.frequency;
	}

	@Override
	public void setFrequency(int frequency)
	{
		this.frequency = frequency;
	}

	/**
	 * Gets the first linked security station, based on the card slots and frequency.
	 * 
	 * @return
	 */
	public IBiometricIdentifier getBiometricIdentifier()
	{
		/**
		 * Try to find in the cards first.
		 */
		if (this.getSecurityCenters().size() > 0)
		{
			return this.getSecurityCenters().get(0);
		}

		return null;
	}

	public List<IBiometricIdentifier> getSecurityCenters()
	{
		List<IBiometricIdentifier> securityCenters = new ArrayList<IBiometricIdentifier>();

		/**
		 * Try to find in the cards first.
		 */
		for (ItemStack itemStack : this.getCards())
		{
			if (itemStack != null && itemStack.getItem() instanceof ICardLink)
			{
				Vector3 linkedPosition = ((ICardLink) itemStack.getItem()).getLink(itemStack);

				TileEntity tileEntity = linkedPosition.getTileEntity(this.worldObj);

				if (linkedPosition != null && tileEntity instanceof IBiometricIdentifier)
				{
					if (!securityCenters.contains(tileEntity))
					{
						securityCenters.add((IBiometricIdentifier) tileEntity);
					}
				}
			}
		}

		for (IFortronFrequency tileEntity : FortronGrid.instance().get(this.getFrequency()))
		{
			if (tileEntity instanceof IBiometricIdentifier)
			{
				if (!securityCenters.contains(tileEntity))
				{
					securityCenters.add((IBiometricIdentifier) tileEntity);
				}
			}
		}

		return securityCenters;
	}
}
