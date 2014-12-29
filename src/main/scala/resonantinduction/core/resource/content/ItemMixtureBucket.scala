package resonantinduction.core.resource.content

import resonantinduction.core.Reference

/**
 * @author Calclavia
 */
class ItemMixtureBucket(material: String) extends ItemResource(material) with TBucket
{
  setTextureName(Reference.prefix + "bucketMixture")
}
