package resonantinduction.mechanical.mech.turbine

import java.util.List

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.biome.{BiomeGenBase, BiomeGenOcean, BiomeGenPlains}
import net.minecraftforge.common.util.ForgeDirection
import resonant.content.prefab.itemblock.ItemBlockMetadata
import resonant.lib.utility.inventory.InventoryUtility
import resonantinduction.core.Settings
import universalelectricity.core.transform.vector.Vector3
import resonant.lib.wrapper.WrapList._

/** The vertical wind turbine collects airflow. The horizontal wind turbine collects steam from steam
  * power plants.
  *
  * @author Calclavia */
class TileWindTurbine extends TileTurbine
{

    private final val openBlockCache: Array[Byte] = new Array[Byte](224)
    private var checkCount: Int = 0
    private var efficiency: Float = 0
    private var windPower: Long = 0

    //Constructor
    this.itemBlock = classOf[ItemBlockMetadata]

    override def update
    {
        if (tier == 0 && getDirection.offsetY == 0 && worldObj.isRaining && worldObj.isThundering && worldObj.rand.nextFloat < 0.00000008)
        {
            InventoryUtility.dropItemStack(worldObj, new Vector3(this), new ItemStack(Blocks.wool, 1 + worldObj.rand.nextInt(2)))
            InventoryUtility.dropItemStack(worldObj, new Vector3(this), new ItemStack(Items.stick, 3 + worldObj.rand.nextInt(8)))
            worldObj.setBlockToAir(xCoord, yCoord, zCoord)
            return
        }
        if (!getMultiBlock.isPrimary) return
        if (getDirection.offsetY == 0)
        {
            maxPower = 3000
            if (ticks % 20 == 0 && !worldObj.isRemote) computePower
            getMultiBlock.get.power += windPower
        }
        else
        {
            maxPower = 10000
        }
        if (getMultiBlock.isConstructed) mechanicalNode.torque = (defaultTorque / (9d / multiBlockRadius)).asInstanceOf[Long]
        else mechanicalNode.torque = defaultTorque / 12
        super.update
    }

    private def computePower
    {
        val checkSize: Int = 10
        val height: Int = yCoord + checkCount / 28
        val deviation: Int = checkCount % 7
        var checkDir: ForgeDirection = null
        var check: Vector3 = null
        var cc = checkCount / 7 % 4

        if (cc == 0)
        {
            checkDir = ForgeDirection.NORTH
            check = new Vector3(xCoord - 3 + deviation, height, zCoord - 4)
        } else if (cc == 1)
        {
            checkDir = ForgeDirection.WEST
            check = new Vector3(xCoord - 4, height, zCoord - 3 + deviation)
        } else if (cc == 2)
        {
            checkDir = ForgeDirection.SOUTH
            check = new Vector3(xCoord - 3 + deviation, height, zCoord + 4)
        } else
        {
            checkDir = ForgeDirection.EAST
            check = new Vector3(xCoord + 4, height, zCoord - 3 + deviation)
        }
        var openAirBlocks: Int = 0
        while (openAirBlocks < checkSize && worldObj.isAirBlock(check.xi, check.yi, check.zi))
        {
            check.add(checkDir)
            openAirBlocks += 1
        }
        efficiency = efficiency - openBlockCache(checkCount) + openAirBlocks
        openBlockCache(checkCount) = openAirBlocks.asInstanceOf
        checkCount = (checkCount + 1) % (openBlockCache.length - 1)
        val multiblockMultiplier: Float = (multiBlockRadius + 0.5f) * 2
        val materialMultiplier: Float = if (tier == 0) 1.1f else if (tier == 1) 0.9f else 1
        val biome: BiomeGenBase = worldObj.getBiomeGenForCoords(xCoord, zCoord)
        val hasBonus: Boolean = biome.isInstanceOf[BiomeGenOcean] || biome.isInstanceOf[BiomeGenPlains] || biome == BiomeGenBase.river
        val windSpeed: Float = (worldObj.rand.nextFloat / 8) + (yCoord / 256f) * (if (hasBonus) 1.2f else 1) + worldObj.getRainStrength(1.5f)
        windPower = Math.min(materialMultiplier * multiblockMultiplier * windSpeed * efficiency * Settings.WIND_POWER_RATIO, maxPower * Settings.WIND_POWER_RATIO).asInstanceOf[Long]
    }

    override def getSubBlocks(par1: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
    {
        for (i <- 0 to 3)
        {
            par3List.add(new ItemStack(par1, 1, i))

        }
    }
}