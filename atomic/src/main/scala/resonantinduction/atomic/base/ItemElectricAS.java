package resonantinduction.atomic.base;

import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.item.IEnergyItem;
import universalelectricity.api.item.ItemElectric;

@UniversalClass
public abstract class ItemElectricAS extends ItemElectric implements IEnergyItem
{
    public ItemElectricAS(int itemID, String name)
    {
        super(Settings.CONFIGURATION.getItem(name, itemID).getInt());
        this.setUnlocalizedName(Reference.PREFIX + name);
        this.setCreativeTab(TabRI.DEFAULT);
        this.setTextureName(Reference.PREFIX + name);

    }
}
