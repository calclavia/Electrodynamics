package mffs.tileentity;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.modules.IModule;
import mffs.base.TileEntityUniversalEnergy;
import mffs.fortron.FortronHelper;
import mffs.item.card.ItemCardFrequency;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
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
public class TileEntityCoercionDeriver extends TileEntityUniversalEnergy
{
	/**
	 * The amount of watts this machine uses.
	 */
	public static final int WATTAGE = 1000;
	public static final int REQUIRED_TIME = 10 * 20;
	public static final int NORMAL_PRODUCTION = 20;
	public static final float FORTRON_UE_RATIO = WATTAGE / (NORMAL_PRODUCTION + NORMAL_PRODUCTION / 2);

	public static final int SLOT_FREQUENCY = 0;
	public static final int SLOT_BATTERY = 1;
	public static final int SLOT_FUEL = 2;

	public int processTime = 0;
	public boolean isInversed = false;

	public TileEntityCoercionDeriver()
	{
		this.fortronTank.setCapacity(10 * LiquidContainerRegistry.BUCKET_VOLUME);
		this.startModuleIndex = 3;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (!this.isDisabled() && this.isActive())
			{
				if (this.isInversed && Settings.ENABLE_ELECTRICITY)
				{
					// Convert Fortron to Electricity
					double watts = Math.min(this.getFortronEnergy() * FORTRON_UE_RATIO, WATTAGE);

					ElectricityPack remainder = this.produce(watts);
					double electricItemGiven = 0;

					if (remainder.getWatts() > 0)
					{
						electricItemGiven = ElectricItemHelper.chargeItem(this.getStackInSlot(SLOT_BATTERY), remainder.getWatts(), this.getVoltage());
					}

					this.requestFortron((int) ((watts - (remainder.getWatts() - electricItemGiven)) / FORTRON_UE_RATIO), true);

					this.animation++;
				}
				else
				{
					// Convert Electricity to Fortron
					this.wattsReceived += ElectricItemHelper.dechargeItem(this.getStackInSlot(SLOT_BATTERY), WATTAGE, this.getVoltage());

					if (this.wattsReceived >= TileEntityCoercionDeriver.WATTAGE || !Settings.ENABLE_ELECTRICITY)
					{
						if (this.ticks % 20 == 0)
						{
							int production = 3;

							if (this.isStackValidForSlot(SLOT_FUEL, this.getStackInSlot(SLOT_FUEL)))
							{
								production *= NORMAL_PRODUCTION;
							}

							this.fortronTank.fill(FortronHelper.getFortron(production + this.worldObj.rand.nextInt(production)), true);
						}

						if (this.processTime == 0)
						{
							this.decrStackSize(SLOT_FUEL, 1);
							this.processTime = REQUIRED_TIME;

							if (this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed) > 0)
							{
								this.processTime = this.processTime * this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed) / 30;
							}
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

						this.wattsReceived -= WATTAGE;
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
	public int getSizeInventory()
	{
		return 6;
	}

	@Override
	public ElectricityPack getRequest()
	{
		if (this.canConsume() && !this.isInversed)
		{
			return new ElectricityPack(WATTAGE / this.getVoltage(), this.getVoltage());
		}

		return super.getRequest();
	}

	public boolean canConsume()
	{
		if (this.isActive() && !this.isInversed)
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
		objects.add(this.isInversed);
		objects.add(this.wattsReceived);
		// objects.add(this.processTime);
		return objects;
	}

	@Override
	public void onReceivePacket(int packetID, ByteArrayDataInput dataStream) throws IOException
	{
		super.onReceivePacket(packetID, dataStream);

		if (packetID == TilePacketType.DESCRIPTION.ordinal())
		{
			this.isInversed = dataStream.readBoolean();
			this.wattsReceived = dataStream.readDouble();
			// this.processTime = dataStream.readInt();
		}
		else if (packetID == TilePacketType.TOGGLE_MODE.ordinal())
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
	public boolean isStackValidForSlot(int slotID, ItemStack itemStack)
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
					return itemStack.isItemEqual(new ItemStack(Item.dyePowder, 1, 4));
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