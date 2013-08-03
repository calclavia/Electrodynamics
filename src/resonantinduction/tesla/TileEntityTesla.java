/**
 * 
 */
package resonantinduction.tesla;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.BlockFurnace;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
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

	@Override
	public void initiate()
	{
		TeslaGrid.instance().register(this);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.ticks % 2 == 0 /* &&this.getEnergyStored() > 0 && this.doTransfer */)
		{

			Set<ITesla> transferTeslaCoils = new HashSet<ITesla>();

			for (ITesla tesla : TeslaGrid.instance().get())
			{
				if (tesla != this && new Vector3((TileEntity) tesla).distance(new Vector3(this)) < this.getRange())
				{
					transferTeslaCoils.add(tesla);
				}
			}

			if (transferTeslaCoils.size() > 0)
			{
				float transferEnergy = this.getEnergyStored() / transferTeslaCoils.size();

				for (ITesla tesla : transferTeslaCoils)
				{
					tesla.transfer(transferEnergy * (1 - (this.worldObj.rand.nextFloat() * 0.1f)));
					this.transfer(-transferEnergy);
					ResonantInduction.proxy.renderElectricShock(this.worldObj, new Vector3(this).translate(new Vector3(0.5)), new Vector3((TileEntity) tesla).translate(new Vector3(0.5)));
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
					furnaceTile.furnaceBurnTime--;
				}

				if (doBlockStateUpdate != furnaceTile.furnaceBurnTime > 0)
				{
					BlockFurnace.updateFurnaceBlockState(furnaceTile.furnaceBurnTime > 0, furnaceTile.worldObj, furnaceTile.xCoord, furnaceTile.yCoord, furnaceTile.zCoord);
				}
			}
		}
	}

	@Override
	public void transfer(float transferEnergy)
	{
		this.energy += transferEnergy;
		this.doTransfer = true;
	}

	public float getEnergyStored()
	{
		return this.energy;
	}

	public int getRange()
	{
		return 8;
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

	@Override
	public void invalidate()
	{
		TeslaGrid.instance().unregister(this);
		super.invalidate();
	}

}
