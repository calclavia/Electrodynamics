package mffs.security

import mffs.slot.{SlotActive, SlotDisabled, SlotBase}
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.gui.ContainerBase

class ContainerBiometricIdentifier(player: EntityPlayer, tile: TileBiometricIdentifier) extends ContainerBase(tile)
{
  addSlotToContainer(new SlotActive(tile, 0, 88, 91))
  addSlotToContainer(new SlotBase(tile, 1, 8, 46))
  addSlotToContainer(new SlotActive(tile, 2, 8, 91))
  (0 until 9) foreach (i => addSlotToContainer(new SlotActive(tile, 3 + i, 8 + i * 18, 111)))
  addSlotToContainer(new SlotBase(tile, TileBiometricIdentifier.SLOT_COPY, 8, 66))
  addPlayerInventory(player)
}