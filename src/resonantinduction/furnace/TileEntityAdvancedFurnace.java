package resonantinduction.furnace;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.electricity.IVoltage;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.vector.Vector3;
import universalelectricity.core.block.IConductor;
import calclavia.lib.prefab.block.EnergyStorage;

/**
 * Meant to replace the furnace class.
 * 
 * @author Calclavia
 * 
 */
@UniversalClass
public class TileEntityAdvancedFurnace extends TileEntityFurnace implements IEnergyInterface, IVoltage
{
	private static final float WATTAGE = 5;
	private boolean doProduce = false;
	private boolean init = true;

	private EnergyStorage energy = new EnergyStorage(ResonantInduction.FURNACE_WATTAGE * 5);

	@Override
	public void updateEntity()
	{
		boolean doSmeltingProcess = canSmelt() && this.getStackInSlot(1) == null && this.furnaceBurnTime == 0;

		if (doSmeltingProcess)
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
		else if (this.getStackInSlot(0) == null && TileEntityFurnace.getItemBurnTime(this.getStackInSlot(1)) > 0 && !this.energy.isFull())
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
			}

			if (doBlockStateUpdate != this.furnaceBurnTime > 0)
			{
				this.refreshConductors();
			}
		}
		else
		{
			// TODO: Fix vanilla changing the blockID of this furnace.
			super.updateEntity();
		}
	}

	/**
	 * Checks if the furnace should produce power.
	 */
	public void checkProduce()
	{
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(direction).getTileEntity(this.worldObj);

			if (tileEntity instanceof IConductor)
			{
				this.doProduce = true;
				return;
			}
		}

		this.doProduce = false;
	}

	public void refreshConductors()
	{
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(direction).getTileEntity(this.worldObj);

			if (tileEntity instanceof IConductor)
			{
				((IConductor) tileEntity).refresh();
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
	public int onReceiveEnergy(ForgeDirection from, int receive, boolean doReceive)
	{
		if (this.canSmelt() && this.getStackInSlot(1) == null && this.furnaceBurnTime == 0)
		{
			return this.energy.receiveEnergy(receive, doReceive);
		}

		return 0;
	}

	@Override
	public int onExtractEnergy(ForgeDirection from, int request, boolean doProvide)
	{
		return this.energy.extractEnergy(request, doProvide);
	}

	@Override
	public int getVoltage(ForgeDirection direction)
	{
		return 100;
	}

}
