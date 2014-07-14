package resonantinduction.electrical.em.laser.focus

import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.world.World
import net.minecraft.entity.player.EntityPlayer
import resonantinduction.electrical.em.Vector3
import net.minecraftforge.common.util.ForgeDirection
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack

/**
 * @author Calclavia
 */
abstract class BlockFocusBase(material: Material) extends BlockContainer(material)
{
  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, itemStack: ItemStack)
  {
    val focusDevice = world.getTileEntity(x, y, z).asInstanceOf[IFocus]
    focusDevice.setFocus(new Vector3(-entity.rotationYaw, entity.rotationPitch).normalize)
  }

  /**
   * Called upon block activation (right click on the block.)
   */
  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean =
  {
    if (player.getCurrentEquippedItem == null || !player.getCurrentEquippedItem.getItem.isInstanceOf[ItemFocusingMatrix])
    {
      val focusDevice = world.getTileEntity(x, y, z).asInstanceOf[IFocus]

      if (player.isSneaking)
      {
        focusDevice.focus(new Vector3(ForgeDirection.getOrientation(side)) + new Vector3(x, y, z) + 0.5)
      }
      else
      {
        focusDevice.focus(new Vector3(hitX, hitY, hitZ) + new Vector3(x, y, z))
      }

      return true
    }

    return false
  }
}
