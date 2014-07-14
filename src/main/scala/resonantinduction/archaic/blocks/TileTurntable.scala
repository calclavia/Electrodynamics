package resonantinduction.archaic.blocks

import codechicken.multipart.TileMultipart
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IconRegister
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Icon
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.ForgeDirection
import resonant.api.IRotatable
import resonant.api.blocks.IRotatableBlock
import resonant.api.blocks.IRotatableBlock
import resonant.lib.content.module.TileBlock
import resonant.lib.content.module.TileRender
import resonant.lib.render.RotatedTextureRenderer
import resonantinduction.core.Reference
import universalelectricity.api.vector.Vector3
import cpw.mods.fml.relauncher.Side

class TileTurntable extends TileBlock(Material.piston) with IRotatable {
  textureName = "turntable_side"
  tickRandomly = true
  rotationMask = Integer.parseInt("111111", 2).toByte

  override def tickRate(par1World: World): Int =
    {
      return 5
    }

  @SideOnly(Side.CLIENT) override def registerIcons(iconReg: IconRegister) {
    super.registerIcons(iconReg)
    TileTurntable.top = iconReg.registerIcon(Reference.PREFIX + "turntable")
  }

  override def updateEntity() {
    updateTurntableState(world, x, y, z)
  }

  @SideOnly(Side.CLIENT) override def getIcon(access: IBlockAccess, side: Int): Icon =
    {
      if (side == super.metadata()) {
        return TileTurntable.top
      }

      return getIcon
    }

  @SideOnly(Side.CLIENT) override def getIcon(side: Int, meta: Int): Icon =
    {
      if (side == 1) {
        return TileTurntable.top
      }
      return getIcon
    }

  override def onNeighborChanged() {
    scheduelTick(10)
  }

  private def updateTurntableState(world: World, x: Int, y: Int, z: Int) {
    if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
      try {
        val facing: ForgeDirection = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z))
        val position: Vector3 = new Vector3(x, y, z).translate(facing)
        val tileEntity: TileEntity = position.getTileEntity(world)
        val block: Block = Block.blocksList(position.getBlockID(world))
        if (!(tileEntity.isInstanceOf[TileMultipart])) {
          if (tileEntity.isInstanceOf[IRotatable]) {
            val blockRotation: ForgeDirection = (tileEntity.asInstanceOf[IRotatable]).getDirection
            (tileEntity.asInstanceOf[IRotatable]).setDirection(blockRotation.getRotation(facing.getOpposite))
          } else if (block.isInstanceOf[IRotatableBlock]) {
            val blockRotation: ForgeDirection = (block.asInstanceOf[IRotatableBlock]).getDirection(world, position.intX, position.intY, position.intZ)
            (block.asInstanceOf[IRotatableBlock]).setDirection(world, position.intX, position.intY, position.intZ, blockRotation.getRotation(facing.getOpposite))
          } else if (block != null) {
            Block.blocksList(blockID).rotateBlock(world, position.intX, position.intY, position.intZ, facing.getOpposite)
          }
          world.markBlockForUpdate(position.intX, position.intY, position.intZ)
          world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "tile.piston.in", 0.5F, world.rand.nextFloat * 0.15F + 0.6F)
        }
      } catch {
        case e: Exception =>
          {
            System.out.println("Error while rotating a block near " + x + "x " + y + "y " + z + "z " + (if (world != null && world.provider != null) world.provider.dimensionId + "d" else "null:world"))
            e.printStackTrace
          }
      }
    }
  }

  @SideOnly(Side.CLIENT) protected override def newRenderer: TileRender =
    {
      return new RotatedTextureRenderer(this)
    }
}

object TileTurntable {
  var top: Icon = null
}