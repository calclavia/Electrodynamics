package mffs.tileentity;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import mffs.api.modules.IModule;
import mffs.fortron.FortronHelper;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import universalelectricity.core.electricity.ElectricityPack;

import com.google.common.io.ByteArrayDataInput;

/**
 * A TileEntity that extract forcillium into fortrons.
 * 
 * @author Calclavia
 * 
 */
public class TileEntityCoercionDeriver extends TileEntityElectric
{
	/**
	 * The amount of watts this machine uses.
	 */
	public static final int WATTAGE = 1000;
	public static final int REQUIRED_TIME = 20 * 15;
	public int processTime = 0;

	public TileEntityCoercionDeriver()
	{
		this.fortronTank.setCapacity(10 * LiquidContainerRegistry.BUCKET_VOLUME);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (!this.isDisabled())
			{

				if (this.wattsReceived >= TileEntityCoercionDeriver.WATTAGE)
				{
					if (this.processTime == 0)
					{
						this.processTime = REQUIRED_TIME;
					}

					if (this.processTime > 0)
					{
						// We are processing
						this.processTime--;

						if (this.ticks % 20 == 0)
						{
							int production = 1;

							if (this.isStackValidForSlot(0, this.getStackInSlot(0)))
							{
								production *= 10;
							}

							this.fortronTank.fill(FortronHelper.getFortron(production + this.worldObj.rand.nextInt(production)), true);
						}

						if (this.processTime < 1)
						{
							this.decrStackSize(0, 1);
							this.processTime = 0;
						}
					}
					else
					{
						this.processTime = 0;
					}

					this.wattsReceived -= WATTAGE;
				}
			}
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 5;
	}

	@Override
	public ElectricityPack getRequest()
	{
		if (this.canUse() && !this.isPoweredByRedstone())
		{
			return new ElectricityPack(WATTAGE / this.getVoltage(), this.getVoltage());
		}

		return super.getRequest();
	}

	@Override
	public boolean isActive()
	{
		return !this.isPoweredByRedstone();
	}

	public boolean canUse()
	{
		if (!this.isDisabled())
		{
			return FortronHelper.getAmount(this.fortronTank) < this.fortronTank.getCapacity();
		}

		return false;
	}

	/**
	 * Packet Methods
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List getPacketUpdate()
	{
		List objects = new LinkedList();
		objects.addAll(super.getPacketUpdate());
		objects.add(this.processTime);
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);
		if (packetID == 1)
		{
			this.processTime = dataStream.readInt();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		this.processTime = nbt.getInteger("processTime");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setInteger("processTime", this.processTime);
	}

	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (slotID >= 2 && slotID <= 4)
			{
				return itemStack.getItem() instanceof IModule;
			}

			switch (slotID)
			{
				case 0:
					return itemStack.isItemEqual(new ItemStack(Item.dyePowder, 4));
				case 1:
					return itemStack.getItem() instanceof ItemCardFrequency;
			}
		}

		return false;
	}
}