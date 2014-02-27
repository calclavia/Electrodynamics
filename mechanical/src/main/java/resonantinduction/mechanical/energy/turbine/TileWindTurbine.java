package resonantinduction.mechanical.energy.turbine;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraft.world.biome.BiomeGenPlains;

/**
 * The vertical wind turbine collects airflow.
 * The horizontal wind turbine collects steam from steam power plants.
 * 
 * @author Calclavia
 * 
 */
public class TileWindTurbine extends TileMechanicalTurbine
{
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
		 * If this is a vertical turbine.
		 */
		if (getDirection().offsetY == 0)
		{
			maxPower = 120;
			getMultiBlock().get().power += getWindPower();
		}
		else
		{
			maxPower = 1000;
		}
		
		if (getMultiBlock().isConstructed())
			torque = (long) (defaultTorque / (2.5f / multiBlockRadius));
		else
			torque = defaultTorque / 12;

		super.updateEntity();
	}

	public long getWindPower()
	{
		BiomeGenBase biome = worldObj.getBiomeGenForCoords(xCoord, zCoord);
		boolean hasBonus = biome instanceof BiomeGenOcean || biome instanceof BiomeGenPlains || biome == BiomeGenBase.river;

		if (!worldObj.canBlockSeeTheSky(xCoord, yCoord + 4, zCoord))
			return 0;

		return (long) (((((float) yCoord + 4) / 256) * maxPower) * (hasBonus ? 2 : 1) * (worldObj.isRaining() ? 1.5 : 1));
	}
}
