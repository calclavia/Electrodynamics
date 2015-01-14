package edx.core.resource.content

import edx.core.Reference
import net.minecraft.creativetab.CreativeTabs
import resonant.lib.factory.resources.item.TItemResource

/**
 * @author Calclavia
 */
class ItemRubble extends TItemResource
{
  setTextureName(Reference.prefix + "oreRubble")
  setCreativeTab(CreativeTabs.tabMaterials)
}
