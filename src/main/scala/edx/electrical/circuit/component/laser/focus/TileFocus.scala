package edx.electrical.circuit.component.laser.focus

import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.transform.vector.Vector3

/**
 * @author Calclavia
 */
abstract class TileFocus(material: Material) extends ResonantTile(material) with IFocus
{
  override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
  {
    setFocus(new Vector3(-entityLiving.rotationYaw, entityLiving.rotationPitch).normalize)
  }

  override def activate(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.getCurrentEquippedItem == null || !player.getCurrentEquippedItem.getItem.isInstanceOf[ItemFocusingMatrix])
    {
      if (player.isSneaking)
      {
        focus(new Vector3(ForgeDirection.getOrientation(side)) + new Vector3(x, y, z) + 0.5)
      }
      else
      {
        focus(hit + new Vector3(x, y, z))
      }

      return true
    }

    return false
  }

  def isPowered = getWorldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)
}
