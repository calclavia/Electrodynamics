package resonantinduction.mechanical.mech.turbine

import java.util.List

import cpw.mods.fml.relauncher.ReflectionHelper
import net.minecraft.block.{Block, BlockLiquid}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Vec3
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.tile.INodeProvider
import resonant.content.prefab.itemblock.ItemBlockMetadata
import resonant.lib.transform.vector.Vector3
import resonant.lib.wrapper.WrapList._
import resonantinduction.core.Settings
import resonantinduction.mechanical.mech.grid.NodeMechanical

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
      if (other.isInstanceOf[NodeMechanical] && !(other.isInstanceOf[TileTurbine]))
      {
        val sourceTile: TileEntity = toVectorWorld.add(from).getTileEntity

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
        getMultiBlock.get.mechanicalNode.rotate(getWaterPower, getWaterPower / 100)
        powerTicks -= 1
      }

      if (ticks % 20 == 0)
      {
        val blockIDAbove: Block = worldObj.getBlock(xCoord, yCoord + 1, zCoord)
        val metadata: Int = worldObj.getBlockMetadata(xCoord, yCoord + 1, zCoord)
        val isWater: Boolean = blockIDAbove == Blocks.water || blockIDAbove == Blocks.flowing_water
        if (isWater && worldObj.isAirBlock(xCoord, yCoord - 1, zCoord) && metadata == 0)
        {
          powerTicks = 20
          worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord)
          worldObj.setBlock(xCoord, yCoord - 1, zCoord, Blocks.flowing_water)
          getMultiBlock.get.mechanicalNode.rotate(10000, 10)
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
          val check: Vector3 = toVector3.add(dir)
          val blockID: Block = worldObj.getBlock(check.xi, check.yi, check.zi)
          val metadata: Int = worldObj.getBlockMetadata(check.xi, check.yi, check.zi)
          if (blockID == Blocks.water || blockID == Blocks.flowing_water)
          {

            val m = ReflectionHelper.findMethod(classOf[BlockLiquid], null, Array[String]("getFlowVector", "func_72202_i"), classOf[IBlockAccess], Integer.TYPE, Integer.TYPE, Integer.TYPE)
            val vector = new Vector3(m.invoke(Blocks.water, worldObj, check.xi: Integer, check.yi: Integer, check.zi: Integer).asInstanceOf[Vec3])
            val invert = (currentDir.offsetZ > 0 && vector.x < 0) || (currentDir.offsetZ < 0 && vector.x > 0) || (currentDir.offsetX > 0 && vector.z > 0) || (currentDir.offsetX < 0 && vector.z < 0)

            if (getDirection.offsetX != 0)
            {
              getMultiBlock.get.mechanicalNode.rotate(if (invert) -1 else 1 * Math.abs(getWaterPower * vector.z * (7 - metadata) / 7f), 10)
              powerTicks = 20
            }
            if (getDirection.offsetZ != 0)
            {
              getMultiBlock.get.mechanicalNode.rotate(if (invert) -1 else 1 * Math.abs(getWaterPower * vector.x * (7 - metadata) / 7f), 10)
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