package edx.mechanical.mech.turbine

import java.util.List

import cpw.mods.fml.relauncher.ReflectionHelper
import edx.core.Settings
import edx.mechanical.mech.grid.NodeMechanical
import net.minecraft.block.BlockLiquid
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Vec3
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.api.graph.INodeProvider
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.wrapper.CollectionWrapper._
import resonantengine.prefab.block.itemblock.ItemBlockMetadata

/**
 * The vertical water turbine collects flowing water flowing on X axis.
 * The horizontal water turbine collects flowing water on Z axis.
 *
 * @author Calclavia
 *
 */
class TileWaterTurbine extends TileTurbine
{
  var powerTicks = 0

  //Constructor
  itemBlock = classOf[ItemBlockMetadata]
  mechanicalNode = new NodeTurbine(this)
  {
    override def canConnect[B](other: B, from: ForgeDirection): Boolean =
    {
      if (other.isInstanceOf[NodeMechanical] && !other.isInstanceOf[TileTurbine])
      {
        val sourceTile: TileEntity = position.add(from).getTileEntity

        if (sourceTile.isInstanceOf[INodeProvider])
        {
          val sourceInstance: NodeMechanical = sourceTile.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], from.getOpposite).asInstanceOf[NodeMechanical]
          return sourceInstance == other && (from == getDirection.getOpposite || from == getDirection)
        }
      }

      return false
    }
  }

  override def update()
  {
    super.update()

    if (getDirection.offsetY != 0)
    {
      if (powerTicks > 0)
      {
        getMultiBlock.get.mechanicalNode.accelerate(getWaterPower)
        powerTicks -= 1
      }

      if (ticks % 20 == 0)
      {
        val blockAbove = worldObj.getBlock(xCoord, yCoord + 1, zCoord)
        val blockBelow = worldObj.getBlock(xCoord, yCoord - 1, zCoord)
        val metadata = worldObj.getBlockMetadata(xCoord, yCoord + 1, zCoord)
        val isWater = blockAbove == Blocks.water || blockAbove == Blocks.flowing_water

        if (isWater && metadata == 0 && blockBelow.isReplaceable(world, x, y - 1, z))
        {
          powerTicks = 20
          worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord)
          worldObj.setBlock(xCoord, yCoord - 1, zCoord, Blocks.flowing_water)
          getMultiBlock.get.mechanicalNode.accelerate(10000)
        }
      }
    }
    else
    {
      val currentDir: ForgeDirection = getDirection

      for (dir <- ForgeDirection.VALID_DIRECTIONS)
      {
        if (dir != currentDir && dir != currentDir.getOpposite)
        {
          val check: Vector3 = position.add(dir)
          val block = worldObj.getBlock(check.xi, check.yi, check.zi)
          val metadata: Int = worldObj.getBlockMetadata(check.xi, check.yi, check.zi)

          if (block == Blocks.water || block == Blocks.flowing_water)
          {
            val m = ReflectionHelper.findMethod(classOf[BlockLiquid], null, Array[String]("getFlowVector", "func_72202_i"), classOf[IBlockAccess], Integer.TYPE, Integer.TYPE, Integer.TYPE)
            val vector = new Vector3(m.invoke(Blocks.water, worldObj, check.xi: Integer, check.yi: Integer, check.zi: Integer).asInstanceOf[Vec3])
            val invert = (currentDir.offsetZ > 0 && vector.x < 0) || (currentDir.offsetZ < 0 && vector.x > 0) || (currentDir.offsetX > 0 && vector.z > 0) || (currentDir.offsetX < 0 && vector.z < 0)

            if (getDirection.offsetX != 0)
            {
              getMultiBlock.get.mechanicalNode.accelerate(if (invert) -1 else 1 * Math.abs(getWaterPower * vector.z * (7 - metadata) / 7f))
              powerTicks = 20
            }
            if (getDirection.offsetZ != 0)
            {
              getMultiBlock.get.mechanicalNode.accelerate(if (invert) -1 else 1 * Math.abs(getWaterPower * vector.x * (7 - metadata) / 7f))
              powerTicks = 20
            }
          }
        }
      }
    }
  }

  /**
   * Gravitation Potential Energy:
   * PE = mgh
   */
  private def getWaterPower = (10000 / (2 - tier + 1)) * Settings.WATER_POWER_RATIO

  override def getSubBlocks(par1: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
  {
    for (i <- 0 to 2)
    {
      par3List.add(new ItemStack(par1, 1, i))
    }
  }
}