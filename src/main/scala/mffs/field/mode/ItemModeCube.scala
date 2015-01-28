package mffs.field.mode

import java.util.{HashSet, Set}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.tileentity.TileEntity
import nova.core.util.transform.Vector3d
import org.lwjgl.opengl.GL11
import resonantengine.api.mffs.machine.{IFieldMatrix, IProjector}
import resonantengine.lib.render.model.ModelCube
import resonantengine.lib.transform.region.Cuboid
import resonantengine.lib.transform.rotation.EulerAngle

class ItemModeCube extends ItemMode
{
  private val step = 1

  def getExteriorPoints(projector: IFieldMatrix): Set[Vector3d] =
  {
    val fieldBlocks = new HashSet[Vector3d]
    val posScale: Vector3d = projector.getPositiveScale
    val negScale: Vector3d = projector.getNegativeScale

    for (x <- -negScale.xi to posScale.xi by step; y <- -negScale.yi to posScale.yi by step; z <- -negScale.zi to posScale.zi by step)
      if (y == -negScale.yi || y == posScale.yi || x == -negScale.xi || x == posScale.xi || z == -negScale.zi || z == posScale.zi) {
        fieldBlocks.add(new Vector3d(x, y, z))
      }


    return fieldBlocks
  }

  def getInteriorPoints(projector: IFieldMatrix): Set[Vector3d] =
  {
    val fieldBlocks = new HashSet[Vector3d]
    val posScale = projector.getPositiveScale
    val negScale = projector.getNegativeScale

    //TODO: Check parallel possibility
    for (x <- -negScale.xi to posScale.xi by step; y <- -negScale.yi to posScale.yi by step; z <- -negScale.zi to posScale.zi by step)
      fieldBlocks.add(new Vector3d(x, y, z))

    return fieldBlocks
  }

  override def isInField(projector: IFieldMatrix, position: Vector3d): Boolean =
  {
    val projectorPos: Vector3d = new Vector3d(projector.asInstanceOf[TileEntity])
    projectorPos.add(projector.getTranslation)
    val relativePosition = position.clone.subtract(projectorPos)
    relativePosition.transform(new EulerAngle(-projector.getRotationYaw, -projector.getRotationPitch, 0))
    val region = new Cuboid(-projector.getNegativeScale, projector.getPositiveScale)
    return region.intersects(relativePosition)
  }

  @SideOnly(Side.CLIENT)
  override def render(projector: IProjector, x: Double, y: Double, z: Double, f: Float, ticks: Long)
  {
    GL11.glScalef(0.5f, 0.5f, 0.5f)
    ModelCube.INSTNACE.render
  }
}