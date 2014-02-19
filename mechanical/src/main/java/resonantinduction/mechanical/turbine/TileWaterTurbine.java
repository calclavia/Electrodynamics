package resonantinduction.mechanical.turbine;

import net.minecraft.block.Block;

/**
 * The vertical wind turbine collects airflow.
 * The horizontal wind turbine collects steam from steam power plants.
 * 
 * @author Calclavia
 * 
 */
public class TileWaterTurbine extends TileMechanicalTurbine
{
	public TileWaterTurbine()
	{
		maxPower = 300;
		torque = defaultTorque;
	}

	@Override
	public void invalidate()
	{
		getNetwork().split(this);
		super.invalidate();
	}

	@Override
	public void updateEntity()
	{

		/**
		 * If this is a horizontal turbine.
		 */
		if (getDirection().offsetY != 0)
		{
			int blockIDAbove = worldObj.getBlockId(xCoord, yCoord + 1, zCoord);

			if (blockIDAbove == Block.waterStill.blockID && worldObj.isAirBlock(xCoord, yCoord - 1, zCoord))
			{
				getMultiBlock().get().power += getWaterPower();
				worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord);
				worldObj.setBlock(xCoord, yCoord - 1, zCoord, Block.waterStill.blockID);
			}
		}
		else if (this.getMultiBlock().isPrimary())
		{
			if (worldObj.getBlockId(xCoord, yCoord - (this.getMultiBlock().isConstructed() ? 2 : 1), zCoord) == Block.waterMoving.blockID)
			{
				getMultiBlock().get().power += getWaterPower();
			}
		}

		super.updateEntity();
	}

	/**
	 * Gravitation Potential Energy:
	 * PE = mgh
	 */
	private long getWaterPower()
	{
		return 1 * 10 * 2;
	}
}
