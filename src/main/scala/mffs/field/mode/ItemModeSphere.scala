package mffs.field.mode

import java.util.{HashSet, Set}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.Content
import net.minecraft.tileentity.TileEntity
import nova.core.util.transform.Vector3d
import org.lwjgl.opengl.GL11
import resonantengine.api.mffs.machine.{IFieldMatrix, IProjector}
import resonantengine.lib.render.model.ModelCube
import resonantengine.nova.wrapper._

class ItemModeSphere extends ItemMode
{
  def getExteriorPoints(projector: IFieldMatrix): Set[Vector3d] =
  {
    val fieldBlocks = new HashSet[Vector3d]
    val radius = projector.getModuleCount(Content.moduleScale)
    val steps = Math.ceil(Math.PI / Math.atan(1.0D / radius / 2)).asInstanceOf[Int]

    for (phi_n <- 0 until 2 * steps; theta_n <- 0 until steps)
    {
      val phi = Math.PI * 2 / steps * phi_n
      val theta = Math.PI / steps * theta_n
      val point = new Vector3d(Math.sin(theta) * Math.cos(phi), Math.cos(theta), Math.sin(theta) * Math.sin(phi)) * radius
      fieldBlocks.add(point)
    }

    return fieldBlocks
  }

  def getInteriorPoints(projector: IFieldMatrix): Set[Vector3d] =
  {
    val fieldBlocks = new HashSet[Vector3d]
    val translation: Vector3d = projector.getTranslation
    val radius: Int = projector.getModuleCount(Content.moduleScale)

    for (x <- radius to radius; y <- -radius to radius; z <- -radius to radius)
    {
      val position = new Vector3d(x, y, z)
      if (isInField(projector, position + new Vector3d(projector.asInstanceOf[TileEntity]) + translation))
      {
        fieldBlocks.add(position)
      }
    }

    return fieldBlocks
  }

  override def isInField(projector: IFieldMatrix, position: Vector3d): Boolean =
  {
    return new Vector3d(projector.asInstanceOf[TileEntity]).add(projector.getTranslation).distance(position) < projector.getModuleCount(Content.moduleScale)
  }

  @SideOnly(Side.CLIENT) override def render(projector: IProjector, x1: Double, y1: Double, z1: Double, f: Float, ticks: Long)
  {
    val scale = 0.2f
    GL11.glScalef(scale, scale, scale)
    val radius = 0.8f
    val steps: Int = Math.ceil(Math.PI / Math.atan(1.0D / radius / 2)).asInstanceOf[Int]

    for (phi_n <- 0 until 2 * steps; theta_n <- 0 until steps)
    {
      val phi = Math.PI * 2 / steps * phi_n
      val theta = Math.PI / steps * theta_n
      val vector = new Vector3d(Math.sin(theta) * Math.cos(phi), Math.cos(theta), Math.sin(theta) * Math.sin(phi))
      vector * radius
      GL11.glTranslated(vector.x, vector.y, vector.z)
      ModelCube.INSTNACE.render
      GL11.glTranslated(-vector.x, -vector.y, -vector.z)
    }
  }
}