package resonantinduction.core.prefab.node

import net.minecraft.block.material.Material
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.prefab.fluid.NodeFluid

class TilePressureNode(material: Material) extends TileFluidProvider(material: Material)
{
  //Constructor
  fluidNode == new NodePressure(this)

  override protected var fluidNode: NodeFluid = new NodePressure(this)

  def getPressureNode: NodePressure =
  {
    return fluidNode.asInstanceOf[NodePressure]
  }

  def getPressure(direction: ForgeDirection): Int =
  {
    return getPressureNode.pressure(direction)
  }
}