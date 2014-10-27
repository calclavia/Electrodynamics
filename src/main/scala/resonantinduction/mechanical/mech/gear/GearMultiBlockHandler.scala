package resonantinduction.mechanical.mech.gear

import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.multiblock.reference.MultiBlockHandler
import resonant.lib.transform.vector.Vector3
import codechicken.multipart.TMultiPart
import codechicken.multipart.TileMultipart

class GearMultiBlockHandler(wrapper: PartGear) extends MultiBlockHandler[PartGear](wrapper: PartGear)
{

    override def getWrapperAt(position: Vector3): PartGear =
    {
        val tile: TileEntity = position.getTileEntity(this.tile.getWorld)
        if (tile.isInstanceOf[TileMultipart])
        {
            val part: TMultiPart = (tile.asInstanceOf[TileMultipart]).partMap(getPlacementSide.ordinal)
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

    def getPlacementSide: ForgeDirection =
    {
        return tile.placementSide
    }
}