package resonantinduction.base;

import net.minecraftforge.common.Configuration;
import resonantinduction.ResonantInduction;
import resonantinduction.TabRI;
import codechicken.multipart.JItemMultiPart;

/**
 * @author Calclavia
 * 
 */
public abstract class ItemMultipartBase extends JItemMultiPart
{
	public ItemMultipartBase(String name, int id)
	{
		super(ResonantInduction.CONFIGURATION.get(Configuration.CATEGORY_ITEM, name, id).getInt(id));
		this.setCreativeTab(TabRI.INSTANCE);
		this.setUnlocalizedName(ResonantInduction.PREFIX + name);
		this.setTextureName(ResonantInduction.PREFIX + name);
	}

}
