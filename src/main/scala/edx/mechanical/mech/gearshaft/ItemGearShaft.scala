package edx.mechanical.mech.gearshaft

import java.util.List

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.multipart.{TItemMultiPart, TMultiPart}
import edx.core.ResonantPartFactory
import edx.core.prefab.part.IHighlight
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import resonant.lib.wrapper.WrapList._

class ItemGearShaft extends Item with TItemMultiPart with IHighlight
{
  setHasSubtypes(true)

  override def getUnlocalizedName(itemStack: ItemStack): String = super.getUnlocalizedName(itemStack) + "." + itemStack.getItemDamage

  def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, side: Int, hit: Vector3): TMultiPart =
  {
    val part = ResonantPartFactory.create(classOf[PartGearShaft])
    if (part != null)
    {
      part.preparePlacement(side, itemStack.getItemDamage)
    }
    return part
  }

  override def getSubItems(itemID: Item, tab: CreativeTabs, listToAddTo: List[_])
  {
    for (i <- 0 until 3)
    {
      listToAddTo.add(new ItemStack(itemID, 1, i))
    }
  }

  override def getHighlightType: Int = 0
}