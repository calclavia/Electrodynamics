package resonantinduction.core.prefab.item;

import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;

/** @author Calclavia */
public class ItemRI extends Item
{
	public ItemRI(String name)
	{
		this(name, Settings.getNextItemID());
	}

	public ItemRI(String name, int id)
	{
		super(Settings.CONFIGURATION.get(Configuration.CATEGORY_ITEM, name, id).getInt(id));
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setTextureName(Reference.PREFIX + name);
	}
}
