package edx.core.resource.content

import edx.core.Reference
import net.minecraft.creativetab.CreativeTabs
import resonantengine.lib.factory.resources.item.TItemResource

/**
 * @author Calclavia
 */
class ItemMoltenBucket extends TItemResource with TBucket
{
  setTextureName(Reference.prefix + "bucketMolten")
  setCreativeTab(CreativeTabs.tabMaterials)
}
