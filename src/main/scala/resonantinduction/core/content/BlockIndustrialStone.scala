package resonantinduction.core.content

import java.util.List

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import resonant.content.spatial.block.SpatialBlock
import resonant.lib.wrapper.WrapList._
import resonantinduction.core.Reference

/**
 * A block used to build machines or decoration.
 *
 * @author Calclavia
 *
 */
object BlockIndustrialStone
{
  var iconNames = Array("material_stone_brick", "material_stone_brick2", "material_stone_chiseled", "material_stone_cobble", "material_stone_cracked", "material_stone", "material_stone_slab", "material_stone_mossy", "material_steel_dark", "material_steel_tint", "material_steel")
  var icons = new Array[IIcon](iconNames.length)
}

class BlockIndustrialStone extends SpatialBlock(Material.rock)
{
  blockHardness = 1
  stepSound = Block.soundStoneFootstep

  def damageDropped(par1: Int): Int =
  {
    return par1
  }

  override def getIcon(side: Int, metadata: Int): IIcon =
  {
    return icons(metadata)
  }

  override def registerIcons(register: IIconRegister)
  {
    super.registerIcons(register)
    (0 until BlockIndustrialStone.icons.size) foreach (i => BlockIndustrialStone.icons(i) = register.registerIcon(Reference.prefix + iconNames(i)))
  }

  def getSubBlocks(par1: Int, par2CreativeTabs: CreativeTabs, list: List[_])
  {
    (0 until iconNames.length) foreach (i => list.add(new ItemStack(par1, 1, i)))
  }

}