/**
 * 
 */
package resonantinduction.battery;

import resonantinduction.base.ItemBase;

/**
 * Stores power.
 * 
 * @author Calclavia
 * 
 */
public class ItemCapacitor extends ItemBase
{
	public ItemCapacitor(int id)
	{
		super("capacitor", id);
		this.setMaxStackSize(1);
		this.setMaxDamage(1000);
	}

}
