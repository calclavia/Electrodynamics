package resonantinduction.mechanical.mech.gear

import codechicken.lib.vec.Rotation
import codechicken.multipart.{TMultiPart, TileMultipart}
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.grid.node.DCNode
import resonantinduction.core.interfaces.IMechanicalNode
import resonantinduction.mechanical.mech.MechanicalNode
import resonantinduction.mechanical.mech.gearshaft.PartGearShaft
import resonant.api.grid.INodeProvider
import resonant.lib.transform.vector.{IVectorWorld, Vector3, VectorWorld}

/**
 * Node for the gear
 *
 * @author Calclavia, Edited by: Darkguardsman
 */
class GearNode(parent: PartGear) extends MechanicalNode(parent: PartGear)
{

    protected def gear: PartGear =
    {
        return this.getParent.asInstanceOf[PartGear]
    }

    override def onUpdate
    {
        super.onUpdate
        if (!gear.getMultiBlock.isPrimary)
        {
            torque = 0
            angularVelocity = 0
        }
        else if (gear.tier == 10)
        {
            torque = 100
            angularVelocity = 100
        }
    }

    override def getTorqueLoad: Double =
    {
        if (gear.tier == 1) return 0.2
        if (gear.tier == 2) return 0.1
        if (gear.tier == 0) return 0
        return 0.3
    }

    override def getAngularVelocityLoad: Double =
    {
        if (gear.tier == 1) return 0.2
        if (gear.tier == 2) return 0.1
        if (gear.tier == 0) return 0
        return 0.3
    }

    override def reconstruct()
    {
        connections.clear
        if (!gear.getMultiBlock.isPrimary || world == null)
        {
            return
        }
        val tileBehind: TileEntity = new Vector3(gear.tile).add(gear.placementSide).getTileEntity(world)
        if (tileBehind.isInstanceOf[INodeProvider])
        {
            val instance: MechanicalNode = (tileBehind.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], gear.placementSide.getOpposite).asInstanceOf[MechanicalNode]
            if (instance != null && instance != this && !(instance.getParent.isInstanceOf[PartGearShaft]) && instance.canConnect(this,gear.placementSide.getOpposite))
            {
                connect(instance, gear.placementSide)
            }
        }
        for (i <- 0 until 6)
        {
            val checkDir: ForgeDirection = ForgeDirection.getOrientation(i)
            var tile: TileEntity = gear.tile
            if (gear.getMultiBlock.isConstructed && checkDir != gear.placementSide && checkDir != gear.placementSide.getOpposite)
            {
                tile = new Vector3(gear.tile).add(checkDir).getTileEntity(world)
            }
            if (tile.isInstanceOf[INodeProvider])
            {
                val instance: MechanicalNode = (tile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], if (checkDir == gear.placementSide.getOpposite) ForgeDirection.UNKNOWN else checkDir).asInstanceOf[MechanicalNode]
                if (!directionMap.containsValue(checkDir) && instance != this && checkDir != gear.placementSide && instance != null && instance.canConnect(this,checkDir.getOpposite))
                {
                    connect(instance, checkDir)
                }
            }
        }
        var displaceCheck: Int = 1
        if (gear.getMultiBlock.isPrimary && gear.getMultiBlock.isConstructed)
        {
            displaceCheck = 2
        }
        for (i <- 0 until 4)
        {
            val checkDir: ForgeDirection = ForgeDirection.getOrientation(Rotation.rotateSide(gear.placementSide.ordinal, i))
            val checkTile: TileEntity = new Vector3(gear.tile).add(checkDir).getTileEntity(world)
            if (!directionMap.containsValue(checkDir) && checkTile.isInstanceOf[INodeProvider])
            {
                val instance: MechanicalNode = (checkTile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], gear.placementSide).asInstanceOf[MechanicalNode]
                if (instance != null && instance != this && instance.canConnect(this,checkDir.getOpposite) && !(instance.getParent.isInstanceOf[PartGearShaft]))
                {
                    connect(instance, checkDir)
                }
            }
        }
    }

    /**
     * Can this gear be connected BY the source?
     *
     * @param from - Direction source is coming from.
     * @param other - The source of the connection.
     * @return True is so.
     */
    override def canConnect[B](other: B, from: ForgeDirection): Boolean =
    {
        if (!gear.getMultiBlock.isPrimary)
        {
            return false
        }
        if (other.isInstanceOf[MechanicalNode])
        {
            val parent: INodeProvider = (other.asInstanceOf[MechanicalNode]).getParent
            if (from == gear.placementSide.getOpposite)
            {
                if (parent.isInstanceOf[PartGear] || parent.isInstanceOf[PartGearShaft])
                {
                    if (parent.isInstanceOf[PartGearShaft])
                    {
                        val shaft: PartGearShaft = parent.asInstanceOf[PartGearShaft]
                        return shaft.tile.partMap(from.getOpposite.ordinal) != gear && Math.abs(shaft.placementSide.offsetX) == Math.abs(gear.placementSide.offsetX) && Math.abs(shaft.placementSide.offsetY) == Math.abs(gear.placementSide.offsetY) && Math.abs(shaft.placementSide.offsetZ) == Math.abs(gear.placementSide.offsetZ)
                    }
                    else if (parent.isInstanceOf[PartGear])
                    {
                        if ((parent.asInstanceOf[PartGear]).tile == gear.tile && !gear.getMultiBlock.isConstructed)
                        {
                            return true
                        }
                        if ((parent.asInstanceOf[PartGear]).placementSide ne gear.placementSide)
                        {
                            val part: TMultiPart = gear.tile.partMap((parent.asInstanceOf[PartGear]).placementSide.ordinal)
                            if (part.isInstanceOf[PartGear])
                            {
                                val sourceGear: PartGear = part.asInstanceOf[PartGear]
                                if (sourceGear.isCenterMultiBlock && !sourceGear.getMultiBlock.isPrimary)
                                {
                                    return true
                                }
                            }
                            else
                            {
                                if (gear.getMultiBlock.isConstructed)
                                {
                                    val checkPart: TMultiPart = (parent.asInstanceOf[PartGear]).tile.partMap(gear.placementSide.ordinal)
                                    if (checkPart.isInstanceOf[PartGear])
                                    {
                                        val requiredDirection: ForgeDirection = (checkPart.asInstanceOf[PartGear]).getPosition.subtract(position).toForgeDirection
                                        return (checkPart.asInstanceOf[PartGear]).isCenterMultiBlock && (parent.asInstanceOf[PartGear]).placementSide == requiredDirection
                                    }
                                }
                            }
                        }
                    }
                }
                val sourceTile: TileEntity = position.add(from.getOpposite).getTileEntity(world)
                if (sourceTile.isInstanceOf[INodeProvider])
                {
                    val sourceInstance: MechanicalNode = (sourceTile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], from).asInstanceOf[MechanicalNode]
                    return sourceInstance == other
                }
            }
            else if (from == gear.placementSide)
            {
                val sourceTile: TileEntity = position.add(from).getTileEntity(world)
                if (sourceTile.isInstanceOf[INodeProvider])
                {
                    val sourceInstance: MechanicalNode = (sourceTile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], from.getOpposite).asInstanceOf[MechanicalNode]
                    return sourceInstance == other
                }
            }
            else
            {
                val destinationTile: TileEntity = (other.asInstanceOf[MechanicalNode]).position.add(from.getOpposite).getTileEntity(world)
                if (destinationTile.isInstanceOf[INodeProvider] && destinationTile.isInstanceOf[TileMultipart])
                {
                    val destinationPart: TMultiPart = (destinationTile.asInstanceOf[TileMultipart]).partMap(gear.placementSide.ordinal)
                    if (destinationPart.isInstanceOf[PartGear])
                    {
                        if (gear ne destinationPart)
                        {
                            return (destinationPart.asInstanceOf[PartGear]).isCenterMultiBlock
                        }
                        else
                        {
                            return true
                        }
                    }
                    else
                    {
                        return true
                    }
                }
            }
        }
        return false
    }

  override def getRadius(dir: ForgeDirection, `with`: IMechanicalNode): Double =
    {
        val deltaPos: Vector3 = new VectorWorld(`with`.asInstanceOf[IVectorWorld]).subtract(position)
        val caseX: Boolean = gear.placementSide.offsetX != 0 && deltaPos.y == 0 && deltaPos.z == 0
        val caseY: Boolean = gear.placementSide.offsetY != 0 && deltaPos.x == 0 && deltaPos.z == 0
        val caseZ: Boolean = gear.placementSide.offsetZ != 0 && deltaPos.x == 0 && deltaPos.y == 0
        if (caseX || caseY || caseZ)
        {
            return super.getRadius(dir, `with`)
        }
        return if (gear.getMultiBlock.isConstructed) 1.5f else super.getRadius(dir, `with`)
    }
}