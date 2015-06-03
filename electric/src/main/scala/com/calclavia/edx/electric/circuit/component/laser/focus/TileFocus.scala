package com.calclavia.edx.electric.circuit.component.laser.focus

import nova.core.block.Block
import nova.core.util.Direction

/**
 * @author Calclavia
 */
abstract class TileFocus extends Block with IFocus
{
  override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
  {
	  setFocus(new Vector3d(-entityLiving.rotationYaw, entityLiving.rotationPitch).normalize)
  }

	override def activate(player: EntityPlayer, side: Int, hit: Vector3d): Boolean =
  {
    if (player.getCurrentEquippedItem == null || !player.getCurrentEquippedItem.getItem.isInstanceOf[ItemFocusingMatrix])
    {
      if (player.isSneaking)
      {
	      focus(new Vector3d(Direction.getOrientation(side)) + new Vector3d(x, y, z) + 0.5)
      }
      else
      {
	      focus(hit + new Vector3d(x, y, z))
      }

      return true
    }

    return false
  }

  def isPowered = getWorldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)
}
