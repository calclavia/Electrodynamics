package edx.quantum.items

import net.minecraft.entity.{Entity, EntityLivingBase}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import resonant.lib.prefab.poison.PoisonRadiation
import resonant.lib.transform.vector.Vector3

/**
 * Radioactive Items
 */
class ItemRadioactive extends Item
{
  override def onUpdate(par1ItemStack: ItemStack, par2World: World, entity: Entity, par4: Int, par5: Boolean)
  {
    if (entity.isInstanceOf[EntityLivingBase])
    {
      PoisonRadiation.INSTANCE.poisonEntity(new Vector3(entity), entity.asInstanceOf[EntityLivingBase], 1)
    }
  }
}