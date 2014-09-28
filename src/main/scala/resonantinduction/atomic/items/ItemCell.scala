package resonantinduction.atomic.items

import net.minecraft.item.ItemStack
import resonant.content.prefab.itemblock.ItemTooltip
import resonant.lib.utility.LanguageUtility
import resonantinduction.atomic.AtomicContent
import resonantinduction.core.{Reference, ResonantTab}

class ItemCell extends ItemTooltip
{
    //Constructor
    setContainerItem(AtomicContent.itemCell)

    def this(name: String)
    {
        this()
        if (!name.equalsIgnoreCase("cellEmpty")) this.setContainerItem(AtomicContent.itemCell)
        this.setUnlocalizedName(Reference.prefix + name)
        this.setTextureName(Reference.prefix + name)
        setCreativeTab(ResonantTab.tab)
    }

    override def getUnlocalizedName(itemstack: ItemStack): String =
    {
        val localized: String = LanguageUtility.getLocal(getUnlocalizedName() + "." + itemstack.getItemDamage + ".name")
        if (localized != null && !localized.isEmpty)
        {
            return getUnlocalizedName() + "." + itemstack.getItemDamage
        }
        return getUnlocalizedName()
    }
}