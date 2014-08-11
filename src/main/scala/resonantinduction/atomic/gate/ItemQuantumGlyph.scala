package resonantinduction.atomic.gate

import java.util.List
import codechicken.microblock.CornerPlacementGrid
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantinduction.core.prefab.part.IHighlight
import codechicken.lib.vec.BlockCoord
import codechicken.lib.vec.Vector3
import codechicken.multipart.JItemMultiPart
import codechicken.multipart.MultiPartRegistry
import codechicken.multipart.TMultiPart
import codechicken.multipart.TileMultipart
import scala.util.control.Breaks._
import resonant.lib.wrapper.WrapList._

class ItemQuantumGlyph extends JItemMultiPart with IHighlight {

  setHasSubtypes(true)

  override def getUnlocalizedName(itemStack: ItemStack): String = {
    return super.getUnlocalizedName(itemStack) + "." + itemStack.getItemDamage
  }

  def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, side: Int, hit: Vector3): TMultiPart = {
    val part: PartQuantumGlyph = MultiPartRegistry.createPart("resonant_induction_quantum_glyph", false).asInstanceOf[PartQuantumGlyph]
    var slot: Int = CornerPlacementGrid.getHitSlot(hit, side)
    val tile: TileEntity = world.getTileEntity(pos.x, pos.y, pos.z)
    if (tile.isInstanceOf[TileMultipart]) {
      val checkPart: TMultiPart = (tile.asInstanceOf[TileMultipart]).partMap(slot)
      if (checkPart != null) {
        side match {
          case 0 =>
            slot -= 1
            break //todo: break is not supported
          case 1 =>
            slot += 1
            break //todo: break is not supported
          case 2 =>
            slot -= 2
            break //todo: break is not supported
          case 3 =>
            slot += 2
            break //todo: break is not supported
          case 4 =>
            slot -= 4
            break //todo: break is not supported
          case 5 =>
            slot += 4
            break //todo: break is not supported
        }
      }
      else {
      }
    }
    part.preparePlacement(slot, itemStack.getItemDamage)
    return part
  }

  override def getSubItems(item: Item, tab: CreativeTabs, listToAddTo: List[_]) {

      for(i <- 0 until PartQuantumGlyph.MAX_GLYPH)
      {
        {
          listToAddTo.add(new ItemStack(item, 1, i))
        }
    }
  }

  def getHighlightType: Int =
  {
    return 1
  }
}