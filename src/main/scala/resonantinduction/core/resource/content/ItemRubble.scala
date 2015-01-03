package resonantinduction.core.resource.content

import resonantinduction.core.Reference

/**
 * @author Calclavia
 */
class ItemRubble(newMaterial: String) extends ItemResource
{
  setTextureName(Reference.prefix + "oreRubble")
  material = newMaterial
}
