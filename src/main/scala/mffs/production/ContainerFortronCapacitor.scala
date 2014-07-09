package mffs.production

import mffs.item.card.ItemCardFrequency
import mffs.slot.SlotBase
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.gui.ContainerBase
import resonant.lib.prefab.slot.SlotSpecific

class ContainerFortronCapacitor(player: EntityPlayer, tileEntity: TileFortronCapacitor) extends ContainerBase(tileEntity)
{
  //Frequency
  addSlotToContainer(new SlotSpecific(tileEntity, 0, 8, 114, classOf[ItemCardFrequency]))

  //Upgrade slots
  (0 to 2) foreach (y => addSlotToContainer(new SlotBase(tileEntity, y + 1, 154, 47 + y * 18)))

  //Input slots
  for (x <- 0 to 1; y <- 0 to 1)
    addSlotToContainer(new SlotBase(this.tileEntity, x + y * 2 + 4, 9 + x * 18, 74 + y * 18))

  //Output slots
  for (x <- 0 to 1; y <- 0 to 1)
    addSlotToContainer(new SlotBase(this.tileEntity, x + y * 2 + 8, 91 + x * 18, 74 + y * 18))

  addPlayerInventory(player)
}