package resonantinduction.mechanical.mech.turbine

import java.util.List

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.biome.{BiomeGenBase, BiomeGenOcean, BiomeGenPlains}
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidTankInfo, FluidStack, Fluid, FluidTank}
import resonant.api.IBoilHandler
import resonant.content.prefab.itemblock.ItemBlockMetadata
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.inventory.InventoryUtility
import resonant.lib.wrapper.WrapList._
import resonantinduction.core.Settings

/** The vertical wind turbine collects airflow. The horizontal wind turbine collects steam from steam
  * power plants.
  *
  * @author Calclavia */
class TileWindTurbine extends TileTurbine with IBoilHandler
{
  private final val openBlockCache = new Array[Byte](224)
  private var checkCount = 0
  private var efficiency = 0f
  private var windTorque = 0d
  private val gasTank = new FluidTank(1000)

  //Constructor
  this.itemBlock = classOf[ItemBlockMetadata]

  override def update()
  {
    super.update()

    if (tier == 0 && getDirection.offsetY == 0 && worldObj.isRaining && worldObj.isThundering && worldObj.rand.nextFloat < 0.00000008)
    {
      InventoryUtility.dropItemStack(worldObj, new Vector3(x, y, z), new ItemStack(Blocks.wool, 1 + worldObj.rand.nextInt(2)))
      InventoryUtility.dropItemStack(worldObj, new Vector3(x, y, z), new ItemStack(Items.stick, 3 + worldObj.rand.nextInt(8)))
      worldObj.setBlockToAir(xCoord, yCoord, zCoord)
    }
    else if (!getMultiBlock.isPrimary)
    {
      if (getDirection.offsetY == 0)
      {
        if (ticks % 20 == 0 && !worldObj.isRemote)
        {
          computePower()
        }

        getMultiBlock.get.mechanicalNode.rotate(3000)
      }
      else
      {
        getMultiBlock.get.mechanicalNode.rotate(10000)
      }
    }
  }

  private def computePower()
  {
    val checkSize: Int = 10
    val height: Int = yCoord + checkCount / 28
    val deviation: Int = checkCount % 7
    var checkDir: ForgeDirection = null
    var check: Vector3 = null
    val cc: Int = checkCount / 7 % 4

    if (cc == 0)
    {
      checkDir = ForgeDirection.NORTH
      check = new Vector3(xCoord - 3 + deviation, height, zCoord - 4)
    }
    else if (cc == 1)
    {
      checkDir = ForgeDirection.WEST
      check = new Vector3(xCoord - 4, height, zCoord - 3 + deviation)
    }
    else if (cc == 2)
    {
      checkDir = ForgeDirection.SOUTH
      check = new Vector3(xCoord - 3 + deviation, height, zCoord + 4)
    }
    else
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
    openBlockCache(checkCount) = openAirBlocks.asInstanceOf[Byte]
    checkCount = (checkCount + 1) % (openBlockCache.length - 1)
    val multiblockMultiplier: Float = (multiBlockRadius + 0.5f) * 2

    var materialMultiplier: Float = 1
    if (tier == 0)
    {
      materialMultiplier = 1.1f
    }
    else if (tier == 1)
    {
      materialMultiplier = 0.9f
    }
    else
    {
      materialMultiplier = 1
    }

    val biome: BiomeGenBase = worldObj.getBiomeGenForCoords(xCoord, zCoord)
    val hasBonus: Boolean = biome.isInstanceOf[BiomeGenOcean] || biome.isInstanceOf[BiomeGenPlains] || biome == BiomeGenBase.river
    val windSpeed: Float = (worldObj.rand.nextFloat / 8) + (yCoord / 256f) * (if (hasBonus) 1.2f else 1) + worldObj.getRainStrength(1.5f)

    windTorque = materialMultiplier * multiblockMultiplier * windSpeed * efficiency * Settings.WIND_POWER_RATIO
  }

  override def getSubBlocks(par1: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
  {
    for (i <- 0 to 2)
    {
      par3List.add(new ItemStack(par1, 1, i))
    }
  }

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = from == ForgeDirection.DOWN && fluid.getName.contains("steam")

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = gasTank.fill(resource, doFill)

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = null

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = null

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    val re: Array[FluidTankInfo] = new Array[FluidTankInfo](1)
    re(1) = gasTank.getInfo
    return re
  }
}