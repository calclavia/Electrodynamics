package edx.mechanical.mech.gear

import codechicken.multipart.TileMultipart
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.prefab.tile.multiblock.reference.MultiBlockHandler
import resonant.lib.transform.vector.Vector3

class GearMultiBlockHandler(wrapper: PartGear) extends MultiBlockHandler[PartGear](wrapper: PartGear)
{
  override def getWrapperAt(position: Vector3): PartGear =
  {
    val tile = position.getTileEntity(this.tile.getWorld)

    if (tile.isInstanceOf[TileMultipart])
    {
      val part = tile.asInstanceOf[TileMultipart].partMap(getPlacementSide.ordinal)

      if (part.isInstanceOf[PartGear])
      {
        if ((part.asInstanceOf[PartGear]).tier == this.tile.tier)
        {
          return part.asInstanceOf[PartGear]
        }
      }
    }
    return null
  }

  def getPlacementSide: ForgeDirection = tile.placementSide
}