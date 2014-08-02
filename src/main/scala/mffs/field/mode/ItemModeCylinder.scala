package mffs.field.mode

import java.util.{HashSet, Set}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11
import resonant.api.mffs.machine.{IFieldMatrix, IProjector}
import resonant.lib.render.block.ModelCube
import universalelectricity.core.transform.vector.Vector3

/**
 * A cylinder mode.
 *
 * @author Calclavia, Thutmose
 */
class ItemModeCylinder extends ItemMode
{
  private val step = 1
  private val radiusExpansion: Int = 0

  def getExteriorPoints(projector: IFieldMatrix): Set[Vector3] =
  {
    val fieldBlocks = new HashSet[Vector3]
    val posScale = projector.getPositiveScale
    val negScale = projector.getNegativeScale
    val radius = (posScale.xi + negScale.xi + posScale.zi + negScale.zi) / 2
    val height = posScale.yi + negScale.yi

    for (x <- -radius to radius by step; y <- 0 to height by step; z <- -radius to radius by step)
    {
      if ((y == 0 || y == height - 1) && (x * x + z * z + radiusExpansion) <= (radius * radius))
      {
        fieldBlocks.add(new Vector3(x, y, z))
      }

      if ((x * x + z * z + radiusExpansion) <= (radius * radius) && (x * x + z * z + radiusExpansion) >= ((radius - 1) * (radius - 1)))
      {
        fieldBlocks.add(new Vector3(x, y, z))
      }
    }

    return fieldBlocks
  }

  def getInteriorPoints(projector: IFieldMatrix): Set[Vector3] =
  {
    val fieldBlocks = new HashSet[Vector3]
    val translation = projector.getTranslation
    val posScale = projector.getPositiveScale
    val negScale = projector.getNegativeScale
    val radius = (posScale.xi + negScale.xi + posScale.zi + negScale.zi) / 2
    val height = posScale.yi + negScale.yi

    for (x <- -radius to radius by step; y <- 0 to height by step; z <- -radius to radius by step)
    {
      val position = new Vector3(x, y, z)

      if (isInField(projector, position + new Vector3(projector.asInstanceOf[TileEntity]) + translation))
      {
        fieldBlocks.add(position)
      }
    }

    return fieldBlocks
  }

  override def isInField(projector: IFieldMatrix, position: Vector3): Boolean =
  {
    val posScale: Vector3 = projector.getPositiveScale
    val negScale: Vector3 = projector.getNegativeScale
    val radius: Int = (posScale.xi + negScale.xi + posScale.zi + negScale.zi) / 2
    val projectorPos: Vector3 = new Vector3(projector.asInstanceOf[TileEntity])
    projectorPos.add(projector.getTranslation)
    val relativePosition: Vector3 = position.clone.subtract(projectorPos)
    relativePosition.apply(new Rotation(-projector.getRotationYaw, -projector.getRotationPitch, 0))
    if (relativePosition.x * relativePosition.x + relativePosition.z * relativePosition.z + radiusExpansion <= radius * radius)
    {
      return true
    }
    return false
  }

  @SideOnly(Side.CLIENT)
  override def render(projector: IProjector, x: Double, y: Double, z: Double, f: Float, ticks: Long)
  {
    val scale = 0.15f
    val detail = 0.5f
    GL11.glScalef(scale, scale, scale)

    val radius = (1.5f * detail).toInt

    var i = 0

    //TODO: Check scale and detail
    for (renderX <- -radius to radius; renderY <- -radius to radius; renderZ <- -radius to radius)
    {
      if (((renderX * renderX + renderZ * renderZ + radiusExpansion) <= (radius * radius) && (renderX * renderX + renderZ * renderZ + radiusExpansion) >= ((radius - 1) * (radius - 1))) || ((renderY == 0 || renderY == radius - 1) && (renderX * renderX + renderZ * renderZ + radiusExpansion) <= (radius * radius)))
      {
        if (i % 2 == 0)
        {
          val vector = new Vector3(renderX / detail, renderY / detail, renderZ / detail)
          GL11.glTranslated(vector.x, vector.y, vector.z)
          ModelCube.INSTNACE.render
          GL11.glTranslated(-vector.x, -vector.y, -vector.z)
        }
        i += 1
      }
    }
  }
}