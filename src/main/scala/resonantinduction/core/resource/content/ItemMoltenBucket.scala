package resonantinduction.core.resource.content

import resonantinduction.core.Reference

/**
 * @author Calclavia
 */
class ItemMoltenBucket(newMaterial: String) extends ItemResource with TBucket
{
  setTextureName(Reference.prefix + "bucketMolten")
  material = newMaterial
}
