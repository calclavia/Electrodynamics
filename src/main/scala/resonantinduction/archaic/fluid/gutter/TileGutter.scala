package resonantinduction.archaic.fluid.gutter

import java.util.{ArrayList, List}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack, IFluidTank}
import org.lwjgl.opengl.GL11
import resonant.api.recipe.{MachineRecipes, RecipeResource}
import resonant.content.factory.resources.RecipeType
import resonant.lib.render.{FluidRenderUtility, RenderUtility}
import resonant.lib.transform.region.Cuboid
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.FluidUtility
import resonant.lib.utility.inventory.InventoryUtility
import resonant.lib.wrapper.BitmaskWrapper._
import resonantinduction.core.Reference
import resonantinduction.core.prefab.node.{NodeFluidPressure, TileFluidProvider}

object TileGutter
{
  @SideOnly(Side.CLIENT)
  private val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "gutter.tcn"))
  @SideOnly(Side.CLIENT)
  private val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "gutter.png")
}

/**
 * The gutter, used for fluid transfer.
 *
 * @author Calclavia
 */
class TileGutter extends TileFluidProvider(Material.rock)
{
  fluidNode = new NodeFluidGravity(this)
  {
    override def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
    {
      return from != ForgeDirection.UP && !fluid.isGaseous
    }

    override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
    {
      return from != ForgeDirection.UP && !fluid.isGaseous
    }

    override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
    {
      if (!resource.getFluid.isGaseous)
      {
        return super.fill(from, resource, doFill)
      }
      return 0
    }
  }

  textureName = "material_wood_surface"
  isOpaqueCube = false
  normalRender = false
  bounds = new Cuboid(0, 0, 0, 1, 0.99, 1)

  override def getCollisionBoxes: java.lang.Iterable[Cuboid] =
  {
    val list: List[Cuboid] = new ArrayList[Cuboid]
    val thickness = 0.1f

    if (!clientRenderMask.mask(ForgeDirection.DOWN))
    {
      list.add(new Cuboid(0.0F, 0.0F, 0.0F, 1.0F, thickness, 1.0F))
    }
    if (!clientRenderMask.mask(ForgeDirection.WEST))
    {
      list.add(new Cuboid(0.0F, 0.0F, 0.0F, thickness, 1.0F, 1.0F))
    }
    if (!clientRenderMask.mask(ForgeDirection.NORTH))
    {
      list.add(new Cuboid(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, thickness))
    }
    if (!clientRenderMask.mask(ForgeDirection.EAST))
    {
      list.add(new Cuboid(1.0F - thickness, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F))
    }
    if (!clientRenderMask.mask(ForgeDirection.SOUTH))
    {
      list.add(new Cuboid(0.0F, 0.0F, 1.0F - thickness, 1.0F, 1.0F, 1.0F))
    }
    return list
  }

  override def collide(entity: Entity)
  {
    if (fluidNode.getFluidAmount > 0)
    {
      for (i <- 2 to 6)
      {
        val dir: ForgeDirection = ForgeDirection.getOrientation(i)
        val pressure: Int = fluidNode.asInstanceOf[NodeFluidPressure].pressure(dir)
        val pos: Vector3 = toVector3.add(dir)
        val checkTile: TileEntity = pos.getTileEntity(world)

        if (checkTile.isInstanceOf[TileGutter])
        {
          val deltaPressure: Int = pressure - checkTile.asInstanceOf[TileGutter].fluidNode.asInstanceOf[NodeFluidPressure].pressure(dir.getOpposite)
          entity.motionX += 0.01 * dir.offsetX * deltaPressure
          entity.motionY += 0.01 * dir.offsetY * deltaPressure
          entity.motionZ += 0.01 * dir.offsetZ * deltaPressure
        }
      }
      if (fluidNode.getFluid.getFluid.getTemperature >= 373)
      {
        entity.setFire(5)
      }
      else
      {
        entity.extinguish()
      }
    }
    if (entity.isInstanceOf[EntityItem])
    {
      entity.noClip = true
    }
  }

  override def activate(player: EntityPlayer, side: Int, vector3: Vector3): Boolean =
  {
    if (player.getCurrentEquippedItem != null)
    {
      var itemStack: ItemStack = player.getCurrentEquippedItem
      val outputs: Array[RecipeResource] = MachineRecipes.INSTANCE.getOutput(RecipeType.MIXER.name, itemStack)
      if (outputs.length > 0)
      {
        if (!world.isRemote)
        {
          val drainAmount: Int = 50 + world.rand.nextInt(50)
          val drain: FluidStack = fluidNode.drain(ForgeDirection.UP, drainAmount, false)

          if (drain != null && drain.amount > 0 && world.rand.nextFloat > 0.9)
          {
            if (world.rand.nextFloat > 0.1)
            {
              for (res <- outputs)
              {
                InventoryUtility.dropItemStack(world, new Vector3(player), res.getItemStack.copy, 0)
              }
            }
            itemStack.stackSize -= 1
            if (itemStack.stackSize <= 0)
            {
              itemStack = null
            }
            player.inventory.setInventorySlotContents(player.inventory.currentItem, itemStack)
          }

          fluidNode.drain(ForgeDirection.UP, drainAmount, true)
          world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "liquid.water", 0.5f, 1)
        }
        return true
      }

      FluidUtility.playerActivatedFluidItem(world, xi, yi, zi, player, side)
      return true
    }
    return true
  }

  override def onFillRain()
  {
    if (!world.isRemote)
    {
      fill(ForgeDirection.UP, new FluidStack(FluidRegistry.WATER, 10), true)
    }
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    render(0, 0x0)
  }

  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    GL11.glPushMatrix()
    GL11.glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

    render(0, clientRenderMask)

    if (world != null)
    {
      val tank: IFluidTank = fluidNode
      val percentageFilled = tank.getFluidAmount / tank.getCapacity.toDouble

      if (percentageFilled > 0.1)
      {
        GL11.glPushMatrix()
        GL11.glScaled(0.99, 0.99, 0.99)
        val ySouthEast = FluidUtility.getAveragePercentageFilledForSides(classOf[TileGutter], percentageFilled, world, toVectorWorld, ForgeDirection.SOUTH, ForgeDirection.EAST)
        val yNorthEast = FluidUtility.getAveragePercentageFilledForSides(classOf[TileGutter], percentageFilled, world, toVectorWorld, ForgeDirection.NORTH, ForgeDirection.EAST)
        val ySouthWest = FluidUtility.getAveragePercentageFilledForSides(classOf[TileGutter], percentageFilled, world, toVectorWorld, ForgeDirection.SOUTH, ForgeDirection.WEST)
        val yNorthWest = FluidUtility.getAveragePercentageFilledForSides(classOf[TileGutter], percentageFilled, world, toVectorWorld, ForgeDirection.NORTH, ForgeDirection.WEST)
        FluidRenderUtility.renderFluidTesselation(tank, ySouthEast, yNorthEast, ySouthWest, yNorthWest)
        println(ySouthEast, yNorthEast, ySouthWest, yNorthWest)
        GL11.glPopMatrix()
      }
    }

    GL11.glPopMatrix()
  }

  def render(meta: Int, sides: Int)
  {
    RenderUtility.bind(TileGutter.texture)

    for (dir <- ForgeDirection.VALID_DIRECTIONS)
    {
      if (dir != ForgeDirection.UP && dir != ForgeDirection.DOWN)
      {
        GL11.glPushMatrix()
        RenderUtility.rotateBlockBasedOnDirection(dir)

        if (sides.mask(ForgeDirection.DOWN))
        {
          GL11.glTranslatef(0, -0.075f, 0)
          GL11.glScalef(1, 1.15f, 1)
        }
        if (!sides.mask(dir))
        {
          TileGutter.model.renderOnly("left")
        }
        if (!sides.mask(dir) || !sides.mask(dir.getRotation(ForgeDirection.UP)))
        {
          TileGutter.model.renderOnly("backCornerL")
        }
        GL11.glPopMatrix()
      }
    }

    if (!sides.mask(ForgeDirection.DOWN))
    {
      TileGutter.model.renderOnly("base")
    }
  }
}