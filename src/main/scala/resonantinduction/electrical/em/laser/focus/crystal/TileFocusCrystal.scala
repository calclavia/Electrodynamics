package resonantinduction.electrical.em.laser.focus.crystal

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.network.{NetworkManager, Packet}
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.electrical.em.ElectromagneticCoherence
import resonantinduction.electrical.em.laser.focus.IFocus
import resonantinduction.electrical.em.laser.{ILaserHandler, Laser, TileBase}

/**
 * Redirects lasers to one point
 * @author Calclavia
 */
class TileFocusCrystal extends TileBase with ILaserHandler with IFocus
{
  var normal = new Vector3(0, 1, 0)

  var energy = 0D
  var color = new Vector3(1, 1, 1)

  override def updateEntity()
  {
    if (isPowered())
    {
      for (a <- 0 to 5)
      {
        val dir = ForgeDirection.getOrientation(a)
        val axis = new Vector3(dir)
        val rotateAngle = world.getIndirectPowerLevelTo(x + axis.x.toInt, y + axis.y.toInt, z + axis.z.toInt, a) * 15

        if (rotateAngle > 0)
        {
          normal = normal.rotate(Math.toRadians(rotateAngle), axis).normalize
        }
      }

      world.markBlockForUpdate(x, y, z)
    }

    if (energy > 0)
    {
      Laser.spawn(worldObj, position + 0.5 + normal * 0.9, position + 0.5, normal, color, energy)
      color = new Vector3(1, 1, 1)
      energy = 0;
    }
  }

  override def focus(newPosition: Vector3)
  {
    normal = ((newPosition - position) - 0.5).normalize
    world.markBlockForUpdate(x, y, z)
  }

  def setFocus(focus: Vector3)
  {
    normal = focus
  }

  override def getFocus: Vector3 = normal

  override def getCacheDirections: java.util.List[Vector3] = null

  override def onLaserHit(renderStart: Vector3, incidentDirection: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double): Boolean =
  {
    ElectromagneticCoherence.proxy.renderLaser(worldObj, renderStart, position + 0.5, color, energy)
    this.energy += energy
    this.color = (this.color + color) / 2
    return true
  }

  override def getDescriptionPacket: Packet =
  {
    val nbt = new NBTTagCompound()
    writeToNBT(nbt)
    return new S35PacketUpdateTileEntity(x, y, z, 0, nbt)
  }

  override def onDataPacket(net: NetworkManager, pkt: S35PacketUpdateTileEntity)
  {
    val receive = pkt.func_148857_g
    readFromNBT(receive)
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    normal = new Vector3(nbt.getCompoundTag("normal"))
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    val normalNBT = new NBTTagCompound()
    normal.writeToNBT(normalNBT)
    nbt.setTag("normal", normalNBT)
  }
}
