package mffs.base;

import resonant.api.mffs.IBiometricIdentifierLink;
import resonant.api.mffs.card.ICoordLink;
import resonant.api.mffs.fortron.FrequencyGrid;
import resonant.api.mffs.security.IBiometricIdentifier;
import com.google.common.io.ByteArrayDataInput;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import resonant.api.blocks.IBlockFrequency;
import universalelectricity.core.transform.vector.Vector3;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class TileFrequency extends TileMFFSInventory implements IBlockFrequency, IBiometricIdentifierLink
{
	private int frequency;

	@Override
	public void initiate()
	{
		FrequencyGrid.instance().register(this);
		super.initiate();
	}

	@Override
	public void invalidate()
	{
		FrequencyGrid.instance().unregister(this);
		super.invalidate();
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.FREQUENCY.ordinal())
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
	@Override
	public IBiometricIdentifier getBiometricIdentifier()
	{
		/**
		 * Try to find in the cards first.
		 */
		if (this.getBiometricIdentifiers().size() > 0)
		{
			return (IBiometricIdentifier) this.getBiometricIdentifiers().toArray()[0];
		}

		return null;
	}

	@Override
	public Set<IBiometricIdentifier> getBiometricIdentifiers()
	{
		Set<IBiometricIdentifier> list = new HashSet<IBiometricIdentifier>();

		/**
		 * Try to find in the cards first.
		 */
		for (ItemStack itemStack : this.getCards())
		{
			if (itemStack != null && itemStack.getItem() instanceof ICoordLink)
			{
				Vector3 linkedPosition = ((ICoordLink) itemStack.getItem()).getLink(itemStack);

				if (linkedPosition != null)
				{
					TileEntity tileEntity = linkedPosition.getTileEntity(this.worldObj);

					if (linkedPosition != null && tileEntity instanceof IBiometricIdentifier)
					{
						list.add((IBiometricIdentifier) tileEntity);
					}
				}
			}
		}

		for (IBlockFrequency tileEntity : FrequencyGrid.instance().get(this.getFrequency()))
		{
			if (tileEntity instanceof IBiometricIdentifier)
			{
				list.add((IBiometricIdentifier) tileEntity);
			}
		}

		return list;
	}
}
