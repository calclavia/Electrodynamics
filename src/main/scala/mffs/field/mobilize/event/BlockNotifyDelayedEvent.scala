package mffs.field.mobilize.event

import mffs.field.mobilize.BlockMobilizer

/**
 * Removes the TileEntity
 *
 * @author Calclavia
 */
class BlockNotifyDelayedEvent(handler: IDelayedEventHandler, ticks: Int, world: World, position: Vector3d) extends DelayedEvent(handler, ticks)
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

		  handler.asInstanceOf[BlockMobilizer].performingMove = false
      }
    }
  }
}