package resonantinduction.mechanical.energy.turbine;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraft.world.biome.BiomeGenPlains;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.inventory.InventoryUtility;

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
			if (tier == 0 && worldObj.isRaining() && worldObj.isThundering() && worldObj.rand.nextFloat() > 0.00000008)
			{
				InventoryUtility.dropItemStack(worldObj, new Vector3(this), new ItemStack(Block.cloth, 1 + worldObj.rand.nextInt(3)));
				InventoryUtility.dropItemStack(worldObj, new Vector3(this), new ItemStack(Item.stick, 4 + worldObj.rand.nextInt(8)));
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
				return;
			}

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
