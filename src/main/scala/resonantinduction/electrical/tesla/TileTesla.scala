/**
 *
 */
package resonantinduction.electrical.tesla

import java.util.{ArrayList, Comparator, HashSet, List, PriorityQueue, Set}

import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.electric.EnergyStorage
import resonant.lib.content.prefab.TEnergyStorage
import resonant.lib.content.prefab.java.TileElectric
import resonant.lib.multiblock.reference.{IMultiBlockStructure, MultiBlockHandler}
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.{TPacketSender, TPacketIDReceiver}
import resonant.lib.render.EnumColor
import resonant.lib.transform.vector.{Vector3, VectorWorld}
import resonant.lib.utility.{LanguageUtility, LinkUtility}
import resonantinduction.core.util.ResonantUtil
import resonantinduction.core.{Reference, ResonantInduction, Settings}

import scala.collection.JavaConversions._

/**
 * The Tesla TileEntity.
 *
 * - Redstone (Prevent Output Toggle) - Right click (Prevent Input Toggle)
 *
 * @author Calclavia
 *
 */
object TileTesla
{
  final val DEFAULT_COLOR: Int = 12
}

class TileTesla extends TileElectric(Material.iron) with IMultiBlockStructure[TileTesla] with ITesla with TPacketIDReceiver with TPacketSender with TEnergyStorage
{

  final val TRANSFER_CAP: Double = 10000D
  var dyeID: Int = TileTesla.DEFAULT_COLOR
  var canReceive: Boolean = true
  var attackEntities: Boolean = true
  /** Client side to do sparks */
  var doTransfer: Boolean = true
  /** Prevents transfer loops */
  final val outputBlacklist: Set[TileTesla] = new HashSet[TileTesla]
  final val connectedTeslas: Set[TileTesla] = new HashSet[TileTesla]
  /**
   * Multiblock Methods.
   */
  private var multiBlock: MultiBlockHandler[TileTesla] = null
  /**
   * Quantum Tesla
   */
  var linked: Vector3 = null
  var linkDim: Int = 0
  /**
   * Client
   */
  var zapCounter: Int = 0
  var isLinkedClient: Boolean = false
  var isTransfering: Boolean = false
  var topCache: TileTesla = null

  //Constructor
  //TODO: Dummy
  energy = new EnergyStorage(0)
  energy.setCapacity(TRANSFER_CAP * 2)
  energy.setMaxTransfer(TRANSFER_CAP)
  setTextureName(Reference.prefix + "material_metal_side")
  normalRender(false)
  isOpaqueCube(false)

  override def start
  {
    super.start
    TeslaGrid.instance.register(this)
  }

  override def update
  {
    super.update
    if (this.getMultiBlock.isPrimary)
    {
      if (this.ticks % (4 + this.worldObj.rand.nextInt(2)) == 0 && ((this.worldObj.isRemote && isTransfering) || (!this.energy.isEmpty && !this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))))
      {
        val topTesla: TileTesla = this.getTopTelsa
        val topTeslaVector: Vector3 = asVector3
        if (this.linked != null || this.isLinkedClient)
        {
          if (!this.worldObj.isRemote)
          {
            val dimWorld: World = MinecraftServer.getServer.worldServerForDimension(this.linkDim)
            if (dimWorld != null)
            {
              val transferTile: TileEntity = this.linked.getTileEntity(dimWorld)
              if (transferTile.isInstanceOf[TileTesla] && !transferTile.isInvalid)
              {
                this.transfer((transferTile.asInstanceOf[TileTesla]), Math.min(energy.getEnergy, TRANSFER_CAP))
                if (this.zapCounter % 5 == 0 && Settings.SOUND_FXS)
                {
                  this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.prefix + "electricshock", this.energy.getEnergy.asInstanceOf[Float] / TRANSFER_CAP.asInstanceOf[Float], 1.3f - 0.5f * (this.dyeID / 16f))
                }
              }
            }
          }
          else
          {
            ResonantInduction.proxy.renderElectricShock(this.worldObj, topTeslaVector.clone.add(0.5), topTeslaVector.clone.add(new Vector3(0.5, java.lang.Double.POSITIVE_INFINITY, 0.5)), false)
          }
        }
        else
        {
          val teslaToTransfer: PriorityQueue[ITesla] = new PriorityQueue[ITesla](1024, new Comparator[ITesla]
          {
            def compare(o1: ITesla, o2: ITesla): Int =
            {
              val distance1: Double = asVector3.distance(new Vector3(o1.asInstanceOf[TileEntity]))
              val distance2: Double = asVector3.distance(new Vector3(o2.asInstanceOf[TileEntity]))
              if (distance1 < distance2)
              {
                return 1
              }
              else if (distance1 > distance2)
              {
                return -1
              }
              return 0
            }
          })

          for (o <- TeslaGrid.instance.get)
          {
            var otherTesla = o
            if (new Vector3(otherTesla.asInstanceOf[TileEntity]).distance(asVector3) < this.getRange && otherTesla != this)
            {
              if (otherTesla.isInstanceOf[TileTesla])
              {
                otherTesla = (otherTesla.asInstanceOf[TileTesla]).getMultiBlock.get
              }
              if (!connectedTeslas.contains(otherTesla) && otherTesla != this && otherTesla.canTeslaTransfer(this) && canTeslaTransfer(otherTesla.asInstanceOf[TileEntity]))
              {
                teslaToTransfer.add(otherTesla)
              }
            }
          }
          if (teslaToTransfer.size > 0)
          {
            val transferEnergy: Double = this.energy.getEnergy / teslaToTransfer.size
            val sentPacket: Boolean = false

            for (count <- 0 to 10)
            {
              if (!teslaToTransfer.isEmpty)
              {
                val tesla: ITesla = teslaToTransfer.poll
                if (this.zapCounter % 5 == 0 && Settings.SOUND_FXS)
                {
                  this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, Reference.prefix + "electricshock", this.energy.getEnergy.asInstanceOf[Float] / TRANSFER_CAP.asInstanceOf[Float], 1.3f - 0.5f * (this.dyeID / 16f))
                }
                var targetVector: Vector3 = new Vector3(tesla.asInstanceOf[TileEntity])
                var heightRange: Int = 1
                if (tesla.isInstanceOf[TileTesla])
                {
                  getMultiBlock.get.outputBlacklist.add(this)
                  targetVector = tesla.asInstanceOf[TileTesla].getTopTelsa.asVector3
                  heightRange = (tesla.asInstanceOf[TileTesla]).getHeight
                }
                val distance: Double = topTeslaVector.distance(targetVector)
                ResonantInduction.proxy.renderElectricShock(this.worldObj, topTesla.asVector3.add(new Vector3(0.5)), targetVector.add(new Vector3(0.5, Math.random * heightRange / 3 - heightRange / 3, 0.5)), EnumColor.DYES(this.dyeID).toColor)
                this.transfer(tesla, Math.min(transferEnergy, TRANSFER_CAP))
                if (!sentPacket && transferEnergy > 0)
                {
                  this.sendPacket(3)
                }
              }
            }

          }
        }
        this.zapCounter += 1
        this.outputBlacklist.clear
        this.doTransfer = false
      }
      if (!this.worldObj.isRemote && this.energy.didEnergyStateChange)
      {
        this.sendPacket(2)
      }
    }
    this.topCache = null
  }

  private def transfer(tesla: ITesla, transferEnergy: Double)
  {
    if (transferEnergy > 0)
    {
      tesla.teslaTransfer(transferEnergy, true)
      this.teslaTransfer(-transferEnergy, true)
    }
  }

  def canTeslaTransfer(tileEntity: TileEntity): Boolean =
  {
    if (tileEntity.isInstanceOf[TileTesla])
    {
      val otherTesla: TileTesla = tileEntity.asInstanceOf[TileTesla]
      if (!(otherTesla.getDye == dyeID || (otherTesla.getDye == TileTesla.DEFAULT_COLOR || dyeID == TileTesla.DEFAULT_COLOR)))
      {
        return false
      }
    }
    return canReceive && tileEntity != getMultiBlock.get && !this.outputBlacklist.contains(tileEntity)
  }

  override def getDescPacket: PacketTile =
  {
    return new PacketTile(this, this.getPacketData(1).toArray)
  }

  /**
   * 1 - Description Packet
   * 2 - Energy Update
   * 3 - Tesla Beam
   */
  def getPacketData(`type`: Int): ArrayList[Any] =
  {
    val data: ArrayList[Any] = new ArrayList[Any]
    data.add(`type`.asInstanceOf[Byte])
    if (`type` == 1)
    {
      data.add(this.dyeID)
      data.add(this.canReceive)
      data.add(this.attackEntities)
      data.add(this.linked != null)
      val nbt: NBTTagCompound = new NBTTagCompound
      getMultiBlock.save(nbt)
      data.add(nbt)
    }
    if (`type` == 2)
    {
      data.add(this.energy.getEnergy > 0)
    }
    return data
  }

  override def read(data: ByteBuf, id: Int, player: EntityPlayer, `type`: PacketType): Boolean =
  {
    if (id == 1)
    {
      this.dyeID = data.readInt
      this.canReceive = data.readBoolean
      this.attackEntities = data.readBoolean
      this.isLinkedClient = data.readBoolean
      getMultiBlock.load(ByteBufUtils.readTag(data))
      return true
    }
    else
    if (id == 2)
    {
      this.isTransfering = data.readBoolean
      return true
    }
    else
    if (id == 3)
    {
      this.doTransfer = true
      return true
    }
    return false
  }

  def teslaTransfer(e: Double, doTransfer: Boolean): Double =
  {
    var transferEnergy = e
    if (getMultiBlock.isPrimary)
    {
      if (doTransfer)
      {
        this.energy.receiveEnergy(transferEnergy, true)
        if (this.energy.didEnergyStateChange)
        {
          this.sendPacket(2)
        }
      }
      return transferEnergy
    }
    else
    {
      if (this.energy.getEnergy > 0)
      {
        transferEnergy += this.energy.getEnergy
        this.energy.setEnergy(0)
      }
      return getMultiBlock.get.teslaTransfer(transferEnergy, doTransfer)
    }
  }

  def getRange: Int =
  {
    return Math.min(4 * (this.getHeight - 1), 50)
  }

  def updatePositionStatus
  {
    val mainTile: TileTesla = getLowestTesla
    mainTile.getMultiBlock.deconstruct
    mainTile.getMultiBlock.construct
    val isTop: Boolean = asVector3.add(new Vector3(0, 1, 0)).getTileEntity(this.worldObj).isInstanceOf[TileTesla]
    val isBottom: Boolean = asVector3.add(new Vector3(0, -1, 0)).getTileEntity(this.worldObj).isInstanceOf[TileTesla]
    if (isTop && isBottom)
    {
      this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 3)
    }
    else if (isBottom)
    {
      this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 2, 3)
    }
    else
    {
      this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 0, 3)
    }
  }

  /**
   * Called only on bottom.
   *
   * @return The highest Tesla coil in this tower.
   */
  def getTopTelsa: TileTesla =
  {
    if (this.topCache != null)
    {
      return this.topCache
    }
    this.connectedTeslas.clear
    val checkPosition: Vector3 = asVector3
    var returnTile: TileTesla = this
    var exit = false
    while (exit)
    {
      val t: TileEntity = checkPosition.getTileEntity(this.worldObj)
      if (t.isInstanceOf[TileTesla])
      {
        this.connectedTeslas.add(t.asInstanceOf[TileTesla])
        returnTile = t.asInstanceOf[TileTesla]
        checkPosition.add(0, 1, 0)
      }
      else
      {
        exit = true
      }
    }
    this.topCache = returnTile
    return returnTile
  }

  /** Gets color of the link */
  def getDye: Int = dyeID

  /**
   * Called only on bottom.
   *
   * @return The highest Tesla coil in this tower.
   */
  def getHeight: Int =
  {
    this.connectedTeslas.clear
    var y: Int = 0
    var exit = false
    while (!exit)
    {
      val t: TileEntity = asVector3.add(new Vector3(0, y, 0)).getTileEntity(this.worldObj)
      if (t.isInstanceOf[TileTesla])
      {
        this.connectedTeslas.add(t.asInstanceOf[TileTesla])
        y += 1
      }
      else
      {
        exit = true
      }
    }
    return y
  }

  override def invalidate
  {
    TeslaGrid.instance.unregister(this)
    super.invalidate
  }

  def setDye(id: Int)
  {
    this.dyeID = id
    this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord)
  }

  def toggleReceive: Boolean =
  {
    this.canReceive = !this.canReceive
    return canReceive
  }

  def toggleEntityAttack: Boolean =
  {
    this.attackEntities = !this.attackEntities
    val returnBool: Boolean = attackEntities
    this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord)
    return returnBool
  }

  /**
   * Reads a tile entity from NBT.
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.dyeID = nbt.getInteger("dyeID")
    this.canReceive = nbt.getBoolean("canReceive")
    this.attackEntities = nbt.getBoolean("attackEntities")
    if (nbt.hasKey("link_x") && nbt.hasKey("link_y") && nbt.hasKey("link_z"))
    {
      this.linked = new Vector3(nbt.getInteger("link_x"), nbt.getInteger("link_y"), nbt.getInteger("link_z"))
      this.linkDim = nbt.getInteger("linkDim")
    }
    getMultiBlock.load(nbt)
  }

  /**
   * Writes a tile entity to NBT.
   */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("dyeID", this.dyeID)
    nbt.setBoolean("canReceive", this.canReceive)
    nbt.setBoolean("attackEntities", this.attackEntities)
    if (this.linked != null)
    {
      nbt.setInteger("link_x", this.linked.x.asInstanceOf[Int])
      nbt.setInteger("link_y", this.linked.y.asInstanceOf[Int])
      nbt.setInteger("link_z", this.linked.z.asInstanceOf[Int])
      nbt.setInteger("linkDim", this.linkDim)
    }
    getMultiBlock.save(nbt)
  }

  def setLink(vector3: Vector3, dimID: Int, setOpponent: Boolean)
  {
    if (!worldObj.isRemote)
    {
      val otherWorld: World = MinecraftServer.getServer.worldServerForDimension(linkDim)
      if (setOpponent && linked != null && otherWorld != null)
      {
        val tileEntity: TileEntity = linked.getTileEntity(otherWorld)
        if (tileEntity.isInstanceOf[TileTesla])
        {
          (tileEntity.asInstanceOf[TileTesla]).setLink(null, this.linkDim, false)
        }
      }
      linked = vector3
      linkDim = dimID
      worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord)
      val newOtherWorld: World = MinecraftServer.getServer.worldServerForDimension(this.linkDim)
      if (setOpponent && newOtherWorld != null && this.linked != null)
      {
        val tileEntity: TileEntity = this.linked.getTileEntity(newOtherWorld)
        if (tileEntity.isInstanceOf[TileTesla])
        {
          (tileEntity.asInstanceOf[TileTesla]).setLink(asVector3, this.worldObj.provider.dimensionId, false)
        }
      }
    }
  }

  def tryLink(vector: VectorWorld): Boolean =
  {
    if (vector != null)
    {
      if (vector.getTileEntity.isInstanceOf[TileTesla])
      {
        setLink(vector, vector.world.provider.dimensionId, true)
      }
      return true
    }
    return false
  }

  def onMultiBlockChanged
  {
  }

  def getMultiBlockVectors: java.lang.Iterable[Vector3] =
  {
    val vectors: List[Vector3] = new ArrayList[Vector3]
    val checkPosition: Vector3 = asVector3
    var exit = false
    while (!exit)
    {
      val t: TileEntity = checkPosition.getTileEntity(this.worldObj)
      if (t.isInstanceOf[TileTesla])
      {
        vectors.add(checkPosition.clone.subtract(getPosition))
      }
      else
      {
        exit = true
      }
      checkPosition.add(0, 1, 0)
    }
    return vectors
  }

  def getLowestTesla: TileTesla =
  {
    var lowest: TileTesla = this
    val checkPosition: Vector3 = asVector3
    var exit = false
    while (!exit)
    {
      val t: TileEntity = checkPosition.getTileEntity(this.worldObj)
      if (t.isInstanceOf[TileTesla])
      {
        lowest = t.asInstanceOf[TileTesla]
      }
      else
      {
        exit = true
      }
      checkPosition.add(0, -1, 0)
    }
    return lowest
  }

  def getWorld: World =
  {
    return worldObj
  }

  def getPosition: Vector3 =
  {
    return asVector3
  }

  def getMultiBlock: MultiBlockHandler[TileTesla] =
  {
    if (multiBlock == null) multiBlock = new MultiBlockHandler[TileTesla](this)
    return multiBlock
  }

  override def setIO(dir: ForgeDirection, `type`: Int)
  {
    if (getMultiBlock.isPrimary)
    {
      super.setIO(dir, `type`)
    }
    else
    {
      getMultiBlock.get.setIO(dir, `type`)
    }
  }

  override def getIO(dir: ForgeDirection): Int =
  {
    if (getMultiBlock.isPrimary)
    {
      return super.getIO(dir)
    }
    return getMultiBlock.get.getIO(dir)
  }

  override def use(entityPlayer: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (entityPlayer.getCurrentEquippedItem != null)
    {
      val dyeColor: Int = ResonantUtil.isDye(entityPlayer.getCurrentEquippedItem)
      if (dyeColor != -1)
      {
        getMultiBlock.get.setDye(dyeColor)
        if (!entityPlayer.capabilities.isCreativeMode)
        {
          entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1)
        }
        return true
      }
      else if (entityPlayer.getCurrentEquippedItem.getItem eq Items.redstone)
      {
        val status: Boolean = getMultiBlock.get.toggleEntityAttack
        if (!entityPlayer.capabilities.isCreativeMode)
        {
          entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1)
        }
        if (!world.isRemote)
        {
          entityPlayer.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.tesla.toggleAttack").replace("%v", status + "")))
        }
        return true
      }
    }
    else
    {
      val receiveMode: Boolean = getMultiBlock.get.toggleReceive
      if (!world.isRemote)
      {
        entityPlayer.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.tesla.mode").replace("%v", receiveMode + "")))
      }
      return true
    }
    return false
  }

  override def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    val itemStack: ItemStack = player.getCurrentEquippedItem
    if (player.isSneaking)
    {
      if (tryLink(LinkUtility.getLink(itemStack)))
      {
        if (world.isRemote) player.addChatMessage(new ChatComponentText("Successfully linked devices."))
        LinkUtility.clearLink(itemStack)
      }
      else
      {
        if (world.isRemote) player.addChatMessage(new ChatComponentText("Marked link for device."))
        LinkUtility.setLink(itemStack, asVectorWorld)
      }
      return true
    }
    return super.configure(player, side, hit)
  }

  override def onNeighborChanged(id: Block)
  {
    updatePositionStatus
  }

}