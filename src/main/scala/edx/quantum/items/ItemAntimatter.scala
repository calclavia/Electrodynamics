package edx.quantum.items

import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.{EDXCreativeTab, Reference}
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.IIcon
import net.minecraft.world.World
import resonant.lib.wrapper.WrapList._

class ItemAntimatter extends ItemCell
{

  private var iconGram: IIcon = null

  //Constructor
  this.setMaxDamage(0)
  this.setHasSubtypes(true)
  this.setUnlocalizedName(Reference.prefix + "antimatter")
  this.setTextureName(Reference.prefix + "antimatter")
  setCreativeTab(EDXCreativeTab)

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