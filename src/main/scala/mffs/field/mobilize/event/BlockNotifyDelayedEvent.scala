package mffs.field.mobilize.event

import cpw.mods.fml.common.Loader
import cpw.mods.fml.relauncher.ReflectionHelper
import mffs.field.mobilize.TileForceMobilizer
import net.minecraft.world.World
import universalelectricity.core.transform.vector.Vector3

/**
 * Removes the TileEntity
 *
 * @author Calclavia
 */
class BlockNotifyDelayedEvent(handler: IDelayedEventHandler, ticks: Int, world: World, position: Vector3) extends DelayedEvent(handler, ticks)
{
  protected override def onEvent
  {
    if (!this.world.isRemote)
    {
      world.notifyBlocksOfNeighborChange(position.xi, position.yi, position.zi, position.getBlock(world))
      val newTile = position.getTileEntity(world)

      if (newTile != null)
      {
        if (Loader.isModLoaded("BuildCraft|Factory"))
        {
          try
          {
            val clazz = Class.forName("buildcraft.factory.TileQuarry").asInstanceOf[Class[_ >: Any]]

            if (clazz == newTile.getClass)
            {
              ReflectionHelper.setPrivateValue(clazz, newTile, true, "isAlive")
            }
          }
          catch
            {
              case e: Exception =>
              {
                e.printStackTrace
              }
            }
        }

        handler.asInstanceOf[TileForceMobilizer].performingMove = false
      }
    }
  }
}