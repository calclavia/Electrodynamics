package resonantinduction.core.resource.content

import net.minecraft.creativetab.CreativeTabs
import resonant.lib.factory.resources.item.TItemResource
import resonantinduction.core.Reference

/**
 * @author Calclavia
 */
class ItemMoltenBucket extends TItemResource with TBucket
{
  setTextureName(Reference.prefix + "bucketMolten")
  setCreativeTab(CreativeTabs.tabMaterials)
}
