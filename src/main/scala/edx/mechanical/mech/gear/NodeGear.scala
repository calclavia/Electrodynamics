package edx.mechanical.mech.gear

import codechicken.lib.vec.Rotation
import codechicken.multipart.TileMultipart
import edx.core.interfaces.TNodeMechanical
import edx.mechanical.mech.gearshaft.{NodeGearShaft, PartGearShaft}
import edx.mechanical.mech.grid.NodeMechanical
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.api.graph.INodeProvider
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.wrapper.ForgeDirectionWrapper._

/**
 * Node for the gear
 *
 * @author Calclavia, Edited by: Darkguardsman
 */
class NodeGear(parent: PartGear) extends NodeMechanical(parent: PartGear)
{
  override def angleDisplacement = if (gear.getMultiBlock.isConstructed) Math.PI / 36 else Math.PI / 12

  protected def gear = getParent.asInstanceOf[PartGear]

  override def inertia: Double =
  {
    gear.tier match
    {
      case 0 => 50
      case 1 => 20
      case 2 => 15
    }
  }

  override def friction: Double =
  {
    gear.tier match
    {
      case 0 => 1
      case 1 => 1.5
      case 2 => 1.3
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

    //Check behind
    val tileBehind = new Vector3(gear.tile).add(gear.placementSide).getTileEntity(world)
    if (tileBehind.isInstanceOf[INodeProvider])
    {
      val other = tileBehind.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], gear.placementSide.getOpposite)

      if (other != null && other != this && !other.getParent.isInstanceOf[PartGearShaft] && other.canConnect(this, gear.placementSide.getOpposite))
      {
        connect(other, gear.placementSide)
      }
    }

    //Check internal
    for (i <- 0 until 6)
    {
      val toDir = ForgeDirection.getOrientation(i)
      var tile: TileEntity = gear.tile

      if (gear.getMultiBlock.isConstructed && toDir != gear.placementSide && toDir != gear.placementSide.getOpposite)
      {
        tile = new Vector3(gear.tile).add(toDir).getTileEntity(world)
      }

      if (tile.isInstanceOf[INodeProvider])
      {
        val other = tile.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], if (toDir == gear.placementSide.getOpposite) ForgeDirection.UNKNOWN else toDir)

        if (other != this && toDir != gear.placementSide && other != null && canConnect(other, toDir) && other.canConnect(this, toDir.getOpposite))
        {
          connect(other, toDir)
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

        if (other != null && other != this && canConnect(other, toDir) && other.canConnect(this, toDir.getOpposite) && !other.isInstanceOf[NodeGearShaft])
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

      if (from == gear.placementSide)
      {
        //This object is coming from the back of the gear

        //Check if it's a gear that's connected back-to-back
        if (otherParent.isInstanceOf[PartGear])
        {
          val otherGearPart = otherParent.asInstanceOf[PartGear]

          if (otherGearPart.placementSide == parent.placementSide.getOpposite)
          {
            //Check if it is the center of another gear (if it is a multiblock)
            return otherGearPart.getMultiBlock.isPrimary
          }
        }

        //It's not a gear. It might be be another tile node
        val sourceTile = toVectorWorld.add(from).getTileEntity(world)

        if (sourceTile.isInstanceOf[INodeProvider])
        {
          //Found a potential node. Check if it is actually adjacent to the gear.
          val sourceInstance = sourceTile.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], from)
          return sourceInstance == other
        }
      }
      else if (from == gear.placementSide.getOpposite)
      {
        //This object is from the front of the gear

        //Check if it's a gear internally
        if (otherParent.isInstanceOf[PartGear])
        {
          //Check internal gears
          if (otherParent.asInstanceOf[PartGear].tile == parent.tile && !parent.getMultiBlock.isConstructed)
          {
            return true
            //              otherParent.asInstanceOf[PartGear].placementSide != parent.placementSide.getOpposite
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
                val checkPart = otherParent.asInstanceOf[PartGear].tile.partMap(gear.placementSide.ordinal)
                if (checkPart.isInstanceOf[PartGear])
                {
                  val requiredDirection = checkPart.asInstanceOf[PartGear].getPosition.subtract(toVectorWorld).toForgeDirection
                  return checkPart.asInstanceOf[PartGear].isCenterMultiBlock && otherParent.asInstanceOf[PartGear].placementSide == requiredDirection
                }
              }
            }
          }
        }

        //Check if it's a shaft
        if (otherParent.isInstanceOf[PartGearShaft])
        {
          //We are connecting to a shaft.
          val shaft = otherParent.asInstanceOf[PartGearShaft]
          /*shaft.tile.partMap(from.getOpposite.ordinal) != gear && */
          return Math.abs(shaft.placementSide.offsetX) == Math.abs(gear.placementSide.offsetX) && Math.abs(shaft.placementSide.offsetY) == Math.abs(gear.placementSide.offsetY) && Math.abs(shaft.placementSide.offsetZ) == Math.abs(gear.placementSide.offsetZ)
        }
      }
      else if (from != ForgeDirection.UNKNOWN)
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
              val adjacentTile = toVectorWorld.add(from).getTileEntity

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

  override def inverseRotation(other: TNodeMechanical): Boolean =
  {
    if (other.isInstanceOf[NodeGearShaft])
    {
      return parent.placementSide.offset < Vector3.zero
    }

    return !other.isInstanceOf[NodeGearShaft]
  }

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