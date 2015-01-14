package edx.quantum.machine.accelerator

import java.util.List

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData
import edx.core.{Electrodynamics, Reference}
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.entity.{Entity, EntityLiving}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.{ChunkCoordIntPair, World}
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.tile.IElectromagnet
import resonant.lib.prefab.poison.PoisonRadiation
import resonant.lib.transform.vector.Vector3

import scala.collection.JavaConversions._

/**
 * The particle entity used to determine the particle acceleration.
 */
object EntityParticle
{
  /** Speed by which a particle will turn into anitmatter */
  val ANITMATTER_CREATION_SPEED: Float = 0.9f
  val MOVEMENT_DIRECTION_DATAWATCHER_ID: Int = 20

  /**
   * Checks to see if a new particle can be spawned at the location.
   * @param world - world to check in
   * @param pos - location to check
   * @return true if the spawn location is clear and 2 electromagnets are next to the location
   */
  def canSpawnParticle(world: World, pos: Vector3): Boolean =
  {
    val block: Block = pos.getBlock(world)
    if (block == null || !block.isAir(world, pos.xi, pos.yi, pos.zi))
    {
      var electromagnetCount = 0
      for (i <- 0 until 6)
      {
        val dir: ForgeDirection = ForgeDirection.getOrientation(i)
        if (isElectromagnet(world, pos, dir))
        {
          electromagnetCount += 1
        }
      }
      return electromagnetCount >= 2
    }
    return false
  }

  /**
   * Checks to see if the block is an instance of IElectromagnet and is turned on
   * @param world - world to check in
   * @param position - position to look for the block/tile
   * @param dir - direction to check in
   * @return true if the location contains an active electromagnet block
   */
  def isElectromagnet(world: World, position: Vector3, dir: ForgeDirection): Boolean =
  {
    val checkPos: Vector3 = position.clone.add(dir)
    val tile: TileEntity = checkPos.getTileEntity(world)
    if (tile.isInstanceOf[IElectromagnet])
    {
      return (tile.asInstanceOf[IElectromagnet]).isRunning
    }
    return false
  }
}

class EntityParticle(par1World: World) extends Entity(par1World) with IEntityAdditionalSpawnData
{
  var updateTicket: ForgeChunkManager.Ticket = null
  var didParticleCollide: Boolean = false
  private var lastTurn: Int = 60
  private var movementVector: Vector3 = new Vector3
  private var movementDirection: ForgeDirection = ForgeDirection.NORTH

  //Constructor
  this.setSize(0.3f, 0.3f)
  this.renderDistanceWeight = 4f
  this.ignoreFrustumCheck = true

  def this(world: World, pos: Vector3, movementVec: Vector3, dir: ForgeDirection)
  {
    this(world)
    this.setPosition(pos.x, pos.y, pos.z)
    this.movementVector = movementVec
    this.movementDirection = dir
  }

  override def writeSpawnData(data: ByteBuf)
  {
    data.writeInt(this.movementVector.xi)
    data.writeInt(this.movementVector.yi)
    data.writeInt(this.movementVector.zi)
    data.writeInt(this.movementDirection.ordinal)
  }

  override def readSpawnData(data: ByteBuf)
  {
    this.movementVector = new Vector3(data)
    this.movementDirection = ForgeDirection.getOrientation(data.readInt)
  }

  override def onUpdate
  {
    val t: TileEntity = this.worldObj.getTileEntity(this.movementVector.xi, this.movementVector.yi, this.movementVector.zi)
    val tileEntity: TileAccelerator = if (t != null && t.isInstanceOf[TileAccelerator]) t.asInstanceOf[TileAccelerator] else null
    var acceleration: Double = 0.0006f

    if (this.ticksExisted % 10 == 0)
    {
      this.worldObj.playSoundAtEntity(this, Reference.prefix + "accelerator", 1f, (0.6f + (0.4 * (this.getParticleVelocity / EntityParticle.ANITMATTER_CREATION_SPEED))).asInstanceOf[Float])
    }



    //Sanity check
    if (tileEntity == null)
    {
      setDead
      return
    }
    else if (tileEntity.entityParticle == null)
    {
      tileEntity.entityParticle = this
    }

    //Force load chunks TODO calculate direction so to only load two chunks instead of 5
    for (x <- -1 to 1)
    {
      for (z <- -1 to 1)
      {
        ForgeChunkManager.forceChunk(this.updateTicket, new ChunkCoordIntPair((this.posX.asInstanceOf[Int] >> 4) + x, (this.posZ.asInstanceOf[Int] >> 4) + z))

      }
    }

    //Update data watcher
    if (!this.worldObj.isRemote)
    {
      this.dataWatcher.updateObject(EntityParticle.MOVEMENT_DIRECTION_DATAWATCHER_ID, this.movementDirection.ordinal.asInstanceOf[Byte])
    }
    else
    {
      this.movementDirection = ForgeDirection.getOrientation(this.dataWatcher.getWatchableObjectByte(EntityParticle.MOVEMENT_DIRECTION_DATAWATCHER_ID))
    }


    if ((!EntityParticle.isElectromagnet(worldObj, new Vector3(this), movementDirection.getRotation(ForgeDirection.UP)) || !EntityParticle.isElectromagnet(worldObj, new Vector3(this), movementDirection.getRotation(ForgeDirection.DOWN))) && this.lastTurn <= 0)
    {
      acceleration = turn
      this.motionX = 0
      this.motionY = 0
      this.motionZ = 0
      this.lastTurn = 40
    }
    this.lastTurn -= 1
    if (!EntityParticle.canSpawnParticle(this.worldObj, new Vector3(this)) || this.isCollided)
    {
      handleCollisionWithEntity
      return
    }
    val dongLi: Vector3 = new Vector3
    dongLi.add(this.movementDirection)
    dongLi.multiply(acceleration)
    this.motionX = Math.min(dongLi.x + this.motionX, EntityParticle.ANITMATTER_CREATION_SPEED)
    this.motionY = Math.min(dongLi.y + this.motionY, EntityParticle.ANITMATTER_CREATION_SPEED)
    this.motionZ = Math.min(dongLi.z + this.motionZ, EntityParticle.ANITMATTER_CREATION_SPEED)
    this.isAirBorne = true
    this.lastTickPosX = this.posX
    this.lastTickPosY = this.posY
    this.lastTickPosZ = this.posZ
    this.moveEntity(this.motionX, this.motionY, this.motionZ)
    this.setPosition(this.posX, this.posY, this.posZ)
    if (this.lastTickPosX == this.posX && this.lastTickPosY == this.posY && this.lastTickPosZ == this.posZ && this.getParticleVelocity <= 0 && this.lastTurn <= 0)
    {
      this.setDead
    }
    this.worldObj.spawnParticle("portal", this.posX, this.posY, this.posZ, 0, 0, 0)
    this.worldObj.spawnParticle("largesmoke", this.posX, this.posY, this.posZ, 0, 0, 0)
    val radius: Float = 0.5f

    // Handle collision with entities TODO turn into a ray trace call so we know what we hit
    val bounds: AxisAlignedBB = AxisAlignedBB.getBoundingBox(this.posX - radius, this.posY - radius, this.posZ - radius, this.posX + radius, this.posY + radius, this.posZ + radius)
    val entitiesNearby: List[_] = this.worldObj.getEntitiesWithinAABB(classOf[Entity], bounds)
    if (entitiesNearby.size > 1)
    {
      this.handleCollisionWithEntity
      return
    }
  }

  /**
   * Try to move the particle left or right depending on which side is empty.
   *
   * @return The new velocity.
   */
  private def turn: Double =
  {
    //TODO rewrite to allow for up and down turning
    val RELATIVE_MATRIX: Array[Array[Int]] = Array(Array(3, 2, 1, 0, 5, 4), Array(4, 5, 0, 1, 2, 3), Array(0, 1, 3, 2, 4, 5), Array(0, 1, 2, 3, 5, 4), Array(0, 1, 5, 4, 3, 2), Array(0, 1, 4, 5, 2, 3))
    val zuoFangXiang: ForgeDirection = ForgeDirection.getOrientation(RELATIVE_MATRIX(this.movementDirection.ordinal)(ForgeDirection.EAST.ordinal))
    val zuoBian: Vector3 = new Vector3(this).floor
    zuoBian.add(zuoFangXiang)
    val youFangXiang: ForgeDirection = ForgeDirection.getOrientation(RELATIVE_MATRIX(this.movementDirection.ordinal)(ForgeDirection.WEST.ordinal))
    val youBian: Vector3 = new Vector3(this).floor
    youBian.add(youFangXiang)
    if (zuoBian.getBlock(this.worldObj) == null)
    {
      this.movementDirection = zuoFangXiang
    }
    else if (youBian.getBlock(this.worldObj) == null)
    {
      this.movementDirection = youFangXiang
    }
    else
    {
      setDead
      return 0
    }
    this.setPosition(Math.floor(this.posX) + 0.5, Math.floor(this.posY) + 0.5, Math.floor(this.posZ) + 0.5)
    return this.getParticleVelocity - (this.getParticleVelocity / Math.min(Math.max(70 * this.getParticleVelocity, 4), 30))
  }

  def handleCollisionWithEntity
  {
    this.worldObj.playSoundAtEntity(this, Reference.prefix + "antimatter", 1.5f, 1f - this.worldObj.rand.nextFloat * 0.3f)
    if (!this.worldObj.isRemote)
    {
      if (this.getParticleVelocity > EntityParticle.ANITMATTER_CREATION_SPEED / 2)
      {
        val radius: Float = 1f
        val bounds: AxisAlignedBB = AxisAlignedBB.getBoundingBox(this.posX - radius, this.posY - radius, this.posZ - radius, this.posX + radius, this.posY + radius, this.posZ + radius)
        val entitiesNearby: List[_] = this.worldObj.getEntitiesWithinAABB(classOf[EntityParticle], bounds)
        if (entitiesNearby.size > 0)
        {
          didParticleCollide = true
          setDead
          return
        }
      }
      this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, this.getParticleVelocity.asInstanceOf[Float] * 2.5f, true)
    }
    val radius: Float = 6
    val bounds: AxisAlignedBB = AxisAlignedBB.getBoundingBox(this.posX - radius, this.posY - radius, this.posZ - radius, this.posX + radius, this.posY + radius, this.posZ + radius)
    val livingNearby: List[_] = this.worldObj.getEntitiesWithinAABB(classOf[EntityLiving], bounds)

    for (entity <- livingNearby)
    {
      PoisonRadiation.INSTANCE.poisonEntity(new Vector3(entity.asInstanceOf[Entity]), entity.asInstanceOf[EntityLiving])
    }
    setDead
  }

  def getParticleVelocity: Double =
  {
    return Math.abs(this.motionX) + Math.abs(this.motionY) + Math.abs(this.motionZ)
  }

  override def setDead
  {
    ForgeChunkManager.releaseTicket(this.updateTicket)
    super.setDead
  }

  override def applyEntityCollision(par1Entity: Entity)
  {
    this.handleCollisionWithEntity
  }

  protected override def entityInit
  {
    this.dataWatcher.addObject(EntityParticle.MOVEMENT_DIRECTION_DATAWATCHER_ID, 3.asInstanceOf[Byte])
    if (this.updateTicket == null)
    {
      this.updateTicket = ForgeChunkManager.requestTicket(Electrodynamics, this.worldObj, Type.ENTITY)
      this.updateTicket.getModData
      this.updateTicket.bindEntity(this)
    }
  }

  protected def readEntityFromNBT(nbt: NBTTagCompound)
  {
    this.movementVector = new Vector3(nbt.getCompoundTag("jiqi"))
    ForgeDirection.getOrientation(nbt.getByte("fangXiang"))
  }

  protected def writeEntityToNBT(nbt: NBTTagCompound)
  {
    nbt.setTag("jiqi", this.movementVector.writeNBT(new NBTTagCompound))
    nbt.setByte("fangXiang", this.movementDirection.ordinal.asInstanceOf[Byte])
  }
}