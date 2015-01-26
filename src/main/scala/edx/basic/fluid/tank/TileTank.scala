package edx.basic.fluid.tank

import java.awt.Color
import java.util.{ArrayList, List}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.basic.BasicContent
import edx.basic.fluid.gutter.NodeFluidGravity
import edx.core.Reference
import edx.core.prefab.node.TileFluidProvider
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import org.lwjgl.opengl.GL11
import resonantengine.api.tile.IRemovable.ISneakPickup
import resonantengine.api.tile.node.INode
import resonantengine.lib.grid.core.Node
import resonantengine.lib.render.block.RenderConnectedTexture
import resonantengine.lib.render.{FluidRenderUtility, RenderBlockUtility, RenderUtility}
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.FluidUtility
import resonantengine.lib.wrapper.BitmaskWrapper._

/**
 * Tile/Block class for basic Dynamic tanks
 *
 * @author Darkguardsman
 */
class TileTank extends TileFluidProvider(Material.iron) with ISneakPickup with RenderConnectedTexture
{
  edgeTexture = Reference.prefix + "tankEdge"
  isOpaqueCube = false
  normalRender = false
  itemBlock = classOf[ItemBlockTank]

  fluidNode = new NodeFluidGravity(this, 16 * FluidContainerRegistry.BUCKET_VOLUME)
  {
    override def connect[B <: IFluidHandler](obj: B, dir: ForgeDirection)
    {
      super.connect(obj, dir)

      if (obj.isInstanceOf[INode] && !obj.asInstanceOf[Node].parent.isInstanceOf[TileTank])
        _connectedMask = _connectedMask.closeMask(dir)
    }

    override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
    {
      //Try to fill the current tank. If it fails, try to fill the tank above it.
      val result = super.fill(from, resource, doFill)

      if (result == 0)
      {
        val tile = (toVectorWorld + new Vector3(0, 1, 0)).getTileEntity

        if (tile.isInstanceOf[TileTank])
        {
          return tile.asInstanceOf[TileTank].fill(from, resource, doFill)
        }
      }

      return result
    }
  }

  fluidNode.asInstanceOf[NodeFluidGravity].maxFlowRate = FluidContainerRegistry.BUCKET_VOLUME
  fluidNode.asInstanceOf[NodeFluidGravity].doPressureUpdate = false
  fluidNode.onFluidChanged = () => if (!world.isRemote && !isInvalid) sendPacket(1)

  override def shouldSideBeRendered(access: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean = new Vector3(x, y, z).getBlock(access) != block

  override def use(player: EntityPlayer, side: Int, vector3: Vector3): Boolean =
  {
    if (!world.isRemote)
    {
      return FluidUtility.playerActivatedFluidItem(world, xi, yi, zi, player, side)
    }

    return true
  }

  override def getLightValue(access: IBlockAccess): Int =
  {
    if (fluidNode.getPrimaryTank.getFluid != null && fluidNode.getPrimaryTank.getFluid.getFluid != null)
    {
      return fluidNode.getPrimaryTank.getFluid.getFluid.getLuminosity
    }

    return super.getLightValue(access)
  }

  @SideOnly(Side.CLIENT)
  override def renderStatic(renderer: RenderBlocks, pos: Vector3, pass: Int): Boolean =
  {
    RenderBlockUtility.tessellateBlockWithConnectedTextures(clientRenderMask, world, pos.xi, pos.yi, pos.zi, tile.getBlockType, if (faceTexture != null) RenderUtility.getIcon(faceTexture) else null, RenderUtility.getIcon(edgeTexture))
    return true
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(position: Vector3, frame: Float, pass: Int)
  {
    renderTankFluid(position.x, position.y, position.z, fluidNode.getPrimaryTank.getFluid)
  }

  /**
   * Renders the fluid inside the tank
   */
  @SideOnly(Side.CLIENT)
  def renderTankFluid(x: Double, y: Double, z: Double, fluid: FluidStack)
  {
    if (world != null)
    {
      GL11.glPushMatrix()
      GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
      if (fluid != null)
      {
        GL11.glPushMatrix()
        if (!fluid.getFluid.isGaseous)
        {
          GL11.glScaled(0.99, 0.99, 0.99)
          val tank: IFluidTank = fluidNode.getPrimaryTank
          val percentageFilled: Double = tank.getFluidAmount.toDouble / tank.getCapacity.toDouble
          val ySouthEast = FluidUtility.getAveragePercentageFilledForSides(classOf[TileTank], percentageFilled, world, toVector3, ForgeDirection.SOUTH, ForgeDirection.EAST)
          val yNorthEast = FluidUtility.getAveragePercentageFilledForSides(classOf[TileTank], percentageFilled, world, toVector3, ForgeDirection.NORTH, ForgeDirection.EAST)
          val ySouthWest = FluidUtility.getAveragePercentageFilledForSides(classOf[TileTank], percentageFilled, world, toVector3, ForgeDirection.SOUTH, ForgeDirection.WEST)
          val yNorthWest = FluidUtility.getAveragePercentageFilledForSides(classOf[TileTank], percentageFilled, world, toVector3, ForgeDirection.NORTH, ForgeDirection.WEST)
          FluidRenderUtility.renderFluidTesselation(tank, ySouthEast, yNorthEast, ySouthWest, yNorthWest)
        }
        GL11.glPopMatrix()
      }
      GL11.glPopMatrix()
    }
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    super.renderInventory(itemStack)

    GL11.glPushMatrix()

    if (itemStack.getTagCompound != null && itemStack.getTagCompound.hasKey("fluid"))
    {
      renderInventoryFluid(0, 0, 0, FluidStack.loadFluidStackFromNBT(itemStack.getTagCompound.getCompoundTag("fluid")), fluidNode.getPrimaryTank.getCapacity)
    }

    GL11.glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  def renderInventoryFluid(x: Double, y: Double, z: Double, fluid: FluidStack, capacity: Int)
  {
    val tank = new FluidTank(fluid, capacity)

    GL11.glPushMatrix()
    GL11.glTranslated(0.02, 0.02, 0.02)
    GL11.glScaled(0.92, 0.92, 0.92)
    if (fluid != null)
    {
      GL11.glPushMatrix()
      if (!fluid.getFluid.isGaseous)
      {
        val percentageFilled: Double = tank.getFluidAmount.toDouble / tank.getCapacity.toDouble
        FluidRenderUtility.renderFluidTesselation(tank, percentageFilled, percentageFilled, percentageFilled, percentageFilled)
      }
      else
      {
        val filledPercentage: Double = fluid.amount.toDouble / capacity.toDouble
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        val color: Color = new Color(fluid.getFluid.getColor)
        RenderUtility.enableBlending()
        GL11.glColor4d(color.getRed / 255f, color.getGreen / 255f, color.getBlue / 255f, if (fluid.getFluid.isGaseous) filledPercentage else 1)
        RenderUtility.bind(FluidRenderUtility.getFluidSheet(fluid))
        FluidRenderUtility.renderFluidTesselation(tank, 1, 1, 1, 1)
        RenderUtility.disableBlending()
        GL11.glPopAttrib()
      }
      GL11.glPopMatrix()
    }
    GL11.glPopMatrix()
  }

  def getRemovedItems(entity: EntityPlayer): List[ItemStack] =
  {
    val drops = new ArrayList[ItemStack]
    val itemStack: ItemStack = new ItemStack(BasicContent.blockTank, 1, 0)
    if (itemStack != null)
    {
      if (fluidNode != null && fluidNode.getFluid != null)
      {
        val stack: FluidStack = fluidNode.getFluid
        if (stack != null)
        {
          if (itemStack.getTagCompound == null)
          {
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