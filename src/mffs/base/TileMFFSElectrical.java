package mffs.base;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.energy.IEnergyContainer;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.vector.Vector3;

/**
 * @author Calclavia
 * 
 */
@UniversalClass
public abstract class TileMFFSElectrical extends TileEntityModuleAcceptor implements IEnergyInterface, IEnergyContainer
{
	protected EnergyStorageHandler energy;

	/**
	 * Recharges electric item.
	 */
	public void recharge(ItemStack itemStack)
	{
		this.energy.extractEnergy(CompatibilityModule.chargeItem(itemStack, this.energy.getEmptySpace(), true), true);
	}

	/**
	 * Discharges electric item.
	 */
	public void discharge(ItemStack itemStack)
	{
		this.energy.receiveEnergy(CompatibilityModule.dischargeItem(itemStack, this.energy.getEnergy(), true), true);
	}

	/**
	 * The electrical input direction.
	 * 
	 * @return The direction that electricity is entered into the tile. Return null for no input. By
	 * default you can accept power from all sides.
	 */
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return EnumSet.allOf(ForgeDirection.class);
	}

	/**
	 * The electrical output direction.
	 * 
	 * @return The direction that electricity is output from the tile. Return null for no output. By
	 * default it will return an empty EnumSet.
	 */
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (direction == null || direction.equals(ForgeDirection.UNKNOWN))
		{
			return false;
		}

		return this.getInputDirections().contains(direction) || this.getOutputDirections().contains(direction);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.energy.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		this.energy.writeToNBT(nbt);
	}

	@Override
	public long getEnergy(ForgeDirection from)
	{
		return this.energy.getEnergy();
	}

	@Override
	public long getEnergyCapacity(ForgeDirection from)
	{
		return this.energy.getEnergyCapacity();
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		return this.energy.receiveEnergy(receive, doReceive);
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		return this.energy.extractEnergy(extract, doExtract);
	}

	@Override
	public void setEnergy(ForgeDirection from, long energy)
	{
		this.energy.setEnergy(energy);
	}

	protected void produce()
	{
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			if (this.energy.getEnergy() > 0)
			{
				TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(direction).getTileEntity(this.worldObj);

				if (tileEntity != null)
				{
					long used = CompatibilityModule.receiveEnergy(tileEntity, direction.getOpposite(), this.energy.extractEnergy(this.energy.getEnergy(), false), true);
					this.energy.extractEnergy(used, true);
				}
			}
		}
	}
}
