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
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._
import resonant.lib.prefab.tile.spatial.SpatialTile
import resonant.lib.prefab.tile.traits.TRotatable
import resonant.lib.render.RenderUtility
import resonant.lib.transform.vector.Vector3
import resonantinduction.core.Reference

/**
 * An emitter that shoots out lasers.
 *
 * Consider: E=hf. Higher frequency light has more energy.
 *
 * @author Calclavia
 */
object TileLaserEmitter
{
  @SideOnly(Side.CLIENT) val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "laserEmitter.tcn"))
  @SideOnly(Side.CLIENT) val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "laserEmitter.png")
}

class TileLaserEmitter extends SpatialTile(Material.iron) with ILaserHandler with TRotatable
{
  var energy = 0D

  domain = ""
  textureName = "stone"
  normalRender = false
  isOpaqueCube = false

  /**
   * Called when the block is placed by a living entity
   * @param entityLiving - entity who placed the block
   * @param itemStack - ItemStack the entity used to place the block
   */
  override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
  {
    val l = BlockPistonBase.determineOrientation(world, xi, yi, zi, entityLiving)
    world.setBlockMetadataWithNotify(xi, yi, zi, l, 2)
  }

  override def getLightValue(access: IBlockAccess): Int = ((energy / Laser.maxEnergy) * 15).toInt

  override def onLaserHit(renderStart: Vector3, incidentDirection: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double) = false

  override def update()
  {
    if (isIndirectlyPowered)
    {
      energy += world.getStrongestIndirectPower(xi, yi, zi) * (Laser.maxEnergy / 15)
    }

    if (energy > 0)
    {
      Laser.spawn(worldObj, toVector3 + 0.5 + new Vector3(getDirection) * 0.51, toVector3 + new Vector3(getDirection) * 0.6 + 0.5, new Vector3(getDirection), energy)
      energy = 0
    }
  }

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

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(TileLaserEmitter.texture)
    TileLaserEmitter.model.renderAll()

    RenderUtility.disableBlending()
    GL11.glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    glPushMatrix()
    glRotated(180, 0, 1, 0)

    RenderUtility.enableBlending()

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(TileLaserEmitter.texture)
    TileLaserEmitter.model.renderAll()

    RenderUtility.disableBlending()

    GL11.glPopMatrix()
  }
}
