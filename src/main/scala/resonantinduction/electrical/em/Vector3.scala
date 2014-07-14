package resonantinduction.electrical.em

import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import net.minecraft.util.{AxisAlignedBB, MovingObjectPosition, Vec3}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import java.lang.Double.doubleToLongBits
import net.minecraft.entity.Entity
import scala.collection.convert.wrapAsScala._

/**
 * @author Calclavia
 */
class Vector3
{
  var x = 0D
  var y = 0D
  var z = 0D

  def this(newX: Double, newY: Double, newZ: Double)
  {
    this()
    this.x = newX
    this.y = newY
    this.z = newZ
  }

  def this(yaw: Double, pitch: Double)
  {
    this(-Math.sin(Math.toRadians(yaw)), Math.sin(Math.toRadians(pitch)), -Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)))
  }

  def this(tile: TileEntity)
  {
    this(tile.xCoord, tile.yCoord, tile.zCoord)
  }

  def this(entity: Entity)
  {
    this(entity.posX, entity.posY, entity.posZ)
  }

  def this(vec: Vec3)
  {
    this(vec.xCoord, vec.yCoord, vec.zCoord)
  }

  def this(nbt: NBTTagCompound)
  {
    this(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"))
  }

  def this(dir: ForgeDirection)
  {
    this(dir.offsetX, dir.offsetY, dir.offsetZ)
  }

  def toVec3 = Vec3.createVectorHelper(x, y, z)

  /**
   * Operations
   */
  def +=(amount: Double): Vector3 =
  {
    x += amount
    y += amount
    z += amount
    return this
  }

  def -=(amount: Double): Vector3 =
  {
    this += -amount
    return this
  }

  def /=(amount: Double): Vector3 =
  {
    x /= amount
    y /= amount
    z /= amount
    return this
  }

  def *=(amount: Double): Vector3 =
  {
    x *= amount
    y *= amount
    z *= amount
    return this
  }

  def +(amount: Double): Vector3 =
  {
    return new Vector3(x + amount, y + amount, z + amount)
  }

  def +(amount: Vector3): Vector3 =
  {
    return new Vector3(x + amount.x, y + amount.y, z + amount.z)
  }

  def -(amount: Double): Vector3 =
  {
    return (this + -amount)
  }

  def -(amount: Vector3): Vector3 =
  {
    return (this + (amount * -1))
  }

  def /(amount: Double): Vector3 =
  {
    return new Vector3(x / amount, y / amount, z / amount)
  }

  def *(amount: Double): Vector3 =
  {
    return new Vector3(x * amount, y * amount, z * amount)
  }

  def $(other: Vector3) = x * other.x + y * other.y + z * other.z

  def x(other: Vector3): Vector3 = new Vector3(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)

  def magnitudeSquared = this $ this;

  def magnitude = Math.sqrt(magnitudeSquared)

  def normalize = this / magnitude

  def distance(other: Vector3) = (other - this).magnitude

  def rotate(angle: Double, axis: Vector3): Vector3 =
  {
    return translateMatrix(getRotationMatrix(angle, axis), this)
  }

  def getRotationMatrix(angle: Double, axis: Vector3): Seq[Double] = axis.getRotationMatrix(angle)

  def getRotationMatrix(newAngle: Double): Seq[Double] =
  {
    var angle = newAngle
    val matrix = new Array[Double](16)
    val axis = this.clone().normalize
    val x = axis.x
    val y = axis.y
    val z = axis.z
    angle *= 0.0174532925D
    val cos = Math.cos(angle)
    val ocos = 1.0F - cos
    val sin = Math.sin(angle)
    matrix(0) = (x * x * ocos + cos)
    matrix(1) = (y * x * ocos + z * sin)
    matrix(2) = (x * z * ocos - y * sin)
    matrix(4) = (x * y * ocos - z * sin)
    matrix(5) = (y * y * ocos + cos)
    matrix(6) = (y * z * ocos + x * sin)
    matrix(8) = (x * z * ocos + y * sin)
    matrix(9) = (y * z * ocos - x * sin)
    matrix(10) = (z * z * ocos + cos)
    matrix(15) = 1.0F
    return matrix
  }

  def translateMatrix(matrix: Seq[Double], translation: Vector3): Vector3 =
  {
    val x = translation.x * matrix(0) + translation.y * matrix(1) + translation.z * matrix(2) + matrix(3)
    val y = translation.x * matrix(4) + translation.y * matrix(5) + translation.z * matrix(6) + matrix(7)
    val z = translation.x * matrix(8) + translation.y * matrix(9) + translation.z * matrix(10) + matrix(11)
    translation.x = x
    translation.y = y
    translation.z = z
    return translation
  }

  def eulerAngles = new Vector3(Math.toDegrees(Math.atan2(x, z)), Math.toDegrees(-Math.atan2(y, Math.hypot(z, x))), 0)

  def rayTrace(world: World, end: Vector3): MovingObjectPosition =
  {
    val block = world.rayTraceBlocks(toVec3, end.toVec3)
    val entity = rayTraceEntities(world, end)

    if (block == null)
      return entity
    if (entity == null)
      return block

    if (distance(new Vector3(block.hitVec)) < distance(new Vector3(entity.hitVec)))
      return block

    return entity
  }

  def rayTraceEntities(world: World, end: Vector3): MovingObjectPosition =
  {
    var closestEntityMOP: MovingObjectPosition = null
    var closetDistance = 0D

    val checkDistance = distance(end)
    val scanRegion = AxisAlignedBB.getBoundingBox(-checkDistance, -checkDistance, -checkDistance, checkDistance, checkDistance, checkDistance).offset(x, y, z)

    val checkEntities = world.getEntitiesWithinAABB(classOf[Entity], scanRegion).map(_.asInstanceOf[Entity])

    checkEntities.foreach(
      entity =>
      {
        if (entity != null && entity.canBeCollidedWith && entity.boundingBox != null)
        {
          val border = entity.getCollisionBorderSize
          val bounds = entity.boundingBox.expand(border, border, border)
          val hit = bounds.calculateIntercept(toVec3, end.toVec3)

          if (hit != null)
          {

            if (bounds.isVecInside(toVec3))
            {
              if (0 < closetDistance || closetDistance == 0)
              {
                closestEntityMOP = new MovingObjectPosition(entity)

                closestEntityMOP.hitVec = hit.hitVec
                closetDistance = 0
              }
            }
            else
            {
              val dist = distance(new Vector3(hit.hitVec))

              if (dist < closetDistance || closetDistance == 0)
              {
                closestEntityMOP = new MovingObjectPosition(entity)
                closestEntityMOP.hitVec = hit.hitVec

                closetDistance = dist
              }
            }
          }
        }
      }
    )

    return closestEntityMOP
  }

  def writeToNBT(nbt: NBTTagCompound)
  {
    nbt.setDouble("x", x)
    nbt.setDouble("y", y)
    nbt.setDouble("z", z)
  }

  override def equals(o: Any): Boolean =
  {
    if (o.isInstanceOf[Vector3])
    {
      val other = o.asInstanceOf[Vector3]
      return other.x == x && other.y == y && other.z == z
    }

    return false
  }

  override def clone = new Vector3(x, y, z)

  override def toString = "Vector3[" + x + "," + y + "," + z + "]"

  override def hashCode(): Int =
  {
    val x = doubleToLongBits(this.x)
    val y = doubleToLongBits(this.y)
    val z = doubleToLongBits(this.z)
    var hash = (x ^ (x >>> 32))
    hash = 31 * hash + y ^ (y >>> 32)
    hash = 31 * hash + z ^ (z >>> 32)
    return hash.toInt;
  }

}