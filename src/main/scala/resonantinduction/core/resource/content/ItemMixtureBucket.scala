package resonantinduction.core.resource.content

import resonantinduction.core.Reference

/**
 * @author Calclavia
 */
class ItemMixtureBucket(newMaterial: String) extends ItemResource with TBucket
{
  setTextureName(Reference.prefix + "bucketMixture")
  material = newMaterial
}
