package mffs.container;

import mffs.base.ContainerBase;
import mffs.slot.SlotBase;
import mffs.slot.SlotCard;
import mffs.tileentity.TileEntityInterdictionMatrix;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerInterdictionMatrix extends ContainerBase
{
	public ContainerInterdictionMatrix(EntityPlayer player, TileEntityInterdictionMatrix tileEntity)
	{
		super(tileEntity);

		/**
		 * Frequency Card
		 */
		this.addSlotToContainer(new SlotCard(tileEntity, 0, 87, 89));
		this.addSlotToContainer(new SlotBase(tileEntity, 1, 69, 89));

		/**
		 * Module slots.
		 */
		for (int var3 = 0; var3 < 2; var3++)
		{
			for (int var4 = 0; var4 < 4; var4++)
			{
				this.addSlotToContainer(new SlotBase(tileEntity, var4 + var3 * 4 + 2, 99 + var4 * 18, 31 + var3 * 18));
			}
		}

		/**
		 * Item filter slots.
		 */
		for (int var4 = 0; var4 < 9; var4++)
		{
			this.addSlotToContainer(new SlotBase(tileEntity, var4 + 8 + 2, 9 + var4 * 18, 69));
		}

		this.addPlayerInventory(player);
	}
}