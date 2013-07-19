package mffs.tileentity;

import java.io.IOException;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.modules.IModule;
import mffs.base.TileEntityMFFSUniversal;
import mffs.fortron.FortronHelper;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

/**
 * A TileEntity that extract forcillium into fortrons.
 * 
 * @author Calclavia
 * 
 */
public class TileEntityCoercionDeriver extends TileEntityMFFSUniversal
{
	/**
	 * The amount of watts this machine uses.
	 */
	public static final int WATTAGE = 1000;
	public static final int REQUIRED_TIME = 10 * 20;
	private static final int INITIAL_PRODUCTION = 40;
	public static final int MULTIPLE_PRODUCTION = 4;
	public static final float FORTRON_UE_RATIO = WATTAGE / (INITIAL_PRODUCTION * MULTIPLE_PRODUCTION);

	public static final int SLOT_FREQUENCY = 0;
	public static final int SLOT_BATTERY = 1;
	public static final int SLOT_FUEL = 2;

	public int processTime = 0;
	public boolean isInversed = false;

	public TileEntityCoercionDeriver()
	{
		super(WATTAGE);
		this.capacityBase = 30;
		this.startModuleIndex = 3;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.isActive())
			{
				if (this.isInversed && Settings.ENABLE_ELECTRICITY)
				{
					float provide = this.getProvide(ForgeDirection.UNKNOWN);
					ElectricItemHelper.chargeItem(this.getStackInSlot(SLOT_BATTERY), this.requestFortron((int) (provide / FORTRON_UE_RATIO), true) * FORTRON_UE_RATIO);
					// Convert Fortron to Electricity
					this.produce();
				}
				else
				{
					// Convert Electricity to Fortron
					this.discharge(this.getStackInSlot(SLOT_BATTERY));

					if (this.provideElectricity(ForgeDirection.UNKNOWN, ElectricityPack.getFromWatts(WATTAGE, this.getVoltage()), false).getWatts() >= WATTAGE || (!Settings.ENABLE_ELECTRICITY && this.isItemValidForSlot(SLOT_FUEL, this.getStackInSlot(SLOT_FUEL))))
					{
						// Fill Fortron
						int production = getProductionRate();

						this.fortronTank.fill(FortronHelper.getFortron(production + this.worldObj.rand.nextInt(production)), true);

						// Use fuel
						if (this.processTime == 0 && this.isItemValidForSlot(SLOT_FUEL, this.getStackInSlot(SLOT_FUEL)))
						{
							this.decrStackSize(SLOT_FUEL, 1);
							this.processTime = REQUIRED_TIME * Math.max(this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed) / 20, 1);
						}

						if (this.processTime > 0)
						{
							// We are processing
							this.processTime--;

							if (this.processTime < 1)
							{
								this.processTime = 0;
							}
						}
						else
						{
							this.processTime = 0;
						}

						this.provideElectricity(ForgeDirection.UNKNOWN, ElectricityPack.getFromWatts(WATTAGE, this.getVoltage()), true);
					}

				}
			}
		}
		else if (this.isActive())
		{
			this.animation++;
		}
	}

	@Override
	public float getProvide(ForgeDirection direction)
	{
		if (this.isInversed)
		{
			return Math.min(this.getFortronEnergy() * FORTRON_UE_RATIO, WATTAGE);
		}

		return 0;
	}

	/**
	 * @return Rate is per tick!
	 */
	public int getProductionRate()
	{
		if (this.isActive())
		{
			if (!this.isInversed)
			{
				int production = INITIAL_PRODUCTION;

				if (this.processTime > 0)
				{
					production *= MULTIPLE_PRODUCTION;
				}

				return production;
			}
		}

		return 0;
	}

	@Override
	public int getSizeInventory()
	{
		return 6;
	}

	@Override
	public float getRequest(ForgeDirection direction)
	{
		if (this.canConsume())
		{
			return WATTAGE;
		}

		return 0;
	}

	public boolean canConsume()
	{
		if (this.isActive() && !this.isInversed)
		{
			return FortronHelper.getAmount(this.fortronTank) < this.fortronTank.getCapacity();
		}

		return false;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.TOGGLE_MODE.ordinal())
		{
			this.isInversed = !this.isInversed;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.processTime = nbt.getInteger("processTime");
		this.isInversed = nbt.getBoolean("isInversed");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("processTime", this.processTime);
		nbt.setBoolean("isInversed", this.isInversed);
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (slotID >= this.startModuleIndex)
			{
				return itemStack.getItem() instanceof IModule;
			}

			switch (slotID)
			{
				case SLOT_FREQUENCY:
					return itemStack.getItem() instanceof ItemCardFrequency;
				case SLOT_BATTERY:
					return itemStack.getItem() instanceof IItemElectric;
				case SLOT_FUEL:
					return itemStack.isItemEqual(new ItemStack(Item.dyePowder, 1, 4)) || itemStack.isItemEqual(new ItemStack(Item.netherQuartz));
			}
		}

		return false;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

}