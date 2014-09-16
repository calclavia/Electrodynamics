package resonantinduction.electrical.levitator

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.microblock.FacePlacementGrid
import codechicken.multipart.{TItemMultiPart, TMultiPart}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import resonantinduction.core.ResonantPartFactory
import resonantinduction.core.prefab.part.IHighlight

class ItemLevitator extends Item with TItemMultiPart with IHighlight
{
  def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, bside: Int, hit: Vector3): TMultiPart =
  {
    val side = FacePlacementGrid.getHitSlot(hit, bside)
    val part: PartLevitator = ResonantPartFactory.create(classOf[PartLevitator])
    if (part != null)
    {
      val l: Int = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3
      val facing: Int = if (l == 0) 2 else (if (l == 1) 5 else (if (l == 2) 3 else (if (l == 3) 4 else 0)))
      part.preparePlacement(side, facing)
    }
    return part
  }

  def getHighlightType: Int = 0
}