package resonantinduction.archaic.fluid.gutter

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.IModelCustom
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.IFluidTank
import org.lwjgl.opengl.GL11
import resonant.api.recipe.MachineRecipes
import resonant.api.recipe.RecipeResource
import resonant.content.factory.resources.RecipeType
import resonant.lib.render.FluidRenderUtility
import resonant.lib.render.RenderUtility
import resonant.lib.utility.FluidUtility
import resonant.lib.utility.WorldUtility
import resonant.lib.utility.inventory.InventoryUtility
import resonantinduction.core.Reference
import resonantinduction.core.prefab.node.TilePressureNode
import universalelectricity.core.transform.region.Cuboid
import universalelectricity.core.transform.vector.Vector3
import java.util.ArrayList
import java.util.List
object TileGutter
{
    @SideOnly(Side.CLIENT) private[gutter] var MODEL: IModelCustom = _;
    @SideOnly(Side.CLIENT) private[gutter] var TEXTURE: ResourceLocation = _;
}
/**
 * The gutter, used for fluid transfer.
 *
 * @author Calclavia
 */
class TileGutter extends TilePressureNode(Material.rock)
{



    //Constructor
    tankNode_$eq(new FluidGravityNode(this))
    setTextureName("material_wood_surface")
    isOpaqueCube(false)
    normalRender(false)
    bounds(new Cuboid(0, 0, 0, 1, 0.99, 1))

    override def getCollisionBoxes: java.lang.Iterable[Cuboid] =
    {
        val list: List[Cuboid] = new ArrayList[Cuboid]
        val thickness: Float = 0.1F
        if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.DOWN))
        {
            list.add(new Cuboid(0.0F, 0.0F, 0.0F, 1.0F, thickness, 1.0F))
        }
        if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.WEST))
        {
            list.add(new Cuboid(0.0F, 0.0F, 0.0F, thickness, 1.0F, 1.0F))
        }
        if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.NORTH))
        {
            list.add(new Cuboid(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, thickness))
        }
        if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.EAST))
        {
            list.add(new Cuboid(1.0F - thickness, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F))
        }
        if (!WorldUtility.isEnabledSide(renderSides, ForgeDirection.SOUTH))
        {
            list.add(new Cuboid(0.0F, 0.0F, 1.0F - thickness, 1.0F, 1.0F, 1.0F))
        }
        return list
    }

    override def collide(entity: Entity)
    {
        if (getTank.getFluidAmount > 0)
        {
            {
                var i: Int = 2
                while (i < 6)
                {
                    {
                        val dir: ForgeDirection = ForgeDirection.getOrientation(i)
                        val pressure: Int = getPressure(dir)
                        val position: Vector3 = position.add(dir)
                        val checkTile: TileEntity = position.getTileEntity(world)
                        if (checkTile.isInstanceOf[TileGutter])
                        {
                            val deltaPressure: Int = pressure - (checkTile.asInstanceOf[TileGutter]).getPressure(dir.getOpposite)
                            entity.motionX += 0.01 * dir.offsetX * deltaPressure
                            entity.motionY += 0.01 * dir.offsetY * deltaPressure
                            entity.motionZ += 0.01 * dir.offsetZ * deltaPressure
                        }
                    }
                    ({
                        i += 1; i - 1
                    })
                }
            }
            if (getTank.getFluid.getFluid.getTemperature >= 373)
            {
                entity.setFire(5)
            }
            else
            {
                entity.extinguish
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
                    val _drain: FluidStack = drain(ForgeDirection.UP, drainAmount, false)
                    if (_drain != null && _drain.amount > 0 && world.rand.nextFloat > 0.9)
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
                    drain(ForgeDirection.UP, drainAmount, true)
                    world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "liquid.water", 0.5f, 1)
                }
                return true
            }
        }
        return true
    }

    override def onFillRain
    {
        if (!world.isRemote)
        {
            fill(ForgeDirection.UP, new FluidStack(FluidRegistry.WATER, 10), true)
        }
    }

    override def renderDynamic(position: Vector3, frame: Float, pass: Int)
    {
        if (TileGutter.MODEL == null)
        {
            TileGutter.MODEL = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "gutter.tcn"))
        }
        if (TileGutter.TEXTURE == null)
        {
            TileGutter.TEXTURE = new ResourceLocation(Reference.domain, Reference.modelPath + "gutter.png")
        }
        GL11.glPushMatrix
        GL11.glTranslated(position.x + 0.5, position.y + 0.5, position.z + 0.5)
        val liquid: FluidStack = getTank.getFluid
        val capacity: Int = getTank.getCapacity
        render(0, renderSides)
        if (world != null)
        {
            val tank: IFluidTank = getTank
            val percentageFilled: Double = tank.getFluidAmount.asInstanceOf[Double] / tank.getCapacity.asInstanceOf[Double]
            if (percentageFilled > 0.1)
            {
                GL11.glPushMatrix
                GL11.glScaled(0.990, 0.99, 0.990)
                val ySouthEast: Double = FluidUtility.getAveragePercentageFilledForSides(classOf[TileGutter], percentageFilled, world, position, ForgeDirection.SOUTH, ForgeDirection.EAST)
                val yNorthEast: Double = FluidUtility.getAveragePercentageFilledForSides(classOf[TileGutter], percentageFilled, world, position, ForgeDirection.NORTH, ForgeDirection.EAST)
                val ySouthWest: Double = FluidUtility.getAveragePercentageFilledForSides(classOf[TileGutter], percentageFilled, world, position, ForgeDirection.SOUTH, ForgeDirection.WEST)
                val yNorthWest: Double = FluidUtility.getAveragePercentageFilledForSides(classOf[TileGutter], percentageFilled, world, position, ForgeDirection.NORTH, ForgeDirection.WEST)
                FluidRenderUtility.renderFluidTesselation(tank, ySouthEast, yNorthEast, ySouthWest, yNorthWest)
                GL11.glPopMatrix
            }
        }
        GL11.glPopMatrix
    }

    def render(meta: Int, sides: Byte)
    {
        RenderUtility.bind(TileGutter.TEXTURE)
        val thickness: Double = 0.055
        val height: Double = 0.5
        for (dir <- ForgeDirection.VALID_DIRECTIONS)
        {
            if (dir != ForgeDirection.UP && dir != ForgeDirection.DOWN)
            {
                GL11.glPushMatrix
                RenderUtility.rotateBlockBasedOnDirection(dir)
                if (WorldUtility.isEnabledSide(sides, ForgeDirection.DOWN))
                {
                    GL11.glTranslatef(0, -0.075f, 0)
                    GL11.glScalef(1, 1.15f, 1)
                }
                if (!WorldUtility.isEnabledSide(sides, dir))
                {
                    TileGutter.MODEL.renderOnly("left")
                }
                if (!WorldUtility.isEnabledSide(sides, dir) || !WorldUtility.isEnabledSide(sides, dir.getRotation(ForgeDirection.UP)))
                {
                    TileGutter.MODEL.renderOnly("backCornerL")
                }
                GL11.glPopMatrix
            }
        }
        if (!WorldUtility.isEnabledSide(sides, ForgeDirection.DOWN))
        {
            TileGutter.MODEL.renderOnly("base")
        }
    }
}