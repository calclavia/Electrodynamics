package resonantinduction.core.prefab.node

import net.minecraft.block.material.Material
import net.minecraftforge.common.util.ForgeDirection

/**
 * Created by robert on 8/15/2014.
 */
class TilePressureNode(material: Material) extends TileTankNode(material: Material) {

  //Constructor
  tankNode_$eq(new NodePressure(this))

  def getPressureNode: NodePressure = {
    return tankNode.asInstanceOf[NodePressure]
  }

  def getPressure(direction: ForgeDirection): Int = {
    return getPressureNode.getPressure(direction)
  }
}