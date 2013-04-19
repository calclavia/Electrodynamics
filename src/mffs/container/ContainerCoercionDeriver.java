package mffs.container;

import mffs.base.ContainerBase;
import mffs.slot.SlotBase;
import mffs.slot.SlotCard;
import mffs.tileentity.TileEntityCoercionDeriver;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerCoercionDeriver extends ContainerBase
{
	public ContainerCoercionDeriver(EntityPlayer player, TileEntityCoercionDeriver tileEntity)
	{
		super(tileEntity);

		/**
		 * Frequency Card
		 */
		this.addSlotToContainer(new SlotCard(tileEntity, 0, 9, 41));

		/**
		 * Input
		 */
		this.addSlotToContainer(new SlotBase(tileEntity, 1, 9, 83));
		this.addSlotToContainer(new SlotBase(tileEntity, 2, 9 + 20, 83));

		/**
		 * Upgrades
		 */
		this.addSlotToContainer(new SlotBase(tileEntity, 3, 154, 67));
		this.addSlotToContainer(new SlotBase(tileEntity, 4, 154, 87));
		this.addSlotToContainer(new SlotBase(tileEntity, 5, 154, 47));

		this.addPlayerInventory(player);
	}
}