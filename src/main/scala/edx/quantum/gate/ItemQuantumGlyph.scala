package edx.quantum.gate

import java.util.List

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.microblock.CornerPlacementGrid
import codechicken.multipart._
import edx.core.ResonantPartFactory
import edx.core.prefab.part.IHighlight
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import resonant.lib.wrapper.CollectionWrapper._

class ItemQuantumGlyph extends Item with TItemMultiPart with IHighlight
{
  setHasSubtypes(true)

  override def getUnlocalizedName(itemStack: ItemStack): String =
  {
    return super.getUnlocalizedName(itemStack) + "." + itemStack.getItemDamage
  }

  def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, side: Int, hit: Vector3): TMultiPart =
  {
    val part = ResonantPartFactory.create(classOf[PartQuantumGlyph])
    var slot = CornerPlacementGrid.getHitSlot(hit, side)
    val tile = world.getTileEntity(pos.x, pos.y, pos.z)

    if (tile.isInstanceOf[TileMultipart])
    {
      val checkPart: TMultiPart = (tile.asInstanceOf[TileMultipart]).partMap(slot)
      if (checkPart != null)
      {
        side match
        {
          case 0 => slot -= 1
          case 1 => slot += 1
          case 2 => slot -= 2
          case 3 => slot += 2
          case 4 => slot -= 4
          case 5 => slot += 4
        }
      }
    }

    part.preparePlacement(slot, itemStack.getItemDamage)

    return part
  }

  override def getSubItems(item: Item, tab: CreativeTabs, listToAddTo: List[_])
  {
    for (i <- 0 until PartQuantumGlyph.MAX_GLYPH)
    {
      listToAddTo.add(new ItemStack(item, 1, i))
    }
  }

  def getHighlightType: Int = 1
}