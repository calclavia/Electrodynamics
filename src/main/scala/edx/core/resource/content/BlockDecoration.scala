package edx.core.resource.content

import java.util.List

import edx.core.Reference
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.IIcon
import resonantengine.lib.modcontent.block.ResonantBlock
import resonantengine.lib.wrapper.CollectionWrapper._
import resonantengine.prefab.block.itemblock.ItemBlockMetadata

/**
 * A block used to build machines or decoration.
 *
 * @author Calclavia
 *
 */
class BlockDecoration extends ResonantBlock(Material.rock)
{
  var iconNames = Array("material_stone_brick", "material_stone_brick2", "material_stone_chiseled", "material_stone_cobble", "material_stone_cracked", "material_stone", "material_stone_slab", "material_stone_mossy", "material_steel_dark", "material_steel_tint", "material_steel")
  var icons = new Array[IIcon](iconNames.length)

  // Constructor
  name = "industrialStone"
  blockHardness = 1
  stepSound = Block.soundTypeStone
  this.itemBlock = classOf[ItemBlockMetadata]

  override def getIcon(side: Int, metadata: Int): IIcon =
  {
    return icons(metadata)
  }

  override def registerIcons(register: IIconRegister)
  {
    super.registerIcons(register)
    (0 until icons.size) foreach (i => icons(i) = register.registerIcon(Reference.prefix + iconNames(i)))
  }

  override def getSubBlocks(item: Item, par2CreativeTabs: CreativeTabs, list: List[_])
  {
    (0 until iconNames.length) foreach (i => list.add(new ItemStack(item, 1, i)))
  }
}