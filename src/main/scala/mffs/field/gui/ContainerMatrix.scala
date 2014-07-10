package mffs.field.gui

import mffs.base.TileFieldMatrix
import mffs.item.card.ItemCardFrequency
import mffs.slot.SlotBase
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.gui.ContainerBase
import resonant.lib.prefab.slot.SlotSpecific
import universalelectricity.core.transform.vector.Vector2

class ContainerMatrix(player: EntityPlayer, tileEntity: TileFieldMatrix, val matrixCenter: Vector2 = new Vector2(80, 55)) extends ContainerBase(tileEntity)
{
  //Frequency
  addSlotToContainer(new SlotSpecific(tileEntity, 0, 8, 114, classOf[ItemCardFrequency]))

  /**
   * Matrix. See {@link GuiElectromagneticProjector}
   */

  val slotCenter = matrixCenter + 1

  //Mode
  addSlotToContainer(new SlotBase(tileEntity, 1, slotCenter.xi, slotCenter.yi))

  //FRONT (SOUTH)
  (1 to 2) foreach (i => addSlotToContainer(new SlotBase(tileEntity, i + 1, slotCenter.xi, slotCenter.yi - 18 * i)))
  //BACK (NORTH)
  (1 to 2) foreach (i => addSlotToContainer(new SlotBase(tileEntity, i + 3, slotCenter.xi, slotCenter.yi + 18 * i)))
  //RIGHT (WEST)
  (1 to 2) foreach (i => addSlotToContainer(new SlotBase(tileEntity, i + 5, slotCenter.xi + 18 * i, slotCenter.yi)))
  //LEFT (EAST)
  (1 to 2) foreach (i => addSlotToContainer(new SlotBase(tileEntity, i + 7, slotCenter.xi - 18 * i, slotCenter.yi)))

  //UP
  addSlotToContainer(new SlotBase(tileEntity, 10, slotCenter.xi - 18, slotCenter.yi - 18))
  addSlotToContainer(new SlotBase(tileEntity, 11, slotCenter.xi + 18, slotCenter.yi - 18))
  //DOWN
  addSlotToContainer(new SlotBase(tileEntity, 12, slotCenter.xi - 18, slotCenter.yi + 18))
  addSlotToContainer(new SlotBase(tileEntity, 13, slotCenter.xi + 18, slotCenter.yi + 18))

  var count = 0
  //Draw non-directional slots
  for (x <- -2 to 2; y <- -2 to 2)
  {
    if (new Vector2(x, y).magnitude > 2)
    {
      addSlotToContainer(new SlotBase(tileEntity, count + 14, slotCenter.xi + 18 * x, slotCenter.yi + 18 * y))
      count += 1
    }
  }

  addPlayerInventory(player)

}