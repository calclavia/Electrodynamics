package mffs.tile;

import resonant.api.mffs.modules.IModule;
import com.google.common.io.ByteArrayDataInput;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.base.TileMFFSElectrical;
import mffs.FortronHelper;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.EnergyStorageHandler;

import java.io.IOException;
import java.util.EnumSet;

/**
 * A TileEntity that extract energy into Fortron.
 *
 * @author Calclavia
 */
public class TileCoercionDeriver extends TileMFFSElectrical
{
	public static final int FUEL_PROCESS_TIME = 10 * 20;
	public static final int MULTIPLE_PRODUCTION = 4;
	/**
	 * Ration from UE to Fortron. Multiply J by this value to convert to Fortron.
	 */
	public static final float UE_FORTRON_RATIO = 0.005f;
	public static final int ENERGY_LOSS = 1;
	public static final int SLOT_FREQUENCY = 0;
	public static final int SLOT_BATTERY = 1;

	public static final int SLOT_FUEL = 2;
	/**
	 * The amount of KiloWatts this machine uses.
	 */
	private static final int DEFAULT_WATTAGE = 5000000;
	public int processTime = 0;
	public boolean isInversed = false;

	public TileCoercionDeriver()
	{
		super();
		this.capacityBase = 30;
		this.startModuleIndex = 3;
		this.energy = new EnergyStorageHandler();
		updateEnergyInfo();
	}

	private void updateEnergyInfo()
	{
		this.energy.setCapacity(getWattage());
		this.energy.setMaxTransfer(getWattage() / 20);
	}

	@Override
	public void initiate()
	{
		super.initiate();
		updateEnergyInfo();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!worldObj.isRemote)
		{
			if (isActive())
			{
				if (isInversed && Settings.ENABLE_ELECTRICITY)
				{
					if (energy.getEnergy() < energy.getEnergyCapacity())
					{
						long withdrawnElectricity = (long) (requestFortron(getProductionRate() / 20, true) / UE_FORTRON_RATIO);
						// Inject electricity from Fortron.
						energy.receiveEnergy(withdrawnElectricity * ENERGY_LOSS, true);
					}

					recharge(getStackInSlot(SLOT_BATTERY));
					produce();
				}
				else
				{
					if (this.getFortronEnergy() < this.getFortronCapacity())
					{
						// Convert Electricity to Fortron
						this.discharge(this.getStackInSlot(SLOT_BATTERY));

						if (this.energy.checkExtract() || (!Settings.ENABLE_ELECTRICITY && this.isItemValidForSlot(SLOT_FUEL, this.getStackInSlot(SLOT_FUEL))))
						{
							// Fill Fortron
							this.fortronTank.fill(FortronHelper.getFortron(this.getProductionRate()), true);
							this.energy.extractEnergy();

							// Use fuel
							if (this.processTime == 0 && this.isItemValidForSlot(SLOT_FUEL, this.getStackInSlot(SLOT_FUEL)))
							{
								this.decrStackSize(SLOT_FUEL, 1);
								this.processTime = FUEL_PROCESS_TIME * Math.max(this.getModuleCount(ModularForceFieldSystem.itemModuleScale) / 20, 1);
							}

							if (this.processTime > 0)
							{
								// We are processing.
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
						}
					}
				}
			}
		}
		else if (this.isActive())
		{
			this.animation++;
		}
	}

	public long getWattage()
	{
		return (long) (DEFAULT_WATTAGE + (DEFAULT_WATTAGE * ((float) this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed) / (float) 8)));
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		if (!isInversed)
		{
			return super.onReceiveEnergy(from, receive, doReceive);
		}
		return receive;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		if (isInversed)
		{
			return super.onExtractEnergy(from, extract, doExtract);
		}
		return 0;
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		updateEnergyInfo();
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.allOf(ForgeDirection.class);
	}

	/**
	 * @return The Fortron production rate per tick!
	 */
	public int getProductionRate()
	{
		if (this.isActive())
		{
			int production = (int) ((float) getWattage() / 20f * UE_FORTRON_RATIO * Settings.FORTRON_PRODUCTION_MULTIPLIER);

			if (this.processTime > 0)
			{
				production *= MULTIPLE_PRODUCTION;
			}

			return production;
		}

		return 0;
	}

	@Override
	public int getSizeInventory()
	{
		return 6;
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
					return CompatibilityModule.isHandler(itemStack.getItem());
				case SLOT_FUEL:
					return itemStack.isItemEqual(new ItemStack(Item.dyePowder, 1, 4)) || itemStack.isItemEqual(new ItemStack(Item.netherQuartz));
			}
		}

		return false;
	}

	@Override
	public boolean canConnect(ForgeDirection direction, Object obj)
	{
		return true;
	}

}