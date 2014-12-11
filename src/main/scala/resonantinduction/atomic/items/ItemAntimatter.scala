package resonantinduction.atomic.items

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.world.World
import resonantinduction.core.Reference
import resonantinduction.core.RICreativeTab
import java.util.List
import resonant.lib.wrapper.WrapList._

class ItemAntimatter extends ItemCell
{

    private var iconGram: IIcon = null

    //Constructor
    this.setMaxDamage(0)
    this.setHasSubtypes(true)
    this.setUnlocalizedName(Reference.prefix + "antimatter")
    this.setTextureName(Reference.prefix + "antimatter")
    setCreativeTab(RICreativeTab)

    @SideOnly(Side.CLIENT) override def registerIcons(iconRegister: IIconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(Reference.prefix + "antimatter_milligram")
        this.iconGram = iconRegister.registerIcon(Reference.prefix + "antimatter_gram")
    }

    override def getIconFromDamage(metadata: Int): IIcon =
    {
        if (metadata >= 1)
        {
            return this.iconGram
        }
        else
        {
            return this.itemIcon
        }
    }

    override def getSubItems(item: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
    {
        par3List.add(new ItemStack(item, 1, 0))
        par3List.add(new ItemStack(item, 1, 1))
    }

    override def getEntityLifespan(itemStack: ItemStack, world: World): Int =
    {
        return 160
    }
}