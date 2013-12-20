package resonantinduction.furnace;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import universalelectricity.api.IEnergyInterface;
import universalelectricity.api.IVoltage;
import universalelectricity.api.UniversalClass;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.electricity.ElectricityHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

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
	private int energyBuffer = 0;

	@Override
	public void updateEntity()
	{
		if (this.init)
		{
			this.checkProduce();
			this.init = false;
		}

		if (this.energyBuffer >= ResonantInduction.FURNACE_WATTAGE / 20)
		{
			this.furnaceCookTime++;

			if (this.furnaceCookTime == 200)
			{
				this.furnaceCookTime = 0;
				this.smeltItem();
				this.onInventoryChanged();
			}

			this.energyBuffer = 0;
		}
		else
		{
			super.updateEntity();

			if (this.doProduce)
			{
				if (this.getStackInSlot(0) == null)
				{
					boolean hasRequest = false;

					for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
					{
						TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(direction).getTileEntity(this.worldObj);

						if (tileEntity instanceof IConductor)
						{
							if (((IConductor) tileEntity).getNetwork().getRequest(this).getWatts() > 0)
							{
								if (this.furnaceBurnTime > 0)
								{
									this.produceUE(direction);
								}

								hasRequest = true;
								break;
							}
						}
					}

					if (hasRequest)
					{
						/**
						 * Steal power from furnace.
						 */
						boolean doBlockStateUpdate = this.furnaceBurnTime > 0;

						if (this.furnaceBurnTime == 0)
						{
							int burnTime = TileEntityFurnace.getItemBurnTime(this.getStackInSlot(1));
							this.decrStackSize(1, 1);
							this.furnaceBurnTime = burnTime;
						}

						if (doBlockStateUpdate != this.furnaceBurnTime > 0)
						{
							// BlockFurnace.updateFurnaceBlockState(this.furnaceBurnTime > 0,
							// this.worldObj, this.xCoord, this.yCoord, this.zCoord);
							this.refreshConductors();
						}
					}
				}
			}
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

	/**
	 * Produces UE power towards a specific direction.
	 * 
	 * @param outputDirection - The output direction.
	 */
	public boolean produceUE(ForgeDirection outputDirection)
	{
		if (!this.worldObj.isRemote && outputDirection != null && outputDirection != ForgeDirection.UNKNOWN)
		{
			float provide = this.onReceiveEnergy(null, Integer.MAX_VALUE, false);

			if (provide > 0)
			{
				TileEntity outputTile = VectorHelper.getConnectorFromSide(this.worldObj, new Vector3(this), outputDirection);
				IElectricityNetwork outputNetwork = ElectricityHelper.getNetworkFromTileEntity(outputTile, outputDirection);

				if (outputNetwork != null)
				{
					ElectricityPack powerRequest = outputNetwork.getRequest(this);

					if (powerRequest.getWatts() > 0)
					{
						ElectricityPack sendPack = ElectricityPack.getFromWatts(provide, this.getVoltage(null));
						float rejectedPower = outputNetwork.produce(sendPack, this);
						this.onExtractEnergy(outputDirection.getOpposite(), (int) (sendPack.getWatts() - rejectedPower), true);
					}

					return true;
				}
			}
		}

		return false;
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
			if (doReceive)
			{
				this.energyBuffer += receive;
				return receive;
			}

			return ResonantInduction.FURNACE_WATTAGE / 20;
		}

		return 0;
	}

	@Override
	public int onExtractEnergy(ForgeDirection from, int request, boolean doProvide)
	{
		if (this.furnaceBurnTime > 0)
		{
			return ResonantInduction.FURNACE_WATTAGE / 20;
		}

		return 0;
	}

	@Override
	public int getVoltage(ForgeDirection direction)
	{
		return 100;
	}

}
