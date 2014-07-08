package mffs.production;

import mffs.slot.SlotBase;
import mffs.slot.SlotCard;
import net.minecraft.entity.player.EntityPlayer;
import resonant.lib.gui.ContainerBase;

public class ContainerCoercionDeriver extends ContainerBase
{
	public ContainerCoercionDeriver(EntityPlayer player, TileCoercionDeriver tileEntity)
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