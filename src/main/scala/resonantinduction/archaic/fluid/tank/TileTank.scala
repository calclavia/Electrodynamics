package resonantinduction.archaic.fluid.tank

import java.awt.Color
import java.util.{ArrayList, List}

import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{IFluidTank, FluidContainerRegistry, FluidStack, FluidTank}
import org.lwjgl.opengl.GL11
import resonant.api.IRemovable.ISneakPickup
import resonant.content.prefab.scala.render.ISimpleItemRenderer
import resonant.lib.render.{FluidRenderUtility, RenderUtility}
import resonant.lib.utility.FluidUtility
import resonant.lib.utility.render.RenderBlockUtility
import resonantinduction.archaic.ArchaicBlocks
import resonantinduction.core.Reference
import resonantinduction.core.prefab.node.TileTankNode
import universalelectricity.core.transform.vector.Vector3

/**
 * Tile/Block class for basic Dynamic tanks
 *
 * @author Darkguardsman
 */
object TileTank {
  final val VOLUME: Int = 16

  object ItemRenderer {
    var instance: TileTank.ItemRenderer = new TileTank.ItemRenderer
  }

  class ItemRenderer extends ISimpleItemRenderer {
    def renderTank(x: Double, y: Double, z: Double, fluid: FluidStack, capacity: Int) {
      val tank: FluidTank = new FluidTank(fluid, capacity)
      GL11.glPushMatrix
      GL11.glTranslated(0.02, 0.02, 0.02)
      GL11.glScaled(0.92, 0.92, 0.92)
      if (fluid != null) {
        GL11.glPushMatrix
        if (!fluid.getFluid.isGaseous) {
          val percentageFilled: Double = tank.getFluidAmount.asInstanceOf[Double] / tank.getCapacity.asInstanceOf[Double]
          FluidRenderUtility.renderFluidTesselation(tank, percentageFilled, percentageFilled, percentageFilled, percentageFilled)
        }
        else {
          val filledPercentage: Double = fluid.amount.asInstanceOf[Double] / capacity.asInstanceOf[Double]
          GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
          GL11.glEnable(GL11.GL_CULL_FACE)
          GL11.glDisable(GL11.GL_LIGHTING)
          GL11.glEnable(GL11.GL_BLEND)
          GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
          val color: Color = new Color(fluid.getFluid.getColor)
          RenderUtility.enableBlending
          GL11.glColor4d(color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f, if (fluid.getFluid.isGaseous) filledPercentage else 1)
          RenderUtility.bind(FluidRenderUtility.getFluidSheet(fluid))
          FluidRenderUtility.renderFluidTesselation(tank, 1, 1, 1, 1)
          RenderUtility.disableBlending
          GL11.glPopAttrib
        }
        GL11.glPopMatrix
      }
      GL11.glPopMatrix
    }

    def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*) {
      GL11.glPushMatrix
      RenderBlockUtility.tessellateBlockWithConnectedTextures(itemStack.getItemDamage, ArchaicBlocks.blockTank, null, RenderUtility.getIcon(Reference.prefix + "tankEdge"))
      GL11.glPopMatrix
      GL11.glPushMatrix
      if (itemStack.getTagCompound != null && itemStack.getTagCompound.hasKey("fluid")) {
        renderTank(0, 0, 0, FluidStack.loadFluidStackFromNBT(itemStack.getTagCompound.getCompoundTag("fluid")), VOLUME * FluidContainerRegistry.BUCKET_VOLUME)
      }
      GL11.glPopMatrix
    }
  }

}

class TileTank extends TileTankNode(Material.iron) with ISneakPickup {

    isOpaqueCube(false)
    normalRender(false)
    forceStandardRender(true)
    itemBlock(classOf[ItemBlockTank])
    setCapacity(16 * FluidContainerRegistry.BUCKET_VOLUME)

  override def shouldSideBeRendered(access: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean = {
    return access.getBlock(x, y, z) ne getBlockType
  }

  override def use(player: EntityPlayer, side: Int, vector3: Vector3): Boolean = {
    if (!world.isRemote) {
      return FluidUtility.playerActivatedFluidItem(world, x, y, z, player, side)
    }
    return true
  }

  override def getLightValue(access: IBlockAccess): Int = {
    if (getFluid.getFluid != null) {
      return getFluid.getFluid.getLuminosity
    }
    return super.getLightValue(access)
  }

  def renderTank(x: Double, y: Double, z: Double, fluid: FluidStack) {
    if (world != null) {
      GL11.glPushMatrix
      GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
      if (fluid != null) {
        GL11.glPushMatrix
        if (!fluid.getFluid.isGaseous) {
          GL11.glScaled(0.99, 0.99, 0.99)
          val tank: IFluidTank = getTank
          val percentageFilled: Double = tank.getFluidAmount.asInstanceOf[Double] / tank.getCapacity.asInstanceOf[Double]
          val ySouthEast: Double = FluidUtility.getAveragePercentageFilledForSides(classOf[TileTank], percentageFilled, world, new Vector3(this), ForgeDirection.SOUTH, ForgeDirection.EAST)
          val yNorthEast: Double = FluidUtility.getAveragePercentageFilledForSides(classOf[TileTank], percentageFilled, world, new Vector3(this), ForgeDirection.NORTH, ForgeDirection.EAST)
          val ySouthWest: Double = FluidUtility.getAveragePercentageFilledForSides(classOf[TileTank], percentageFilled, world, new Vector3(this), ForgeDirection.SOUTH, ForgeDirection.WEST)
          val yNorthWest: Double = FluidUtility.getAveragePercentageFilledForSides(classOf[TileTank], percentageFilled, world, new Vector3(this), ForgeDirection.NORTH, ForgeDirection.WEST)
          FluidRenderUtility.renderFluidTesselation(tank, ySouthEast, yNorthEast, ySouthWest, yNorthWest)
        }
        GL11.glPopMatrix
      }
      GL11.glPopMatrix
    }
  }

  override def renderDynamic(position: Vector3, frame: Float, pass: Int) {
    renderTank(position.x, position.y, position.z, getFluid)
  }

  def getRemovedItems(entity: EntityPlayer): List[ItemStack] = {
    val drops: List[ItemStack] = new ArrayList[ItemStack]
    val itemStack: ItemStack = new ItemStack(ArchaicBlocks.blockTank, 1, 0)
    if (itemStack != null) {
      if (getTank != null && getTank.getFluid != null) {
        val stack: FluidStack = getTank.getFluid
        if (stack != null) {
          if (itemStack.getTagCompound == null) {
            itemStack.setTagCompound(new NBTTagCompound)
          }
          drain(ForgeDirection.UNKNOWN, stack.amount, false)
          itemStack.getTagCompound.setTag("fluid", stack.writeToNBT(new NBTTagCompound))
        }
      }
      drops.add(itemStack)
    }
    return drops
  }
}