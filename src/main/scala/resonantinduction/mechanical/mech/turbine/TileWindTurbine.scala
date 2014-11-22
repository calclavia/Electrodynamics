package resonantinduction.mechanical.mech.turbine

import java.util.List

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.biome.{BiomeGenBase, BiomeGenOcean, BiomeGenPlains}
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTank, FluidTankInfo}
import resonant.api.IBoilHandler
import resonant.content.prefab.itemblock.ItemBlockMetadata
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.inventory.InventoryUtility
import resonant.lib.wrapper.WrapList._
import resonantinduction.core.Settings

/**
 * The vertical wind turbine collects airflow.
 *
 * The horizontal wind turbine collects steam from steam power plants.
 *
 * @author Calclavia
 */
class TileWindTurbine extends TileTurbine with IBoilHandler
{
  /**
   * Wind simulations
   */
  private final val openBlockCache = new Array[Byte](224)
  private var checkCount = 0
  private var efficiency = 0f
  private var windPower = 0d

  /**
   * Steam simulations
   */
  private val gasTank = new FluidTank(1000)

  //Constructor
  this.itemBlock = classOf[ItemBlockMetadata]

  override def update()
  {
    super.update()

    if (!worldObj.isRemote)
    {
      if (tier == 0 && getDirection.offsetY == 0 && worldObj.isRaining && worldObj.isThundering && worldObj.rand.nextFloat < 0.00000008)
      {
        //Break under storm
        InventoryUtility.dropItemStack(worldObj, new Vector3(x, y, z), new ItemStack(Blocks.wool, 1 + worldObj.rand.nextInt(2)))
        InventoryUtility.dropItemStack(worldObj, new Vector3(x, y, z), new ItemStack(Items.stick, 3 + worldObj.rand.nextInt(8)))
        toVectorWorld.setBlockToAir()
      }
      else if (getMultiBlock.isPrimary)
      {
        //Only execute code in the primary block
        if (getDirection.offsetY == 0)
        {
          //This is a vertical wind turbine
          if (ticks % 20 == 0)
            computePower()

          getMultiBlock.get.mechanicalNode.rotate(windPower * multiBlockRadius / 20, windPower / multiBlockRadius / 20)
        }
        else
        {
          //This is a horizontal turbine
          getMultiBlock.get.mechanicalNode.rotate(if (gasTank.getFluid != null) gasTank.drain(gasTank.getFluidAmount, true).amount else 0 * 1000 * Settings.steamMultiplier, 10)
        }
      }
    }
  }

  private def computePower()
  {
    val checkSize = 10
    val height = yCoord + checkCount / 28
    val deviation = checkCount % 7
    var checkDir: ForgeDirection = null
    var check: Vector3 = null
    val cc = checkCount / 7 % 4

    cc match
    {
      case 0 =>
        checkDir = ForgeDirection.NORTH
        check = new Vector3(xCoord - 3 + deviation, height, zCoord - 4)
      case 1 =>
        checkDir = ForgeDirection.WEST
        check = new Vector3(xCoord - 4, height, zCoord - 3 + deviation)
      case 2 =>
        checkDir = ForgeDirection.WEST
        check = new Vector3(xCoord - 4, height, zCoord - 3 + deviation)
      case 3 =>
        checkDir = ForgeDirection.EAST
        check = new Vector3(xCoord + 4, height, zCoord - 3 + deviation)
    }

    var openAirBlocks = 0

    while (openAirBlocks < checkSize && world.isAirBlock(check.xi, check.yi, check.zi))
    {
      check.add(checkDir)
      openAirBlocks += 1
    }

    efficiency = efficiency - openBlockCache(checkCount) + openAirBlocks
    openBlockCache(checkCount) = openAirBlocks.toByte
    checkCount = (checkCount + 1) % (openBlockCache.length - 1)
    val multiblockMultiplier = multiBlockRadius + 0.5f

    val materialMultiplier = tier match
    {
      case 0 => 1.1f
      case 1 => 0.9f
      case 2 => 1f
    }

    val biome = worldObj.getBiomeGenForCoords(xCoord, zCoord)
    val hasBonus = biome.isInstanceOf[BiomeGenOcean] || biome.isInstanceOf[BiomeGenPlains] || biome == BiomeGenBase.river
    val windSpeed = (worldObj.rand.nextFloat / 5) + (yCoord / 256f) * (if (hasBonus) 1.2f else 1) + worldObj.getRainStrength(0.5f)

    windPower = materialMultiplier * multiblockMultiplier * windSpeed * efficiency * Settings.WIND_POWER_RATIO / 20
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

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = Array(gasTank.getInfo)
}