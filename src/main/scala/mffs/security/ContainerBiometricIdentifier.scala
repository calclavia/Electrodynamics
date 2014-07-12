package mffs.security

import mffs.item.card.ItemCardFrequency
import mffs.slot.SlotBase
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.gui.ContainerBase
import resonant.lib.prefab.slot.SlotSpecific

class ContainerBiometricIdentifier(player: EntityPlayer, tile: TileBiometricIdentifier) extends ContainerBase(tile)
{
  //Frequency
  addSlotToContainer(new SlotSpecific(tile, 0, 8, 114, classOf[ItemCardFrequency]))

  for (x <- 0 until 9; y <- 0 until 5)
    addSlotToContainer(new SlotBase(tile, x + y * 5, 9 + x * 18, 36 + y * 18))
}