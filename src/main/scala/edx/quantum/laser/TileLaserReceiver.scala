package edx.quantum.laser

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.block.BlockPistonBase
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.{MovingObjectPosition, ResourceLocation}
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11._
import resonantengine.lib.grid.energy.electric.NodeElectricComponent
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.render.RenderUtility
import resonantengine.lib.transform.vector.Vector3
import resonantengine.prefab.block.impl.{TBlockNodeProvider, TRotatable}

import scala.collection.convert.wrapAll._

/**
 * A block that receives laser light and generates a voltage.
 * @author Calclavia
 */
object TileLaserReceiver
{
  @SideOnly(Side.CLIENT) val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "laserReceiver.tcn"))
  @SideOnly(Side.CLIENT) val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "laserReceiver.png")
}

class TileLaserReceiver extends ResonantTile(Material.rock) with ILaserHandler with TBlockNodeProvider with TRotatable
{
  val electricNode = new NodeElectricComponent(this)

  private var energy = 0D

  domain = ""
  textureName = "stone"
  normalRender = false
  isOpaqueCube = false
  nodes.add(electricNode)

  electricNode.dynamicTerminals = true
  electricNode.setPositives(Set(ForgeDirection.NORTH, ForgeDirection.EAST))
  electricNode.setNegatives(Set(ForgeDirection.SOUTH, ForgeDirection.WEST))

  override def canUpdate: Boolean = false

  override def onLaserHit(renderStart: Vector3, incident: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double): Boolean =
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
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    glPushMatrix()
    glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

    RenderUtility.enableBlending()

    getDirection match
    {
      case ForgeDirection.UNKNOWN =>
      case ForgeDirection.UP => glRotatef(-90, 1, 0, 0)
      case ForgeDirection.DOWN => glRotatef(90, 1, 0, 0)
      case ForgeDirection.NORTH => glRotatef(90, 0, 1, 0)
      case ForgeDirection.SOUTH => glRotatef(-90, 0, 1, 0)
      case ForgeDirection.WEST => glRotatef(-180, 0, 1, 0)
      case ForgeDirection.EAST => glRotatef(0, 0, 1, 0)
    }

    if (getDirection.offsetY == 0)
      glRotatef(-90, 0, 1, 0)
    else
      glRotatef(180, 1, 0, 0)

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(TileLaserReceiver.texture)
    TileLaserReceiver.model.renderAll()

    RenderUtility.disableBlending()

    glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    glPushMatrix()
    glRotated(180, 0, 1, 0)

    RenderUtility.enableBlending()

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(TileLaserReceiver.texture)
    TileLaserReceiver.model.renderAll()

    RenderUtility.disableBlending()

    glPopMatrix()
  }
}
