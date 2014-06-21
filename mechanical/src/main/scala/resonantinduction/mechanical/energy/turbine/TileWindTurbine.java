package resonantinduction.mechanical.energy.turbine;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraft.world.biome.BiomeGenPlains;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Settings;
import universalelectricity.api.vector.Vector3;

/** The vertical wind turbine collects airflow. The horizontal wind turbine collects steam from steam
 * power plants.
 * 
 * @author Calclavia */
public class TileWindTurbine extends TileTurbine
{
    private final byte[] openBlockCache = new byte[224];
    private int checkCount = 0;
    private float efficiency = 0;
    private long windPower = 0;


    @Override
    public void updateEntity()
    {
        /** Break under storm. */
        if (tier == 0 && getDirection().offsetY == 0 && worldObj.isRaining() && worldObj.isThundering() && worldObj.rand.nextFloat() < 0.00000008)
        {
            InventoryUtility.dropItemStack(worldObj, new Vector3(this), new ItemStack(Block.cloth, 1 + worldObj.rand.nextInt(2)));
            InventoryUtility.dropItemStack(worldObj, new Vector3(this), new ItemStack(Item.stick, 3 + worldObj.rand.nextInt(8)));
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
            return;
        }

        /** Only the primary turbine ticks. */
        if (!getMultiBlock().isPrimary())
            return;

        /** If this is a vertical turbine. */
        if (getDirection().offsetY == 0)
        {
            maxPower = 3000;

            if (ticks % 20 == 0 && !worldObj.isRemote)
                computePower();

            getMultiBlock().get().power += windPower;
        }
        else
        {
            maxPower = 10000;
        }

        if (getMultiBlock().isConstructed())
            mechanicalNode.torque = (long) (defaultTorque / (9d / multiBlockRadius));
        else
            mechanicalNode.torque = defaultTorque / 12;

        super.updateEntity();
    }

    private void computePower()
    {
        int checkSize = 10;
        int height = yCoord + checkCount / 28;
        int deviation = checkCount % 7;
        ForgeDirection checkDir;

        Vector3 check = new Vector3(this);

        switch (checkCount / 7 % 4)
        {
            case 0:
                checkDir = ForgeDirection.NORTH;
                check = new Vector3(xCoord - 3 + deviation, height, zCoord - 4);
                break;
            case 1:
                checkDir = ForgeDirection.WEST;
                check = new Vector3(xCoord - 4, height, zCoord - 3 + deviation);
                break;
            case 2:
                checkDir = ForgeDirection.SOUTH;
                check = new Vector3(xCoord - 3 + deviation, height, zCoord + 4);
                break;
            default:
                checkDir = ForgeDirection.EAST;
                check = new Vector3(xCoord + 4, height, zCoord - 3 + deviation);
        }

        byte openAirBlocks = 0;

        while (openAirBlocks < checkSize && worldObj.isAirBlock(check.intX(), check.intY(), check.intZ()))
        {
            check.translate(checkDir);
            openAirBlocks++;
        }

        efficiency = efficiency - openBlockCache[checkCount] + openAirBlocks;
        openBlockCache[checkCount] = openAirBlocks;
        checkCount = (checkCount + 1) % (openBlockCache.length - 1);

        float multiblockMultiplier = (multiBlockRadius + 0.5f) * 2;
        float materialMultiplier = tier == 0 ? 1.1f : tier == 1 ? 0.9f : 1;

        BiomeGenBase biome = worldObj.getBiomeGenForCoords(xCoord, zCoord);
        boolean hasBonus = biome instanceof BiomeGenOcean || biome instanceof BiomeGenPlains || biome == BiomeGenBase.river;

        float windSpeed = (worldObj.rand.nextFloat() / 8) + (yCoord / 256f) * (hasBonus ? 1.2f : 1) + worldObj.getRainStrength(1.5f);
        windPower = (long) Math.min(materialMultiplier * multiblockMultiplier * windSpeed * efficiency * Settings.WIND_POWER_RATIO, maxPower * Settings.WIND_POWER_RATIO);
    }
}
