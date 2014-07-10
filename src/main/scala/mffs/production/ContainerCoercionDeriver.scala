package mffs.production

import mffs.item.card.ItemCardFrequency
import mffs.slot.SlotBase
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.gui.ContainerBase
import resonant.lib.prefab.slot.SlotSpecific

class ContainerCoercionDeriver(player: EntityPlayer, tileEntity: TileCoercionDeriver) extends ContainerBase(tileEntity)
{
  //Frequency
  addSlotToContainer(new SlotSpecific(tileEntity, 0, 8, 114, classOf[ItemCardFrequency]))

  addSlotToContainer(new SlotBase(tileEntity, 1, 9, 76))
  addSlotToContainer(new SlotBase(tileEntity, 2, 9 + 20, 76))

  //Upgrade slots
  (0 to 2) foreach (y => addSlotToContainer(new SlotBase(tileEntity, y + 3, 154, 47 + y * 18)))

  addPlayerInventory(player)

}