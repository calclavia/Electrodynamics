package mffs.field

import mffs.item.card.ItemCardFrequency
import mffs.slot.SlotBase
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.gui.ContainerBase
import resonant.lib.prefab.slot.SlotSpecific
import universalelectricity.core.transform.vector.Vector2

class ContainerElectromagneticProjector(player: EntityPlayer, tileEntity: TileElectromagneticProjector) extends ContainerBase(tileEntity)
{

  //Frequency
  addSlotToContainer(new SlotSpecific(tileEntity, 0, 8, 114, classOf[ItemCardFrequency]))

  /**
   * Matrix. See {@link GuiElectromagneticProjector}
   */

  val center = new Vector2(80, 55) + 1

  //Mode
  addSlotToContainer(new SlotBase(tileEntity, 1, center.xi, center.yi))

  //NORTH
  (1 to 2) foreach (i => addSlotToContainer(new SlotBase(tileEntity, i + 1, center.xi, center.yi - 18 * i)))
  //SOUTH
  (1 to 2) foreach (i => addSlotToContainer(new SlotBase(tileEntity, i + 3, center.xi, center.yi + 18 * i)))
  //EAST
  (1 to 2) foreach (i => addSlotToContainer(new SlotBase(tileEntity, i + 5, center.xi + 18 * i, center.yi)))
  //WEST
  (1 to 2) foreach (i => addSlotToContainer(new SlotBase(tileEntity, i + 7, center.xi - 18 * i, center.yi)))

  //UP
  addSlotToContainer(new SlotBase(tileEntity, 10, center.xi - 18, center.yi - 18))
  addSlotToContainer(new SlotBase(tileEntity, 11, center.xi + 18, center.yi - 18))
  //DOWN
  addSlotToContainer(new SlotBase(tileEntity, 12, center.xi - 18, center.yi + 18))
  addSlotToContainer(new SlotBase(tileEntity, 13, center.xi + 18, center.yi + 18))

  var count = 0
  //Draw non-directional slots
  for (x <- -2 to 2; y <- -2 to 2)
  {
    if (new Vector2(x, y).magnitude > 2)
    {
      addSlotToContainer(new SlotBase(tileEntity, count + 14, center.xi + 18 * x, center.yi + 18 * y))
      count += 1
    }
  }

  addPlayerInventory(player)

}