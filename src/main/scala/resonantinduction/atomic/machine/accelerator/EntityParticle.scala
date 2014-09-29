package resonantinduction.atomic.machine.accelerator

import java.util.List

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData
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
import resonant.api.IElectromagnet
import resonant.lib.prefab.poison.PoisonRadiation
import resonantinduction.atomic.Atomic
import resonantinduction.core.Reference
import universalelectricity.core.transform.vector.Vector3
import scala.collection.JavaConversions._

/**
 * The particle entity used to determine the particle acceleration.
 */
object EntityParticle
{
    def canSpawnParticle(world: World, pos: Vector3): Boolean =
    {
        val block: Block = pos.getBlock(world)
        if (block != null && !block.isAir(world, pos.xi, pos.yi, pos.zi))
        {
            return false
        }
        for (i <- 0 to 1)
        {
            val dir: ForgeDirection = ForgeDirection.getOrientation(i)
            if (!isElectromagnet(world, pos, dir))
            {
                return false
            }
        }
        return true
    }

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

    private final val MOVE_TICK_RATE: Int = 20
}

class EntityParticle(par1World: World) extends Entity(par1World) with IEntityAdditionalSpawnData
{
    //Default Constructor
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

    def writeSpawnData(data: ByteBuf)
    {
        data.writeInt(this.movementVector.xi)
        data.writeInt(this.movementVector.yi)
        data.writeInt(this.movementVector.zi)
        data.writeInt(this.movementDirection.ordinal)
    }

    def readSpawnData(data: ByteBuf)
    {
        this.movementVector = new Vector3(data)
        this.movementDirection = ForgeDirection.getOrientation(data.readInt)
    }

    protected def entityInit
    {
        this.dataWatcher.addObject(EntityParticle.MOVE_TICK_RATE, 3.asInstanceOf[Byte])
        if (this.updateTicket == null)
        {
            this.updateTicket = ForgeChunkManager.requestTicket(Atomic.INSTANCE, this.worldObj, Type.ENTITY)
            this.updateTicket.getModData
            this.updateTicket.bindEntity(this)
        }
    }

    override def onUpdate
    {
        if (this.ticksExisted % 10 == 0)
        {
            this.worldObj.playSoundAtEntity(this, Reference.prefix + "accelerator", 1f, (0.6f + (0.4 * (this.getParticleVelocity / TileAccelerator.clientParticleVelocity))).asInstanceOf[Float])
        }
        val t: TileEntity = this.worldObj.getTileEntity(this.movementVector.xi, this.movementVector.yi, this.movementVector.zi)
        if (!(t.isInstanceOf[TileAccelerator]))
        {
            setDead
            return
        }
        val tileEntity: TileAccelerator = t.asInstanceOf[TileAccelerator]
        if (tileEntity.entityParticle == null)
        {
            tileEntity.entityParticle = this
        }
        for (x <- -1 to 1)
        {
            for (z <- -1 to 1)
            {
                ForgeChunkManager.forceChunk(this.updateTicket, new ChunkCoordIntPair((this.posX.asInstanceOf[Int] >> 4) + x, (this.posZ.asInstanceOf[Int] >> 4) + z))

            }
        }
        try
        {
            if (!this.worldObj.isRemote)
            {
                this.dataWatcher.updateObject(EntityParticle.MOVE_TICK_RATE, this.movementDirection.ordinal.asInstanceOf[Byte])
            }
            else
            {
                this.movementDirection = ForgeDirection.getOrientation(this.dataWatcher.getWatchableObjectByte(EntityParticle.MOVE_TICK_RATE))
            }
        }
        catch
            {
                case e: Exception =>
                {
                    e.printStackTrace
                }
            }
        var acceleration: Double = 0.0006f
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
            explode
            return
        }
        val dongLi: Vector3 = new Vector3
        dongLi.add(this.movementDirection)
        dongLi.multiply(acceleration)
        this.motionX = Math.min(dongLi.x + this.motionX, TileAccelerator.clientParticleVelocity)
        this.motionY = Math.min(dongLi.y + this.motionY, TileAccelerator.clientParticleVelocity)
        this.motionZ = Math.min(dongLi.z + this.motionZ, TileAccelerator.clientParticleVelocity)
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
        val bounds: AxisAlignedBB = AxisAlignedBB.getBoundingBox(this.posX - radius, this.posY - radius, this.posZ - radius, this.posX + radius, this.posY + radius, this.posZ + radius)
        val entitiesNearby: List[_] = this.worldObj.getEntitiesWithinAABB(classOf[Entity], bounds)
        if (entitiesNearby.size > 1)
        {
            this.explode
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

    def explode
    {
        this.worldObj.playSoundAtEntity(this, Reference.prefix + "antimatter", 1.5f, 1f - this.worldObj.rand.nextFloat * 0.3f)
        if (!this.worldObj.isRemote)
        {
            if (this.getParticleVelocity > TileAccelerator.clientParticleVelocity / 2)
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

    override def applyEntityCollision(par1Entity: Entity)
    {
        this.explode
    }

    override def setDead
    {
        ForgeChunkManager.releaseTicket(this.updateTicket)
        super.setDead
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

    var updateTicket: ForgeChunkManager.Ticket = null
    var didParticleCollide: Boolean = false
    private var lastTurn: Int = 60
    private var movementVector: Vector3 = new Vector3
    private var movementDirection: ForgeDirection = ForgeDirection.NORTH
}