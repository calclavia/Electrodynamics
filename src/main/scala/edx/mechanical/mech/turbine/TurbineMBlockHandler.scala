package edx.mechanical.mech.turbine

import net.minecraft.tileentity.TileEntity
import resonantengine.lib.prefab.tile.multiblock.reference.MultiBlockHandler
import resonantengine.lib.transform.vector.Vector3

class TurbineMBlockHandler(wrapper: TileTurbine) extends MultiBlockHandler[TileTurbine](wrapper)
{
  override def getWrapperAt(position: Vector3): TileTurbine =
  {
    val tile: TileEntity = position.getTileEntity(this.tile.getWorld)
    if (tile != null && wrapperClass.isAssignableFrom(tile.getClass))
    {
      if (tile.asInstanceOf[TileTurbine].getDirection == this.tile.getDirection && tile.asInstanceOf[TileTurbine].tier == this.tile.tier)
      {
        return tile.asInstanceOf[TileTurbine]
      }
    }
    return null
  }
}