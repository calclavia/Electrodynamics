package resonantinduction.old.lib.prefab.invgui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** TileEntities that wish to do something if an item is removed from a slot */
public interface ISlotPickResult
{
	/**
	 * If the slot in the gui does something if the item is removed
	 * 
	 * @param entityPlayer - player who removed the item, will pass null from automation
	 * @param slot - slot in the container class the item came from
	 * @param itemStack - item stack pulled from the slot
	 */
	public void onPickUpFromSlot(EntityPlayer entityPlayer, int slot, ItemStack itemStack);
}
