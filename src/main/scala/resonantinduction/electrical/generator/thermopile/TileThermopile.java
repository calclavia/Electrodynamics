package resonantinduction.electrical.generator.thermopile;

import net.minecraft.block.Block;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.prefab.tile.TileElectrical;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;

public class TileThermopile extends TileElectrical
{
	private final int MAX_USE_TICKS = 120 * 20;

	/**
	 * The amount of ticks the thermopile will use the temperature differences before turning all
	 * adjacent sides to thermal equilibrium.
	 */
	private int usingTicks = 0;

	public TileThermopile()
	{
		this.energy = new EnergyStorageHandler(300);
		this.ioMap = 728;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			int heatSources = 0;
			int coolingSources = 0;

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				Vector3 checkPos = new Vector3(this).translate(dir);

				int blockID = checkPos.getBlockID(worldObj);

				if (blockID == Block.waterStill.blockID)
				{
					coolingSources++;
				}
				else if (blockID == Block.snow.blockID)
				{
					coolingSources += 2;
				}
				else if (blockID == Block.ice.blockID)
				{
					coolingSources += 2;
				}
				else if (blockID == Block.fire.blockID)
				{
					heatSources++;
				}
				else if (blockID == Block.lavaStill.blockID)
				{
					heatSources += 2;
				}
			}

			// Max difference would be "3"
			int multiplier = (3 - Math.abs(heatSources - coolingSources));

			if (multiplier > 0 && coolingSources > 0 && heatSources > 0)
			{
				energy.receiveEnergy(15 * multiplier, true);

				if (++usingTicks >= MAX_USE_TICKS)
				{
					/**
					 * Create Thermal Equilibrium
					 */
					for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
					{
						Vector3 checkPos = new Vector3(this).translate(dir);
						int blockID = checkPos.getBlockID(worldObj);

						if (blockID == Block.waterStill.blockID)
						{
							checkPos.setBlock(worldObj, 0);
						}
						else if (blockID == Block.ice.blockID)
						{
							checkPos.setBlock(worldObj, Block.waterStill.blockID);
						}
						else if (blockID == Block.fire.blockID)
						{
							checkPos.setBlock(worldObj, 0);
						}
						else if (blockID == Block.lavaStill.blockID)
						{
							checkPos.setBlock(worldObj, Block.stone.blockID);
						}
					}

					usingTicks = 0;
				}
			}

			produce();
		}
	}

}
