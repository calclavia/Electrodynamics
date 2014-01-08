package resonantinduction.transport;

import resonantinduction.transport.imprinter.TileEntityFilterable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerFilter extends Container
{
    private TileEntityFilterable tileEntity;

    public ContainerFilter(InventoryPlayer playerInv, TileEntityFilterable tile)
    {
        this.tileEntity = tile;
        this.addSlotToContainer(new Slot(tile, TileEntityFilterable.FILTER_SLOT, 37, 25));
        this.addSlotToContainer(new Slot(tile, TileEntityFilterable.BATERY_DRAIN_SLOT, 144, 43));

        int i;
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting par1ICrafting)
    {
        super.addCraftingToCrafters(par1ICrafting);
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return this.tileEntity.isUseableByPlayer(par1EntityPlayer);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
    {
        return null;
    }
}
