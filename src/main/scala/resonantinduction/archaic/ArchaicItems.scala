package resonantinduction.archaic

import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}
import resonant.content.loader.ContentHolder
;

object ArchaicItems extends ContentHolder
{
  var itemImprint: Item = _
  var itemHammer: Item = _
  var itemHandCrank: Item = _

  override def postInit()
  {
    recipes += shaped(itemHandCrank, "S  ", "SSS", "  S", 'S', "stickWood")
    recipes += shaped(itemImprint, "PPP", "PIP", "PPP", 'P', Items.paper, 'I', new ItemStack(Items.dye, 0))
    recipes += shaped(itemHammer, "CC ", "CS ", "  S", 'C', "cobblestone", 'S', "stickWood")
  }
}