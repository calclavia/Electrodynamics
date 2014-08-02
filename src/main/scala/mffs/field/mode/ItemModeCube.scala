package mffs.field.mode

import java.util.{HashSet, Set}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11
import resonant.api.mffs.machine.{IFieldMatrix, IProjector}
import resonant.lib.render.block.ModelCube
import universalelectricity.core.transform.region.Cuboid
import universalelectricity.core.transform.vector.Vector3

class ItemModeCube extends ItemMode
{
  private val step = 1

  def getExteriorPoints(projector: IFieldMatrix): Set[Vector3] =
  {
    val fieldBlocks = new HashSet[Vector3]
    val posScale: Vector3 = projector.getPositiveScale
    val negScale: Vector3 = projector.getNegativeScale

    for (x <- -negScale.xi to posScale.xi by step; y <- -negScale.yi to posScale.yi by step; z <- -negScale.zi to posScale.zi by step)
      if (y == -negScale.yi || y == posScale.yi || x == -negScale.xi || x == posScale.xi || z == -negScale.zi || z == posScale.zi)
        fieldBlocks.add(new Vector3(x, y, z))


    return fieldBlocks
  }

  def getInteriorPoints(projector: IFieldMatrix): Set[Vector3] =
  {
    val fieldBlocks = new HashSet[Vector3]
    val posScale = projector.getPositiveScale
    val negScale = projector.getNegativeScale

    //TODO: Check parallel possiblity
    for (x <- -negScale.xi to posScale.xi by step; y <- -negScale.yi to posScale.yi by step; z <- -negScale.zi to posScale.zi by step)
      fieldBlocks.add(new Vector3(x, y, z))

    return fieldBlocks
  }

  override def isInField(projector: IFieldMatrix, position: Vector3): Boolean =
  {
    val projectorPos: Vector3 = new Vector3(projector.asInstanceOf[TileEntity])
    projectorPos.add(projector.getTranslation)
    val relativePosition = position.clone.subtract(projectorPos)
    relativePosition.apply(new Rotation(-projector.getRotationYaw, -projector.getRotationPitch, 0))
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