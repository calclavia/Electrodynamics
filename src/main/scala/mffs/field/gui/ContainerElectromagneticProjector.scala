package mffs.field.gui

import mffs.field.BlockProjector
import mffs.slot.SlotBase

/**
 * Container for the projector.
 * @author Calclavia
 */
class ContainerElectromagneticProjector(player: EntityPlayer, tileEntity: BlockProjector) extends ContainerMatrix(player, tileEntity)
{
  for (x <- 0 until 2; y <- 0 until 3)
    addSlotToContainer(new SlotBase(tileEntity, x + y * 2 + (1 + 25), 21 + 18 * x, 31 + 18 * y))
}
