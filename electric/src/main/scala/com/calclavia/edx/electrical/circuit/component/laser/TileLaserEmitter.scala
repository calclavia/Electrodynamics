package com.calclavia.edx.electrical.circuit.component.laser

import com.calclavia.edx.core.Reference
import com.calclavia.graph.api.energy.NodeElectricComponent
import com.resonant.core.prefab.block.{Updater, Rotatable}
import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference

import nova.core.block.Block
import nova.core.game.Game
import nova.core.util.Direction
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._
import resonantengine.lib.grid.energy.electric.NodeElectricComponent
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.render.RenderUtility
import resonantengine.lib.transform.vector.Vector3
import resonantengine.prefab.block.impl.{TBlockNodeProvider, TRotatable}

import scala.collection.convert.wrapAll._

/**
 * An emitter that shoots out lasers.
 *
 * Consider: E=hf. Higher frequency light has more energy.
 *
 * @author Calclavia
 */
object TileLaserEmitter
{
  @SideOnly(Side.CLIENT)
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "laserEmitter.tcn"))

  @SideOnly(Side.CLIENT)
  val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "laserEmitter.png")
}

class TileLaserEmitter extends Block with ILaserHandler with Rotatable with Updater
{
  val electricNode = Game.instance.componentManager.make(classOf[NodeElectricComponent], this)

  domain = ""
  textureName = "stone"
  electricNode.dynamicTerminals = true
  electricNode.setPositives(Set(Direction.NORTH, Direction.EAST, Direction.UP))
  electricNode.setNegatives(Set(Direction.SOUTH, Direction.WEST, Direction.DOWN))
  nodes.add(electricNode)

	override def isCube: Boolean = false

	/**
   * Called when the block is placed by a living entity
   * @param entityLiving - entity who placed the block
   * @param itemStack - ItemStack the entity used to place the block
   */
  override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
  {
    val l = BlockPistonBase.determineOrientation(world, x, y, z, entityLiving)
    world.setBlockMetadataWithNotify(x, y, z, l, 2)
  }

	override def getLightValue(world: IBlockAccess, x: Int, y: Int, z: Int): Int = super.getLightValue(world, x, y, z)

	override def getLightValue(access: IBlockAccess): Int = ((electricNode.power / Laser.maxEnergy) * 15).toInt

  override def onLaserHit(renderStart: Vector3, incidentDirection: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double) = false

	override def update(deltaTime: Double)
  {
    super.update()

    if (electricNode.power > 0)
    {
      Laser.spawn(worldObj, position + 0.5 + new Vector3(getDirection) * 0.51, position + new Vector3(getDirection) * 0.6 + 0.5, new Vector3(getDirection), electricNode.power / 20)
    }
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
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

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(TileLaserEmitter.texture)
    TileLaserEmitter.model.renderAll()

    RenderUtility.disableBlending()
    GL11.glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    glPushMatrix()
    glRotated(180, 0, 1, 0)

    RenderUtility.enableBlending()

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(TileLaserEmitter.texture)
    TileLaserEmitter.model.renderAll()

    RenderUtility.disableBlending()

    GL11.glPopMatrix()
  }
}
