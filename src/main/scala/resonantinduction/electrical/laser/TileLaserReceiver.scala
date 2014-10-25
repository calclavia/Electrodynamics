package resonantinduction.electrical.laser

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.BlockPistonBase
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.{MovingObjectPosition, ResourceLocation}
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11._
import resonant.content.prefab.java.TileAdvanced
import resonant.lib.render.RenderUtility
import resonantinduction.core.Reference
import universalelectricity.core.transform.vector.Vector3

/**
 * A block that receives laser light and generates a voltage.
 * @author Calclavia
 */
object TileLaserReceiver
{
  @SideOnly(Side.CLIENT) val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "laserReceiver.tcn"))
  @SideOnly(Side.CLIENT) val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "laserReceiver.png")
}

class TileLaserReceiver extends TileAdvanced(Material.rock) with ILaserHandler
{
  var redstoneValue = 0
  private var energy = 0D
  private var prevRedstoneValue = 0;

  domain = ""
  textureName = "stone"
  normalRender = false
  isOpaqueCube = false

  override def update()
  {
    if (energy > 0)
    {
      redstoneValue = Math.min(Math.ceil(energy / (Laser.maxEnergy / 15)), 15).toInt

      if (redstoneValue != prevRedstoneValue)
      {
        world.notifyBlocksOfNeighborChange(xi, yi, zi, getBlockType)
        ForgeDirection.VALID_DIRECTIONS.foreach(dir => world.notifyBlocksOfNeighborChange(xi + dir.offsetX, yi + dir.offsetY, zi + dir.offsetZ, getBlockType))
        prevRedstoneValue = redstoneValue
      }

      energy = 0
    }
    else
    {
      redstoneValue = 0

      if (redstoneValue != prevRedstoneValue)
      {
        world.notifyBlocksOfNeighborChange(xi, yi, zi, getBlockType)
        ForgeDirection.VALID_DIRECTIONS.foreach(dir => world.notifyBlocksOfNeighborChange(xi + dir.offsetX, yi + dir.offsetY, zi + dir.offsetZ, getBlockType))
        prevRedstoneValue = redstoneValue
      }
    }
  }

  override def onLaserHit(renderStart: Vector3, incident: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double): Boolean =
  {
    if (hit.sideHit == getDirection.ordinal)
    {
      this.energy += energy
    }

    return false
  }

  override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
  {
    val l = BlockPistonBase.determineOrientation(world, xi, yi, zi, entityLiving)
    world.setBlockMetadataWithNotify(xi, yi, zi, l, 2)
  }
  override def getLightValue(access: IBlockAccess): Int =redstoneValue

  override def getWeakRedstonePower(access: IBlockAccess, side: Int): Int = getStrongRedstonePower(access, side)

  override def getStrongRedstonePower(access: IBlockAccess, side: Int): Int = redstoneValue

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
