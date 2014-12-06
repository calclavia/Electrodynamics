package resonantinduction.atomic.machine.extractor

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.{Slot, SlotFurnace}
import resonant.lib.prefab.gui.ContainerBase
import resonant.lib.prefab.slot.SlotEnergyItem

/**
 * Chemical extractor container
 */
object ContainerChemicalExtractor
{
    private final val slotCount: Int = 5
}

class ContainerChemicalExtractor(par1InventoryPlayer: InventoryPlayer, tileEntity: TileChemicalExtractor) extends ContainerBase(tileEntity)
{
    //Constructor
    addSlotToContainer(new SlotEnergyItem(tileEntity, 0, 80, 50))
    addSlotToContainer(new Slot(tileEntity, 1, 53, 25))
    addSlotToContainer(new SlotFurnace(par1InventoryPlayer.player, tileEntity, 2, 107, 25))
    addSlotToContainer(new Slot(tileEntity, 3, 25, 19))
    addSlotToContainer(new Slot(tileEntity, 4, 25, 50))
    addSlotToContainer(new Slot(tileEntity, 5, 135, 19))
    addSlotToContainer(new Slot(tileEntity, 6, 135, 50))
    addPlayerInventory(par1InventoryPlayer.player)


}