package resonantinduction.core.resource.content

import resonantinduction.core.Reference

/**
 * @author Calclavia
 */
class ItemDust(newMaterial: String) extends ItemResource
{
  setTextureName(Reference.prefix + "oreDust")
  material = newMaterial
}
