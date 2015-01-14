package edx.mechanical.mech.gear

import java.util.List

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.microblock.FacePlacementGrid
import codechicken.multipart.{JItemMultiPart, PartMap, TMultiPart, TileMultipart}
import edx.core.ResonantPartFactory
import edx.core.prefab.part.IHighlight
import edx.mechanical.mech.gearshaft.PartGearShaft
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonant.lib.wrapper.CollectionWrapper._

class ItemGear extends JItemMultiPart with IHighlight
{
  setHasSubtypes(true)

  override def getUnlocalizedName(itemStack: ItemStack): String =
  {
    return super.getUnlocalizedName(itemStack) + "." + itemStack.getItemDamage
  }

  override def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, s: Int, hit: Vector3): TMultiPart =
  {

    val part: PartGear = ResonantPartFactory.create(classOf[PartGear])
    var side: Int = FacePlacementGrid.getHitSlot(hit, s)
    val tile: TileEntity = world.getTileEntity(pos.x, pos.y, pos.z)
    if (tile.isInstanceOf[TileMultipart])
    {
      val occupyingPart: TMultiPart = (tile.asInstanceOf[TileMultipart]).partMap(side)
      val centerPart: TMultiPart = (tile.asInstanceOf[TileMultipart]).partMap(PartMap.CENTER.ordinal)
      val clickedCenter: Boolean = hit.mag < 0.4
      if ((clickedCenter && centerPart.isInstanceOf[PartGearShaft]))
      {
        side ^= 1
      }
    }
    part.preparePlacement(side, itemStack.getItemDamage)
    return part
  }

  override def getSubItems(itemID: Item, tab: CreativeTabs, listToAddTo: List[_])
  {
    for (i <- 0 to 2)
    {
      listToAddTo.add(new ItemStack(itemID, 1, i))
    }
  }

  def getHighlightType: Int = 0
}