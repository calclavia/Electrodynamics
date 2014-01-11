package dark.lib.prefab.invgui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import universalelectricity.api.CompatibilityModule;

/**
 * Slot designed to only allow batery like items
 * 
 * @author DarkGuardsman
 */
public class SlotEnergyItem extends Slot
{

	public SlotEnergyItem(IInventory inventory, int slotID, int xPos, int yPos)
	{
		super(inventory, slotID, xPos, yPos);
	}

	@Override
	public boolean isItemValid(ItemStack compareStack)
	{
		return compareStack != null && CompatibilityModule.isHandler(compareStack.getItem());
	}
}
