package mffs.field.gui

import mffs.field.TileElectromagneticProjector
import mffs.slot.SlotBase
import net.minecraft.entity.player.EntityPlayer

/**
 * Container for the projector.
 * @author Calclavia
 */
class ContainerElectromagneticProjector(player: EntityPlayer, tileEntity: TileElectromagneticProjector) extends ContainerMatrix(player, tileEntity)
{
  for (x <- 0 until 2; y <- 0 until 3)
    addSlotToContainer(new SlotBase(tileEntity, x + y * 2 + (1 + 25), 21 + 18 * x, 31 + 18 * y))
}
