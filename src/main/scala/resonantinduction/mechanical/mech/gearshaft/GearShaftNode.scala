package resonantinduction.mechanical.mech.gearshaft

import java.util.ArrayList
import java.util.Iterator
import java.util.List
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.mechanical.mech.MechanicalNode
import resonantinduction.mechanical.mech.gear.PartGear
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.core.transform.vector.Vector3

class GearShaftNode(parent: INodeProvider) extends MechanicalNode(parent)
{

    override def getTorqueLoad: Double =
    {
        shaft.tier match
        {
            case _ =>
                return 0.03
            case 1 =>
                return 0.02
            case 2 =>
                return 0.01
        }
    }

    override def getAngularVelocityLoad: Double =
    {
        return 0
    }

    override def buildConnections
    {
        connections.clear
        val dirs: List[ForgeDirection] = new ArrayList[ForgeDirection]
        dirs.add(shaft.placementSide)
        dirs.add(shaft.placementSide.getOpposite)
        val it: Iterator[ForgeDirection] = dirs.iterator
        while (it.hasNext)
        {
            val checkDir: ForgeDirection = it.next
            if (checkDir == shaft.placementSide || checkDir == shaft.placementSide.getOpposite)
            {
                if (shaft.tile.isInstanceOf[INodeProvider])
                {
                    val instance: MechanicalNode = (shaft.tile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], checkDir).asInstanceOf[MechanicalNode]
                    if (instance != null && instance != this && instance.canConnect(checkDir.getOpposite, this))
                    {
                        addConnection(instance, checkDir)
                        it.remove
                    }
                }
            }
        }
        if (!dirs.isEmpty)
        {
            for (checkDir : ForgeDirection <- dirs)
            {
                if (!connections.containsValue(checkDir) && (checkDir == shaft.placementSide || checkDir == shaft.placementSide.getOpposite))
                {
                    val checkTile: TileEntity = new Vector3(shaft.tile).add(checkDir).getTileEntity(world)
                    if (checkTile.isInstanceOf[INodeProvider])
                    {
                        val instance: MechanicalNode = (checkTile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], checkDir.getOpposite).asInstanceOf[MechanicalNode]
                        if (instance != null && instance != this && instance.getParent.isInstanceOf[PartGearShaft] && instance.canConnect(checkDir.getOpposite, this))
                        {
                            addConnection(instance, checkDir)
                        }
                    }
                }
            }
        }
    }

    override def canConnect(from: ForgeDirection, source: AnyRef): Boolean =
    {
        if (source.isInstanceOf[MechanicalNode])
        {
            if ((source.asInstanceOf[MechanicalNode]).getParent.isInstanceOf[PartGear])
            {
                val gear: PartGear = (source.asInstanceOf[MechanicalNode]).getParent.asInstanceOf[PartGear]
                if (!(Math.abs(gear.placementSide.offsetX) == Math.abs(shaft.placementSide.offsetX) && Math.abs(gear.placementSide.offsetY) == Math.abs(shaft.placementSide.offsetY) && Math.abs(gear.placementSide.offsetZ) == Math.abs(shaft.placementSide.offsetZ)))
                {
                    return false
                }
            }
        }
        return from == shaft.placementSide || from == shaft.placementSide.getOpposite
    }

    override def inverseRotation(dir: ForgeDirection): Boolean =
    {
        if (shaft.placementSide.offsetY != 0 || shaft.placementSide.offsetZ != 0)
        {
            return dir eq shaft.placementSide.getOpposite
        }
        return dir eq shaft.placementSide
    }

    def shaft: PartGearShaft =
    {
        return this.getParent.asInstanceOf[PartGearShaft]
    }
}