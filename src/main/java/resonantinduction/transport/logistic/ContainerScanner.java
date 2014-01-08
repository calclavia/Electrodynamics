package resonantinduction.transport.logistic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** @author Archadia */
public class ContainerScanner extends Container
{

	private TileScanner tileEnt;

	public ContainerScanner(InventoryPlayer par1InventoryPlayer, TileScanner tile)
	{
		bindPlayerInventory(par1InventoryPlayer);

		this.tileEnt = tile;
	}

	public void bindPlayerInventory(InventoryPlayer inv)
	{
		int i;

		for (i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (i = 0; i < 9; ++i)
		{
			this.addSlotToContainer(new Slot(inv, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer)
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
		return null;
	}

}
