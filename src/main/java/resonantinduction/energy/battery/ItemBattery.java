package resonantinduction.energy.battery;

import resonantinduction.AssemblyLine;
import resonantinduction.IndustryTabs;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.api.item.ItemElectric;

import com.builtbroken.minecraft.DarkCore;
import com.builtbroken.minecraft.IExtraInfo.IExtraItemInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Simple battery to store energy
 * 
 * @author DarkGuardsman */
public class ItemBattery extends ItemElectric implements IExtraItemInfo
{
    public ItemBattery()
    {
        super(AssemblyLine.CONFIGURATION.getItem("Battery", DarkCore.getNextItemId()).getInt());
        this.setUnlocalizedName(AssemblyLine.PREFIX + "Battery");
        this.setCreativeTab(IndustryTabs.tabIndustrial());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().replace("item.", ""));
    }

    @Override
    public long getVoltage(ItemStack itemStack)
    {
        return 25;
    }

    @Override
    public long getEnergyCapacity(ItemStack theItem)
    {
        return 5000000;
    }

    @Override
    public boolean hasExtraConfigs()
    {
        return false;
    }

    @Override
    public void loadExtraConfigs(Configuration config)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadOreNames()
    {
        OreDictionary.registerOre("Battery", this);
    }
}
