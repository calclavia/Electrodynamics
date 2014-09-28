package resonantinduction.atomic.items

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import resonant.lib.prefab.poison.PoisonRadiation
import universalelectricity.core.transform.vector.Vector3

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