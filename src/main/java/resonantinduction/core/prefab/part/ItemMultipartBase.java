package resonantinduction.core.prefab.part;

import net.minecraftforge.common.Configuration;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import codechicken.multipart.JItemMultiPart;

/**
 * @author Calclavia
 * 
 */
public abstract class ItemMultipartBase extends JItemMultiPart
{
	public ItemMultipartBase(String name, int id)
	{
		super(Settings.CONFIGURATION.get(Configuration.CATEGORY_ITEM, name, id).getInt(id));
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setTextureName(Reference.PREFIX + name);
	}

}
