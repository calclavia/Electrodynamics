package edx.core.resource.content

import edx.core.Reference
import net.minecraft.creativetab.CreativeTabs
import resonant.lib.factory.resources.item.TItemResource

/**
 * Item for ore dusts
 * @author Calclavia
 */
class ItemDust extends TItemResource
{
  setTextureName(Reference.prefix + "oreDust")
  setCreativeTab(CreativeTabs.tabMaterials)
}
