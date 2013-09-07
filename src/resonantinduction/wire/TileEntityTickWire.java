package resonantinduction.wire;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.block.BlockFurnace;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;

/**
 * A version of the wire that has furnace interaction.
 * 
 * @author Calclavia
 */
public class TileEntityTickWire extends TileEntityWire
{
	private final HashMap<ForgeDirection, TileEntityFurnace> furnaces = new HashMap<ForgeDirection, TileEntityFurnace>();
	private float energyBuffer;
	private static final float FURNACE_VOLTAGE = 120;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.getNetwork().getRequest().getWatts() > 0)
		{
			Iterator<Entry<ForgeDirection, TileEntityFurnace>> it = this.furnaces.entrySet().iterator();

			while (it.hasNext())
			{
				Entry<ForgeDirection, TileEntityFurnace> entry = it.next();
				ForgeDirection direction = entry.getKey();
				TileEntityFurnace tileEntity = entry.getValue();

				if (!tileEntity.isInvalid() && tileEntity.getStackInSlot(0) == null)
				{
					/**
					 * Steal power from furnace.
					 */
					boolean doBlockStateUpdate = tileEntity.furnaceBurnTime > 0;

					if (tileEntity.furnaceBurnTime == 0)
					{
						int burnTime = TileEntityFurnace.getItemBurnTime(tileEntity.getStackInSlot(1));
						tileEntity.decrStackSize(1, 1);
						tileEntity.furnaceBurnTime = burnTime;
					}

					if (tileEntity.furnaceBurnTime > 0)
					{
						this.getNetwork().produce(ElectricityPack.getFromWatts(ResonantInduction.FURNACE_WATTAGE / 20, FURNACE_VOLTAGE));
					}

					if (doBlockStateUpdate != tileEntity.furnaceBurnTime > 0)
					{
						BlockFurnace.updateFurnaceBlockState(tileEntity.furnaceBurnTime > 0, tileEntity.worldObj, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
					}
				}
			}
		}
	}

	// @Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
	{
		this.energyBuffer += receive.getWatts();

		/**
		 * Inject power to furnace.
		 */
		for (int i = 0; i < this.furnaces.size(); i++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(i);
			TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(direction).getTileEntity(this.worldObj);

			if (tileEntity instanceof TileEntityFurnace)
			{
				TileEntityFurnace furnaceTile = (TileEntityFurnace) tileEntity;

				boolean doBlockStateUpdate = furnaceTile.furnaceBurnTime > 0;

				furnaceTile.furnaceBurnTime += 2;

				if (doBlockStateUpdate != furnaceTile.furnaceBurnTime > 0)
				{
					BlockFurnace.updateFurnaceBlockState(furnaceTile.furnaceBurnTime > 0, furnaceTile.worldObj, furnaceTile.xCoord, furnaceTile.yCoord, furnaceTile.zCoord);
				}
			}
		}

		return receive.getWatts();
	}

	@Override
	public void refresh()
	{
		super.refresh();

		if (!this.worldObj.isRemote)
		{
			this.furnaces.clear();

			for (int i = 0; i < 6; i++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(direction).getTileEntity(this.worldObj);

				if (tileEntity instanceof TileEntityFurnace)
				{
					this.furnaces.put(direction, (TileEntityFurnace) tileEntity);
				}
			}
		}
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	// @Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide)
	{
		return new ElectricityPack();
	}

	// @Override
	public float getRequest(ForgeDirection direction)
	{
		return this.furnaces.size() > 0 ? ResonantInduction.FURNACE_WATTAGE / 20 : 0;
	}

	// @Override
	public float getProvide(ForgeDirection direction)
	{
		return 0;
	}

	// @Override
	public float getVoltage()
	{
		return FURNACE_VOLTAGE;
	}

}
