package resonantinduction.core.resource.content

import net.minecraft.creativetab.CreativeTabs
import resonant.lib.factory.resources.item.TItemResource
import resonantinduction.core.Reference

/**
 * Item for ore dusts
 * @author Calclavia
 */
class ItemDust extends TItemResource
{
  setTextureName(Reference.prefix + "oreDust")
  setCreativeTab(CreativeTabs.tabMaterials)
}
