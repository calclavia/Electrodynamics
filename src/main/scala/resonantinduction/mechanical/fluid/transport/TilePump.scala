package resonantinduction.mechanical.fluid.transport

import java.util.{ArrayList, List}

import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTankInfo, IFluidHandler}
import org.lwjgl.opengl.GL11
import resonant.api.IRotatable
import resonant.api.grid.INode
import resonant.lib.render.RenderUtility
import resonant.lib.transform.vector.Vector3
import resonantinduction.core.Reference
import resonantinduction.mechanical.mech.TileMechanical

import scala.collection.mutable

object TilePump
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "pump.tcn"))
  val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "pump.png")
}

class TilePump extends TileMechanical(Material.iron) with IRotatable with IFluidHandler
{
  val pressureNode = new PumpNode(this)

  //Constructor
  normalRender = false
  isOpaqueCube = false
  setTextureName("material_steel")

  override def update()
  {
    super.update()

    if (!worldObj.isRemote && mechanicalNode.getPower > 0)
    {
      val tileIn: TileEntity = toVector3.add(getDirection.getOpposite).getTileEntity(this.worldObj)
      if (tileIn.isInstanceOf[IFluidHandler])
      {
        val drain: FluidStack = (tileIn.asInstanceOf[IFluidHandler]).drain(getDirection, pressureNode.getCapacity, false)
        if (drain != null)
        {
          (tileIn.asInstanceOf[IFluidHandler]).drain(getDirection, fill(getDirection.getOpposite, drain, true), true)
        }
      }
    }
  }

  override def renderDynamic(pos: Vector3, frame: Float, pass: Int): Unit =
  {
    GL11.glPushMatrix()
    GL11.glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
    GL11.glRotatef(-90, 0, 1, 0)
    if (tile.getWorldObj != null) RenderUtility.rotateBlockBasedOnDirection(getDirection)
    RenderUtility.bind(TilePump.texture)
    val notRendered = mutable.Set.empty[String]

    GL11.glPushMatrix
    GL11.glRotated(Math.toDegrees(mechanicalNode.angle.asInstanceOf[Float]), 0, 0, 1)

    for (i <- 1 to 12)
    {
      val fin: String = "fin" + i
      val innerFin: String = "innerFin" + i
      notRendered.add(fin)
      notRendered.add(innerFin)
      TilePump.model.renderOnly(fin, innerFin)
    }

    GL11.glPopMatrix()
    TilePump.model.renderAllExcept(notRendered.toArray[String]: _*)
    GL11.glPopMatrix()
  }

  def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    if (from eq getDirection.getOpposite)
    {
      val tileOut: TileEntity = toVector3.add(from.getOpposite).getTileEntity(this.worldObj)
      if (tileOut.isInstanceOf[IFluidHandler]) return (tileOut.asInstanceOf[IFluidHandler]).fill(from, resource, doFill)
    }
    return 0
  }

  def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    return null
  }

  def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    return null
  }

  def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return from eq this.getDirection.getOpposite
  }

  def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return from eq this.getDirection
  }

  def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return null
  }

  override def getNode[N <: INode](nodeType: Class[_ <: N], from: ForgeDirection): N =
  {
    if (nodeType.isAssignableFrom(pressureNode.getClass))
      return pressureNode.asInstanceOf[N]

    return null.asInstanceOf[N]
  }
}