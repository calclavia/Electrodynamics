package edx.mechanical.mech.gear

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.world.World

class ItemHandCrank extends Item
{
  setMaxStackSize(1)

  override def doesSneakBypassUse(world: World, x: Int, y: Int, z: Int, player: EntityPlayer) = true
}