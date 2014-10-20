package resonantinduction.electrical.laser.focus.mirror

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.network.{NetworkManager, Packet}
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.core.ResonantInduction
import resonantinduction.electrical.laser.focus.IFocus
import resonantinduction.electrical.laser.{ILaserHandler, Laser, TileBase}
import universalelectricity.core.transform.rotation.Quaternion
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAsJava._

/**
 * @author Calclavia
 */
class TileMirror extends TileBase with ILaserHandler with IFocus
{
    var normal = new Vector3(0, 1, 0)

    private var cachedHits = List[Vector3]()

    override def update()
    {
        if (isPowered())
        {
            for (a <- 0 to 5)
            {
                val dir = ForgeDirection.getOrientation(a)
                val axis = new Vector3(dir)
                val rotateAngle = world.getIndirectPowerLevelTo(xi + axis.x.toInt, yi + axis.y.toInt, zi + axis.z.toInt, a) * 15

                if (rotateAngle > 0)
                {
                    normal = normal.transform(new Quaternion(Math.toRadians(rotateAngle), axis)).normalize
                }
            }

            world.markBlockForUpdate(xi, yi, zi)
        }

        if (world.getTotalWorldTime % 20 == 0)
            cachedHits = List()
    }

    override def focus(newPosition: Vector3)
    {
        normal = ((newPosition - asVector3) - 0.5).normalize
        world.markBlockForUpdate(xi, yi, zi)
    }

    def setFocus(focus: Vector3)
    {
        normal = focus
    }

    override def getFocus: Vector3 = normal

    override def getCacheDirections: java.util.List[Vector3] = cachedHits.toList

    override def onLaserHit(renderStart: Vector3, incidentDirection: Vector3, hit: MovingObjectPosition, color: Vector3, energy: Double): Boolean =
    {
        /**
         * Cache hits
         */
        cachedHits = incidentDirection :: cachedHits

        /**
         * Render incoming laser
         */
        ResonantInduction.proxy.renderLaser(worldObj, renderStart, asVector3 + 0.5, color, energy)

        /**
         * Calculate Reflection
         */
        val angle = Math.acos(incidentDirection $ normal)
        val axisOfReflection = incidentDirection.cross(normal)
        val rotateAngle = 180 - Math.toDegrees(2 * angle)

        if (Math.toDegrees(rotateAngle) < 180)
        {
            val newDirection = (incidentDirection.clone.transform(new Quaternion(rotateAngle, axisOfReflection))).normalize
            Laser.spawn(worldObj, asVector3 + 0.5 + newDirection * 0.9, asVector3 + 0.5, newDirection, color, energy / 1.2)
        }

        return true
    }

    override def getDescriptionPacket: Packet =
    {
        val nbt = new NBTTagCompound()
        writeToNBT(nbt)
        return new S35PacketUpdateTileEntity(xi, yi, zi, 0, nbt)
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
        normal.writeNBT(normalNBT)
        nbt.setTag("normal", normalNBT)
    }
}
