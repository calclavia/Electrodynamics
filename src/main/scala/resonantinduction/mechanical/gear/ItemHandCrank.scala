package resonantinduction.mechanical.gear

import net.minecraft.item.Item
import net.minecraft.world.World

class ItemHandCrank extends Item
{
  setMaxStackSize(1)
  
  def shouldPassSneakingClickToBlock(world: World, x: Int, y: Int, z: Int): Boolean =
  {
    return true
  }
}