package resonantinduction.electrical.wire

import codechicken.multipart.TMultiPart
import ic2.api.energy.event.{EnergyTileLoadEvent, EnergyTileUnloadEvent}
import ic2.api.energy.tile.IEnergyTile
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.core.prefab.part.TraitPart
import universalelectricity.compatibility.Compatibility
import universalelectricity.core.transform.vector.{Vector3, VectorWorld}

import scala.util.control.Breaks._

@deprecated
abstract class PartConductor extends TMultiPart with TraitPart
{
  override def doesTick: Boolean =
  {
    return false
  }

  def getConnections: Array[AnyRef] =
  {
    return this.connections
  }

  /** EXTERNAL USE Can this wire be connected by another block? */
  def canConnect(direction: ForgeDirection, source: AnyRef): Boolean =
  {
    val connectPos: Vector3 = new Vector3(tile).add(direction)
    val connectTile: TileEntity = connectPos.getTileEntity(world)
    return Compatibility.isHandler(connectTile)
  }

  def canConnectTo(obj: AnyRef): Boolean

  /** Recalculates all the network connections */
  protected def recalculateConnections
  {
    this.connections = new Array[AnyRef](6)
    for (i <- 0 until 6)
    {
      val side: ForgeDirection = ForgeDirection.getOrientation(i)
      val tileEntity: TileEntity = new VectorWorld(world, x, y, z).getTileEntity
      if (this.canConnect(side, tileEntity))
      {
        connections(i) = tileEntity
      }
    }
  }

  /** IC2 Functions */
  override def onWorldJoin
  {
    if (!world.isRemote && tile.isInstanceOf[IEnergyTile])
    {
      var foundAnotherPart: Boolean = false

      for (i <- 0 until tile.partList.size)
      {
        val part: TMultiPart = tile.partMap(i)
        if (part.isInstanceOf[IEnergyTile] && part != this)
        {
          foundAnotherPart = true
          break
        }
      }
      if (!foundAnotherPart)
      {
        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(tile.asInstanceOf[IEnergyTile]))
      }
    }
  }

  override def preRemove
  {
    if (!world.isRemote)
    {
      //this.getNetwork.split(this)
      if (tile.isInstanceOf[IEnergyTile])
      {
        var foundAnotherPart: Boolean = false
        for (i <- 0 until tile.partList.size)
        {
          val part: TMultiPart = tile.partMap(i)
          if (part.isInstanceOf[IEnergyTile] && part != this)
          {
            foundAnotherPart = true
            break //todo: break is not supported
          }

        }
        if (!foundAnotherPart)
        {
          MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(tile.asInstanceOf[IEnergyTile]))
        }
      }
    }
    super.preRemove
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
  }

  override def toString: String =
  {
    return "[PartConductor]" + x + "x " + y + "y " + z + "z "
  }

  protected var connections: Array[AnyRef] = new Array[AnyRef](6)
}