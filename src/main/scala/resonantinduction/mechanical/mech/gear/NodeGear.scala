package resonantinduction.mechanical.mech.gear

import codechicken.lib.vec.Rotation
import codechicken.multipart.{TMultiPart, TileMultipart}
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.grid.INodeProvider
import resonant.lib.transform.vector.Vector3
import resonant.lib.wrapper.ForgeDirectionWrapper._
import resonantinduction.core.interfaces.TNodeMechanical
import resonantinduction.mechanical.mech.gearshaft.{NodeGearShaft, PartGearShaft}
import resonantinduction.mechanical.mech.grid.NodeMechanical

/**
 * Node for the gear
 *
 * @author Calclavia, Edited by: Darkguardsman
 */
class NodeGear(parent: PartGear) extends NodeMechanical(parent: PartGear)
{
  override def angleDisplacement = if (gear.getMultiBlock.isConstructed) Math.PI / 36 else Math.PI / 12

  protected def gear = getParent.asInstanceOf[PartGear]

  override def getLoad: Double =
  {
    return gear.tier match
    {
      case 0 => 0.1
      case 1 => 0.2
      case 2 => 0.1
    }
  }

  override def reconstruct()
  {
    if (!parent.getMultiBlock.isPrimary)
    {
      parent.getMultiBlock.getPrimary.mechanicalNode.reconstruct()
    }

    super.reconstruct()
  }

  override def rebuild()
  {
    if (!gear.getMultiBlock.isPrimary || world == null)
    {
      return
    }

    val tileBehind = new Vector3(gear.tile).add(gear.placementSide).getTileEntity(world)
    if (tileBehind.isInstanceOf[INodeProvider])
    {
      val instance: NodeMechanical = tileBehind.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], gear.placementSide.getOpposite)
      if (instance != null && instance != this && !instance.getParent.isInstanceOf[PartGearShaft] && instance.canConnect(this, gear.placementSide.getOpposite))
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
        val instance: NodeMechanical = tile.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], if (checkDir == gear.placementSide.getOpposite) ForgeDirection.UNKNOWN else checkDir).asInstanceOf[NodeMechanical]
        if (!directionMap.containsValue(checkDir) && instance != this && checkDir != gear.placementSide && instance != null && instance.canConnect(this, checkDir.getOpposite))
        {
          connect(instance, checkDir)
        }
      }
    }

    //TODO: Change this to the radius of the gear
    val checkDisplacement = if (gear.getMultiBlock.isPrimary && gear.getMultiBlock.isConstructed) 2 else 1

    //Check the sides of the gear for any other gear connections
    for (i <- 0 until 4)
    {
      val toDir = ForgeDirection.getOrientation(Rotation.rotateSide(gear.placementSide.ordinal, i))
      val checkTile = (new Vector3(gear.tile) + (new Vector3(toDir) * checkDisplacement)).getTileEntity(world)

      if (!directionMap.containsValue(toDir) && checkTile.isInstanceOf[INodeProvider])
      {
        val other = checkTile.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], gear.placementSide)

        if (other != null && other != this && canConnect(other, toDir.getOpposite) && other.canConnect(this, toDir) && !other.isInstanceOf[NodeGearShaft])
        {
          connect(other, toDir)
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
  override def canConnect[B <: NodeMechanical](other: B, from: ForgeDirection): Boolean =
  {
    if (!gear.getMultiBlock.isPrimary)
    {
      return false
    }

    if (other.isInstanceOf[NodeMechanical])
    {
      val otherParent = other.getParent

      if (from == gear.placementSide.getOpposite)
      {
        //This object is coming from the front of the gear
        if (otherParent.isInstanceOf[PartGear] || otherParent.isInstanceOf[PartGearShaft])
        {
          if (otherParent.isInstanceOf[PartGearShaft])
          {
            //We are connecting to a shaft.
            val shaft = otherParent.asInstanceOf[PartGearShaft]
            //Check if the shaft is directing connected to the center of the gear (multiblock cases) and also its direction to make sure the shaft is facing the gear itself
            return /*shaft.tile.partMap(from.getOpposite.ordinal) != gear && */ Math.abs(shaft.placementSide.offsetX) == Math.abs(gear.placementSide.offsetX) && Math.abs(shaft.placementSide.offsetY) == Math.abs(gear.placementSide.offsetY) && Math.abs(shaft.placementSide.offsetZ) == Math.abs(gear.placementSide.offsetZ)
          }
          else if (otherParent.isInstanceOf[PartGear])
          {
            if (otherParent.asInstanceOf[PartGear].tile == gear.tile && !gear.getMultiBlock.isConstructed)
            {
              return true
            }
            if (otherParent.asInstanceOf[PartGear].placementSide != gear.placementSide)
            {
              val part = gear.tile.partMap(otherParent.asInstanceOf[PartGear].placementSide.ordinal)
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
                  val checkPart: TMultiPart = otherParent.asInstanceOf[PartGear].tile.partMap(gear.placementSide.ordinal)
                  if (checkPart.isInstanceOf[PartGear])
                  {
                    val requiredDirection: ForgeDirection = checkPart.asInstanceOf[PartGear].getPosition.subtract(toVectorWorld).toForgeDirection
                    return checkPart.asInstanceOf[PartGear].isCenterMultiBlock && otherParent.asInstanceOf[PartGear].placementSide == requiredDirection
                  }
                }
              }
            }
          }
        }
        val sourceTile: TileEntity = toVectorWorld.add(from.getOpposite).getTileEntity(world)
        if (sourceTile.isInstanceOf[INodeProvider])
        {
          val sourceInstance = sourceTile.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], from)
          return sourceInstance == other
        }
      }
      else if (from == gear.placementSide)
      {
        //This object is from the back of the gear
        val sourceTile: TileEntity = toVectorWorld.add(from).getTileEntity(world)
        if (sourceTile.isInstanceOf[INodeProvider])
        {
          val sourceInstance: NodeMechanical = sourceTile.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], from.getOpposite)
          return sourceInstance == other
        }
      }
      else
      {
        //This object is from the sides of the gear. It can either be a gear within this block or outside
        if (other.isInstanceOf[NodeGear])
        {
          val otherParent = other.parent.asInstanceOf[PartGear]
          //Check inside this block
          if (otherParent.tile == parent.tile)
          {
            return otherParent.placementSide != parent.placementSide && otherParent != parent.placementSide.getOpposite
          }

          //Check for gear outside this block placed on the same plane
          val otherTile = other.toVectorWorld.getTileEntity

          if (otherTile.isInstanceOf[TileMultipart])
          {
            if (otherTile.asInstanceOf[TileMultipart].partMap(gear.placementSide.ordinal()) == other.parent)
            {
              //We found another gear, but check if we are connecting to the center spaces of the gear
              //If this is a multiblock, "otherTile" would be the center of that gear, not the adjacent
              val adjacentTile = toVectorWorld.add(from.getOpposite).getTileEntity

              if (adjacentTile.isInstanceOf[TileMultipart])
              {
                val adjacentPart = adjacentTile.asInstanceOf[TileMultipart].partMap(gear.placementSide.ordinal)
                return adjacentPart.asInstanceOf[PartGear].isCenterMultiBlock
              }
            }
          }
        }
      }
    }
    return false
  }

  override def inverseRotation(other: TNodeMechanical): Boolean = !other.isInstanceOf[NodeGearShaft] || (other.isInstanceOf[NodeGearShaft] && parent.placementSide.offset < Vector3.zero)

  override def radius(other: TNodeMechanical): Double =
  {
    val deltaPos = other.asInstanceOf[NodeMechanical].toVectorWorld - toVectorWorld
    val caseX = gear.placementSide.offsetX != 0 && deltaPos.y == 0 && deltaPos.z == 0
    val caseY = gear.placementSide.offsetY != 0 && deltaPos.x == 0 && deltaPos.z == 0
    val caseZ = gear.placementSide.offsetZ != 0 && deltaPos.x == 0 && deltaPos.y == 0

    if (caseX || caseY || caseZ)
      return super.radius(other)

    if (gear.getMultiBlock.isConstructed) 1.5 else super.radius(other)
  }
}