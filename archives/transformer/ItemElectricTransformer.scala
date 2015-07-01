package com.calclavia.edx.electrical.circuit.transformer

import codechicken.lib.vec.{BlockCoord, Vector3}
import codechicken.microblock.FacePlacementGrid
import codechicken.multipart.{JItemMultiPart, TMultiPart}
import edx.core.ResonantPartFactory
import edx.core.prefab.part.IHighlight
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.MathHelper
import net.minecraft.world.World

/**
 * Item for Electric Transformer that handles block/part placement
 */
class ItemElectricTransformer extends JItemMultiPart with IHighlight
{
  override def newPart(itemStack: ItemStack, player: EntityPlayer, world: World, pos: BlockCoord, s: Int, hit: Vector3): TMultiPart =
  {
    val side: Int = FacePlacementGrid.getHitSlot(hit, s)
    val part: PartElectricTransformer = ResonantPartFactory.create(classOf[PartElectricTransformer])
    if (part != null)
    {
      val l: Int = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3
      val facing: Int = if (l == 0) 2 else (if (l == 1) 5 else (if (l == 2) 3 else (if (l == 3) 4 else 0)))
      part.preparePlacement(side, facing)
    }
    return part
  }

  override def getHighlightType: Int =
  {
    return 0
  }
}