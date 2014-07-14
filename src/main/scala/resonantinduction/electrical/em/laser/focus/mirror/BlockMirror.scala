package resonantinduction.electrical.em.laser.focus.mirror

import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import resonantinduction.electrical.em.{TabEC, Vector3, ElectromagneticCoherence}
import net.minecraft.world.World
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import resonantinduction.electrical.em.laser.BlockRenderingHandler
import net.minecraftforge.common.util.ForgeDirection
import cpw.mods.fml.relauncher.{Side, SideOnly}
import resonantinduction.electrical.em.laser.focus.{BlockFocusBase, IFocus, ItemFocusingMatrix}

/**
 * @author Calclavia
 */
class BlockMirror extends BlockFocusBase(Material.rock)
{
  setBlockName(ElectromagneticCoherence.PREFIX + "mirror")
  setBlockTextureName("stone")
  setCreativeTab(TabEC)


  override def createNewTileEntity(world: World, metadata: Int): TileEntity =
  {
    return new TileMirror()
  }

  @SideOnly(Side.CLIENT)
  override def getRenderType = BlockRenderingHandler.getRenderId

  override def renderAsNormalBlock = false

  override def isOpaqueCube = false
}

