/**
 * 
 */
package resonantinduction.battery;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import resonantinduction.ResonantInduction;
import resonantinduction.TabRI;
import resonantinduction.api.ICapacitor;
import universalelectricity.compatibility.ItemUniversalElectric;

/**
 * Stores power.
 * 
 * @author Calclavia
 * 
 */
public class ItemCapacitor extends ItemUniversalElectric implements ICapacitor
{
	public ItemCapacitor(int id)
	{
		super(ResonantInduction.CONFIGURATION.get(Configuration.CATEGORY_ITEM, "capacitor", id).getInt(id));
		this.setCreativeTab(TabRI.INSTANCE);
		this.setUnlocalizedName(ResonantInduction.PREFIX + "capacitor");
		this.setTextureName(ResonantInduction.PREFIX + "capacitor");
		this.setMaxStackSize(1);
		this.setMaxDamage(100);
	}

	@Override
	public float getTransfer(ItemStack itemStack)
	{
		return this.getMaxEnergyStored(itemStack) * 0.05F;
	}

	@Override
	public float getMaxElectricityStored(ItemStack theItem)
	{
		return 500;
	}

}
