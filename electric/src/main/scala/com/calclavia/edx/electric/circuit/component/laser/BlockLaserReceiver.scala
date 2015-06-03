package com.calclavia.edx.electric.circuit.component.laser

import nova.core.block.Block
import nova.core.util.Direction

/**
 * A block that receives laser light and generates a voltage.
 * @author Calclavia
 */
class BlockLaserReceiver extends Block with LaserHandler with TBlockNodeProvider with TRotatable
{
  val electricNode = new NodeElectricComponent(this)

  private var energy = 0D

  domain = ""
  textureName = "stone"
  normalRender = false
  isOpaqueCube = false
  nodes.add(electricNode)

  electricNode.dynamicTerminals = true
  electricNode.setPositives(Set(Direction.NORTH, Direction.EAST))
  electricNode.setNegatives(Set(Direction.SOUTH, Direction.WEST))

  override def canUpdate: Boolean = false

	override def onLaserHit(renderStart: Vector3d, incident: Vector3d, hit: MovingObjectPosition, color: Vector3d, energy: Double): Boolean =
  {
    if (hit.sideHit == getDirection.ordinal)
    {
      electricNode.generatePower(energy)
    }

    return false
  }

  override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
  {
    val l = BlockPistonBase.determineOrientation(world, x, y, z, entityLiving)
    world.setBlockMetadataWithNotify(x, y, z, l, 2)
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3d, frame: Float, pass: Int)
  {
    glPushMatrix()
    glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

    RenderUtility.enableBlending()

    getDirection match
    {
      case Direction.UNKNOWN =>
      case Direction.UP => glRotatef(-90, 1, 0, 0)
      case Direction.DOWN => glRotatef(90, 1, 0, 0)
      case Direction.NORTH => glRotatef(90, 0, 1, 0)
      case Direction.SOUTH => glRotatef(-90, 0, 1, 0)
      case Direction.WEST => glRotatef(-180, 0, 1, 0)
      case Direction.EAST => glRotatef(0, 0, 1, 0)
    }

    if (getDirection.offsetY == 0)
      glRotatef(-90, 0, 1, 0)
    else
      glRotatef(180, 1, 0, 0)

	  FMLClientHandler.instance.getClient.renderEngine.bindTexture(BlockLaserReceiver.texture)
	  BlockLaserReceiver.model.renderAll()

    RenderUtility.disableBlending()

    glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    glPushMatrix()
    glRotated(180, 0, 1, 0)

    RenderUtility.enableBlending()

	  FMLClientHandler.instance.getClient.renderEngine.bindTexture(BlockLaserReceiver.texture)
	  BlockLaserReceiver.model.renderAll()

    RenderUtility.disableBlending()

    glPopMatrix()
  }
}
