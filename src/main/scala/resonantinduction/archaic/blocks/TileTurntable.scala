package resonantinduction.archaic.blocks

import codechicken.multipart.TileMultipart
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.util.IIcon
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.IRotatable
import resonant.content.prefab.RenderRotatedTexture
import resonant.content.spatial.block.SpatialBlock
import resonant.lib.content.prefab.TRotatable
import resonantinduction.core.Reference
import universalelectricity.core.transform.vector.Vector3

object TileTurntable
{
  var top: IIcon = null
}

class TileTurntable extends SpatialBlock(Material.piston) with TRotatable with RenderRotatedTexture
{
  textureName = "turntable_side"
  tickRandomly = true
  rotationMask = Integer.parseInt("111111", 2).toByte

  override def tickRate(par1World: World): Int = 5

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconReg: IIconRegister)
  {
    super.registerIcons(iconReg)
    TileTurntable.top = iconReg.registerIcon(Reference.prefix + "turntable")
  }

  override def update()
  {
    updateTurntableState(world, x, y, z)
  }

  private def updateTurntableState(world: World, x: Int, y: Int, z: Int)
  {
    if (world.isBlockIndirectlyGettingPowered(x, y, z))
    {
      try
      {
        val facing: ForgeDirection = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z))
        val position = new Vector3(x, y, z) + facing
        val tileEntity = position.getTileEntity(world)
        val block = position.getBlock(world)

        if (!(tileEntity.isInstanceOf[TileMultipart]))
        {
          if (tileEntity.isInstanceOf[IRotatable])
          {
            val blockRotation = tileEntity.asInstanceOf[IRotatable].getDirection
            tileEntity.asInstanceOf[IRotatable].setDirection(blockRotation.getRotation(facing.getOpposite))
          }
          else if (block != null)
          {
            block.rotateBlock(world, position.xi, position.yi, position.zi, facing.getOpposite)
          }

          world.markBlockForUpdate(position.xi, position.yi, position.zi)
          world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "tile.piston.in", 0.5F, world.rand.nextFloat * 0.15F + 0.6F)
        }
      }
      catch
        {
          case e: Exception =>
          {
            System.out.println("Error while rotating a block near " + x + "x " + y + "y " + z + "z " + (if (world != null && world.provider != null) world.provider.dimensionId + "d" else "null:world"))
            e.printStackTrace
          }
        }
    }
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(access: IBlockAccess, side: Int): IIcon =
  {
    if (side == metadata)
    {
      return TileTurntable.top
    }

    return getIcon
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == 1)
    {
      return TileTurntable.top
    }
    return getIcon
  }

  override def onNeighborChanged(block: Block)
  {
    scheduleTick(10)
  }
}
