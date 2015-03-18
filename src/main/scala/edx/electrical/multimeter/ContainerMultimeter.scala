/**
 *
 */
package edx.electrical.multimeter

import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{Container, Slot}

/**
 * @author Calclavia
 *
 */
class ContainerMultimeter(inventoryPlayer: InventoryPlayer, tileEntity: PartMultimeter) extends Container
{
  private final val yDisplacement: Int = 51

  for (i <- 0 until 3; j <- 0 until 9)
    addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + yDisplacement))

  for (i <- 0 until 9)
    this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142 + yDisplacement))

  tileEntity.playersUsing.add(inventoryPlayer.player)

  override def onContainerClosed(entityPlayer: EntityPlayer)
  {
    this.tileEntity.playersUsing.remove(entityPlayer)
    super.onContainerClosed(entityPlayer)
  }

  def canInteractWith(entityplayer: EntityPlayer): Boolean = true
}