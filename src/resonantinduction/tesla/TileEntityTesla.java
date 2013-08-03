/**
 * 
 */
package resonantinduction.tesla;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.BlockFurnace;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import resonantinduction.ITesla;
import resonantinduction.ResonantInduction;
import resonantinduction.base.TileEntityBase;
import resonantinduction.base.Vector3;

/**
 * @author Calclavia
 * 
 */
public class TileEntityTesla extends TileEntityBase implements ITesla
{
	private float energy = 0;
	private boolean doTransfer = false;

	private Set<TileEntityTesla> connectedTeslas = new HashSet<TileEntityTesla>();

	@Override
	public void initiate()
	{
		TeslaGrid.instance().register(this);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		/**
		 * Only transfer if it is the bottom controlling Tesla tower.
		 */
		// && this.getEnergyStored() > 0 && this.doTransfer
		if (this.ticks % 2 == 0 && this.isController() && !this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
		{
			Set<ITesla> transferTeslaCoils = new HashSet<ITesla>();

			for (ITesla tesla : TeslaGrid.instance().get())
			{
				if (new Vector3((TileEntity) tesla).distance(new Vector3(this)) < this.getRange())
				{
					if (!this.connectedTeslas.contains(tesla))
					{
						if (tesla instanceof TileEntityTesla)
						{
							tesla = ((TileEntityTesla) tesla).getControllingTelsa();
						}

						transferTeslaCoils.add(tesla);
					}
				}
			}

			if (transferTeslaCoils.size() > 0)
			{
				float transferEnergy = this.getEnergyStored() / transferTeslaCoils.size();

				for (ITesla tesla : transferTeslaCoils)
				{
					tesla.transfer(transferEnergy * (1 - (this.worldObj.rand.nextFloat() * 0.1f)));
					this.transfer(-transferEnergy);
					ResonantInduction.proxy.renderElectricShock(this.worldObj, new Vector3(this.getTopTelsa()).translate(new Vector3(0.5)), new Vector3((TileEntity) tesla).translate(new Vector3(0.5)));
				}
			}
		}

		/*
		 * int radius = 10; List<Entity> entities =
		 * this.worldObj.getEntitiesWithinAABBExcludingEntity(null,
		 * AxisAlignedBB.getAABBPool().getAABB(this.xCoord - radius, this.yCoord - radius,
		 * this.zCoord - radius, this.xCoord + radius, this.yCoord + radius, this.zCoord + radius));
		 * 
		 * for (Entity entity : entities) {
		 * ResonantInduction.proxy.renderElectricShock(this.worldObj, new
		 * Vector3(this).translate(new Vector3(0.5)), new Vector3(entity)); }
		 */

		/**
		 * Draws power from furnace below it.
		 * 
		 * @author Calclavia
		 */
		TileEntity tileEntity = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);

		if (tileEntity instanceof TileEntityFurnace)
		{
			TileEntityFurnace furnaceTile = (TileEntityFurnace) tileEntity;

			if (furnaceTile.getStackInSlot(0) == null)
			{
				/**
				 * Steal power from furnace.
				 */
				boolean doBlockStateUpdate = furnaceTile.furnaceBurnTime > 0;

				if (furnaceTile.furnaceBurnTime == 0)
				{
					int burnTime = TileEntityFurnace.getItemBurnTime(furnaceTile.getStackInSlot(1));

					if (burnTime > 0)
					{
						furnaceTile.decrStackSize(1, 1);
						furnaceTile.furnaceBurnTime = burnTime;
					}
				}
				else
				{
					this.transfer(ResonantInduction.POWER_PER_COAL / 20);
				}

				if (doBlockStateUpdate != furnaceTile.furnaceBurnTime > 0)
				{
					BlockFurnace.updateFurnaceBlockState(furnaceTile.furnaceBurnTime > 0, furnaceTile.worldObj, furnaceTile.xCoord, furnaceTile.yCoord, furnaceTile.zCoord);
				}
			}
			else if (this.getEnergyStored() > ResonantInduction.POWER_PER_COAL / 20 && furnaceTile.getStackInSlot(1) == null && FurnaceRecipes.smelting().getSmeltingResult(furnaceTile.getStackInSlot(0)) != null)
			{
				/**
				 * Inject power to furnace.
				 */
				boolean doBlockStateUpdate = furnaceTile.furnaceBurnTime > 0;

				furnaceTile.furnaceBurnTime++;
				this.transfer(-ResonantInduction.POWER_PER_COAL / 20);

				if (doBlockStateUpdate != furnaceTile.furnaceBurnTime > 0)
				{
					BlockFurnace.updateFurnaceBlockState(furnaceTile.furnaceBurnTime > 0, furnaceTile.worldObj, furnaceTile.xCoord, furnaceTile.yCoord, furnaceTile.zCoord);
				}
			}
		}
	}

	private boolean isController()
	{
		return this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) == 0;
	}

	@Override
	public void transfer(float transferEnergy)
	{
		if (isController() || this.getControllingTelsa() == this)
		{
			this.energy = Math.max(this.energy + transferEnergy, 0);
			this.doTransfer = true;
		}
		else
		{
			this.getControllingTelsa().transfer(transferEnergy);
		}
	}

	public float getEnergyStored()
	{
		return this.energy;
	}

	public int getRange()
	{
		return 5 * this.getHeight();
	}

	public void updatePositionStatus()
	{
		boolean isTop = new Vector3(this).translate(new Vector3(0, 1, 0)).getTileEntity(this.worldObj) instanceof TileEntityTesla;
		boolean isBottom = new Vector3(this).translate(new Vector3(0, -1, 0)).getTileEntity(this.worldObj) instanceof TileEntityTesla;

		if (isTop && isBottom)
		{
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 3);
		}
		else if (isBottom)
		{
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 2, 3);
		}
		else
		{
			this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 0, 3);
		}
	}

	/**
	 * Called only on bottom.
	 * 
	 * @return The highest Tesla coil in this tower.
	 */
	public TileEntityTesla getTopTelsa()
	{
		this.connectedTeslas.clear();
		Vector3 checkPosition = new Vector3(this);
		TileEntityTesla returnTile = this;

		while (true)
		{
			TileEntity t = checkPosition.getTileEntity(this.worldObj);

			if (t instanceof TileEntityTesla)
			{
				this.connectedTeslas.add((TileEntityTesla) t);
				returnTile = (TileEntityTesla) t;
			}
			else
			{
				break;
			}

			checkPosition.y++;
		}

		return returnTile;
	}

	/**
	 * For non-controlling Tesla to use.
	 * 
	 * @return
	 */
	public TileEntityTesla getControllingTelsa()
	{
		Vector3 checkPosition = new Vector3(this);
		TileEntityTesla returnTile = this;

		while (true)
		{
			TileEntity t = checkPosition.getTileEntity(this.worldObj);

			if (t instanceof TileEntityTesla)
			{
				returnTile = (TileEntityTesla) t;
			}
			else
			{
				break;
			}

			checkPosition.y--;
		}

		return returnTile;
	}

	/**
	 * Called only on bottom.
	 * 
	 * @return The highest Tesla coil in this tower.
	 */
	public int getHeight()
	{
		this.connectedTeslas.clear();
		int y = 0;

		while (true)
		{
			TileEntity t = new Vector3(this).translate(new Vector3(0, y, 0)).getTileEntity(this.worldObj);

			if (t instanceof TileEntityTesla)
			{
				this.connectedTeslas.add((TileEntityTesla) t);
				y++;
			}
			else
			{
				break;
			}

		}

		return y;
	}

	@Override
	public void invalidate()
	{
		TeslaGrid.instance().unregister(this);
		super.invalidate();
	}

}
