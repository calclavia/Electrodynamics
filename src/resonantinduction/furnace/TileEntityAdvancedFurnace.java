package resonantinduction.furnace;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.electricity.IVoltage;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.tile.EnergyStorage;

/**
 * Meant to replace the furnace class.
 * 
 * @author Calclavia
 * 
 */
public class TileEntityAdvancedFurnace extends TileEntityFurnace implements IEnergyInterface, IVoltage
{
	private static final float WATTAGE = 5;

	private EnergyStorage energy = new EnergyStorage(ResonantInduction.FURNACE_WATTAGE * 5);

	@Override
	public void updateEntity()
	{
		boolean canSmelt = canSmelt() && this.getStackInSlot(1) == null && this.furnaceBurnTime == 0;
		boolean canBurn = this.getStackInSlot(0) == null && TileEntityFurnace.getItemBurnTime(this.getStackInSlot(1)) > 0;

		if (canSmelt)
		{
			if (this.energy.checkExtract(ResonantInduction.FURNACE_WATTAGE / 20))
			{
				this.furnaceCookTime++;

				if (this.furnaceCookTime == 200)
				{
					this.furnaceCookTime = 0;
					this.smeltItem();
					this.onInventoryChanged();
				}

				this.energy.extractEnergy(ResonantInduction.FURNACE_WATTAGE / 20, true);
			}
		}
		else if (!this.energy.isFull())
		{
			boolean doBlockStateUpdate = this.furnaceBurnTime > 0;

			if (this.furnaceBurnTime == 0)
			{
				int burnTime = TileEntityFurnace.getItemBurnTime(this.getStackInSlot(1));
				this.decrStackSize(1, 1);
				this.furnaceBurnTime = burnTime;
			}

			if (this.furnaceBurnTime > 0)
			{
				this.energy.receiveEnergy(ResonantInduction.FURNACE_WATTAGE / 20, true);

				this.produce();
			}

			if (doBlockStateUpdate != this.furnaceBurnTime > 0)
			{
				// TODO: Send descript packet.
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			}
		}
		else
		{
			boolean flag = this.furnaceBurnTime > 0;
			boolean flag1 = false;

			if (this.furnaceBurnTime > 0)
			{
				--this.furnaceBurnTime;
			}

			if (!this.worldObj.isRemote)
			{
				if (this.furnaceBurnTime == 0 && this.canSmelt())
				{
					this.currentItemBurnTime = this.furnaceBurnTime = getItemBurnTime(this.getStackInSlot(1));

					if (this.furnaceBurnTime > 0)
					{
						flag1 = true;

						if (this.getStackInSlot(1) != null)
						{
							--this.getStackInSlot(1).stackSize;

							if (this.getStackInSlot(1).stackSize == 0)
							{
								this.setInventorySlotContents(1, this.getStackInSlot(1).getItem().getContainerItemStack(this.getStackInSlot(1)));
							}
						}
					}
				}

				if (this.isBurning() && this.canSmelt())
				{
					++this.furnaceCookTime;

					if (this.furnaceCookTime == 200)
					{
						this.furnaceCookTime = 0;
						this.smeltItem();
						flag1 = true;
					}
				}
				else
				{
					this.furnaceCookTime = 0;
				}

				if (flag != this.furnaceBurnTime > 0)
				{
					flag1 = true;
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				}
			}

			if (flag1)
			{
				this.onInventoryChanged();
			}
		}
	}

	private void produce()
	{
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			if (this.energy.getEnergy() > 0)
			{
				TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(direction).getTileEntity(this.worldObj);
				this.energy.extractEnergy(CompatibilityModule.receiveEnergy(tileEntity, direction.getOpposite(), this.energy.extractEnergy(this.energy.getEnergy(), false), true), true);
			}
		}
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	private boolean canSmelt()
	{
		if (this.getStackInSlot(0) == null)
		{
			return false;
		}
		else
		{
			ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.getStackInSlot(0));
			if (itemstack == null)
				return false;
			if (this.getStackInSlot(2) == null)
				return true;
			if (!this.getStackInSlot(2).isItemEqual(itemstack))
				return false;
			int result = getStackInSlot(2).stackSize + itemstack.stackSize;
			return (result <= getInventoryStackLimit() && result <= itemstack.getMaxStackSize());
		}
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		if (this.canSmelt() && this.getStackInSlot(1) == null && this.furnaceBurnTime == 0)
		{
			return this.energy.receiveEnergy(receive, doReceive);
		}

		return 0;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long request, boolean doProvide)
	{
		return this.energy.extractEnergy(request, doProvide);
	}

	@Override
	public long getVoltage(ForgeDirection direction)
	{
		return 100;
	}

}
