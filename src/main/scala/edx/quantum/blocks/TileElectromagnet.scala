package edx.quantum.blocks

import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11._
import resonantengine.api.tile.IElectromagnet
import resonantengine.lib.modcontent.block.ResonantBlock
import resonantengine.lib.render.{RenderBlockUtility, RenderUtility}
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.WorldUtility
import resonantengine.lib.wrapper.CollectionWrapper._
import resonantengine.prefab.block.itemblock.ItemBlockMetadata

/**
 * Electromagnet block
 */
class TileElectromagnet extends ResonantBlock(Material.iron) with IElectromagnet
{
  private val edgeTexture: String = "stone"
  private var iconTop: IIcon = null
  private var iconGlass: IIcon = null

  //Constructor
  blockResistance = 20
  forceItemToRenderAsBlock = true
  normalRender = false
  isOpaqueCube = false
  renderStaticBlock = true
  this.itemBlock(classOf[ItemBlockMetadata])

  override def getIcon(side: Int, metadata: Int): IIcon =
  {
    if (metadata == 1)
    {
      return iconGlass
    }
    if (side == 0 || side == 1)
    {
      return iconTop
    }
    return super.getIcon(side, metadata)
  }

  @SideOnly(Side.CLIENT) override def registerIcons(iconRegister: IIconRegister)
  {
    super.registerIcons(iconRegister)
    iconTop = iconRegister.registerIcon(domain + textureName + "_top")
    iconGlass = iconRegister.registerIcon(domain + "electromagnetGlass")
  }

  override def metadataDropped(meta: Int, fortune: Int): Int =
  {
    return meta
  }

  override def canUpdate: Boolean =
  {
    return false
  }

  override def shouldSideBeRendered(access: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean =
  {
    if (access.getBlockMetadata(x, y, z) != 1)
    {
      return super.shouldSideBeRendered(access, x, y, z, side)
    }
    return true
  }

  override def getRenderBlockPass: Int =
  {
    return 0
  }

  override def getSubBlocks(item: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
  {
    super.getSubBlocks(item, par2CreativeTabs, par3List)
    par3List.add(new ItemStack(item, 1, 1))
  }

  def isRunning: Boolean =
  {
    return true
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    if (itemStack != null)
    {
      glPushMatrix()
      glTranslated(0.5, 0.5, 0.5)
      RenderBlockUtility.tessellateBlockWithConnectedTextures(itemStack.getItemDamage(), block, null, RenderUtility.getIcon(edgeTexture))
      glPopMatrix()
    }
    else
    {
      super.renderInventory(itemStack)
    }
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    var sideMap = 0

    for (dir <- ForgeDirection.VALID_DIRECTIONS)
    {
      val check = toVector3 + dir
      val checkTile = check.getTileEntity(world)

      if (checkTile != null && checkTile.getClass == tile.getClass && check.getBlockMetadata(world) == tile.getBlockMetadata)
      {
        sideMap = WorldUtility.setEnableSide(sideMap, dir, true)
      }
    }

    RenderBlockUtility.tessellateBlockWithConnectedTextures(sideMap, world, xi, yi, zi, tile.getBlockType, null, RenderUtility.getIcon(edgeTexture))
  }

}