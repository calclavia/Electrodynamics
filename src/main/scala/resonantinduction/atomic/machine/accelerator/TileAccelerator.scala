package resonantinduction.atomic.machine.accelerator

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.tile.{IRotatable, IElectromagnet}
import resonant.lib.content.prefab.TEnergyStorage
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.{TPacketIDReceiver, TPacketSender}
import resonant.lib.grid.energy.EnergyStorage
import resonant.lib.prefab.tile.TileElectricInventory
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.BlockUtility
import resonantinduction.atomic.AtomicContent
import resonantinduction.atomic.items.ItemAntimatter
import resonantinduction.core.{Reference, ResonantInduction, Settings}

import scala.collection.JavaConversions._

class TileAccelerator extends TileElectricInventory(Material.iron) with IElectromagnet with IRotatable with TPacketIDReceiver with TPacketSender with TEnergyStorage
{
  final val DESC_PACKET_ID = 2;
  /**
   * Multiplier that is used to give extra anti-matter based on density (hardness) of a given ore.
   */
  private var antiMatterDensityMultiplyer: Int = Settings.ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER
  /**
   * The total amount of energy consumed by this particle. In Joules.
   */
  var totalEnergyConsumed: Double = 0
  /**
   * The amount of anti-matter stored within the accelerator. Measured in milligrams.
   */
  var antimatter: Int = 0
  var entityParticle: EntityParticle = null
  var velocity: Float = 0
  var clientEnergy: Double = 0
  var lastSpawnTick: Int = 0

  //Constructor
  this.setSizeInventory(4)
  //TODO: Dummy
  energy = new EnergyStorage(0)
  energy.setCapacity(Settings.ACCELERATOR_ENERGY_COST_PER_TICK * 20)
  energy.setMaxTransfer(Settings.ACCELERATOR_ENERGY_COST_PER_TICK)

  override def update
  {
    super.update
    if (!worldObj.isRemote)
    {
      clientEnergy = energy.getEnergy
      velocity = getParticleVel()
      outputAntimatter()

      if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
      {
        if (energy.checkExtract)
        {
          if (entityParticle == null)
          {
            //Create new particle if we have materials to spawn it with
            if (getStackInSlot(0) != null && lastSpawnTick >= 40)
            {
              val spawn_vec: Vector3 = toVector3
              spawn_vec.add(getDirection.getOpposite)
              spawn_vec.add(0.5f)
              if (EntityParticle.canSpawnParticle(worldObj, spawn_vec))
              {
                totalEnergyConsumed = 0
                entityParticle = new EntityParticle(worldObj, spawn_vec, toVector3, getDirection.getOpposite)
                worldObj.spawnEntityInWorld(entityParticle)
                CalculateParticleDensity
                decrStackSize(0, 1)
                lastSpawnTick = 0
              }
            }
          }
          else
          {
            if (entityParticle.isDead)
            {
              //Handle strange matter creation
              if (entityParticle.didParticleCollide)
              {
                if (worldObj.rand.nextFloat <= Settings.darkMatterSpawnChance)
                {
                  incrStackSize(3, new ItemStack(AtomicContent.itemDarkMatter))
                }
              }
              entityParticle = null
            }
            else if (velocity > EntityParticle.ANITMATTER_CREATION_SPEED)
            {
              //Create antimatter if we have hit max speed
              worldObj.playSoundEffect(xCoord, yCoord, zCoord, Reference.prefix + "antimatter", 2f, 1f - worldObj.rand.nextFloat * 0.3f)
              val generatedAntimatter: Int = 5 + worldObj.rand.nextInt(antiMatterDensityMultiplyer)
              antimatter += generatedAntimatter

              //Cleanup
              totalEnergyConsumed = 0
              entityParticle.setDead
              entityParticle = null
            }
            if (entityParticle != null)
            {
              worldObj.playSoundEffect(xCoord, yCoord, zCoord, Reference.prefix + "accelerator", 1.5f, (0.6f + (0.4 * (entityParticle.getParticleVelocity) / EntityParticle.ANITMATTER_CREATION_SPEED)).asInstanceOf[Float])
            }
          }
          energy.extractEnergy
        }
        else
        {
          if (entityParticle != null)
          {
            entityParticle.setDead
          }
          entityParticle = null
        }
      }
      else
      {
        if (entityParticle != null)
        {
          entityParticle.setDead
        }
        entityParticle = null
      }
      if (ticks % 5 == 0)
      {
        for (player <- getPlayersUsing)
        {
          sendPacket(getDescPacket, player)
        }
      }
      lastSpawnTick += 1
    }
  }

  /**
   * Converts antimatter storage into item if the condition are meet
   */
  def outputAntimatter()
  {
    //Do we have an empty cell in slot one
    if (AtomicContent.isItemStackEmptyCell(getStackInSlot(1)) && getStackInSlot(1).stackSize > 0)
    {
      // Each cell can only hold 125mg of antimatter TODO maybe a config for this?
      if (antimatter >= 125)
      {
        if (getStackInSlot(2) != null)
        {
          // If the output slot is not empty we must increase stack size
          if (getStackInSlot(2).getItem == AtomicContent.itemAntimatter)
          {
            val newStack: ItemStack = getStackInSlot(2).copy
            if (newStack.stackSize < newStack.getMaxStackSize)
            {
              decrStackSize(1, 1)
              antimatter -= 125
              newStack.stackSize += 1
              setInventorySlotContents(2, newStack)
            }
          }
        }
        else
        {
          //Output to slot 2 and decrease volume of antimatter
          antimatter -= 125
          decrStackSize(1, 1)
          setInventorySlotContents(2, new ItemStack(AtomicContent.itemAntimatter))
        }
      }
    }
  }

  override def activate(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    player.openGui(ResonantInduction, 0, world, xi, yi, zi)
    return true
  }

  private def CalculateParticleDensity
  {
    val itemToAccelerate: ItemStack = this.getStackInSlot(0)
    if (itemToAccelerate != null)
    {
      antiMatterDensityMultiplyer = Settings.ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER

      val potentialBlock: Block = Block.getBlockFromItem(itemToAccelerate.getItem)
      if (potentialBlock != null)
      {
        antiMatterDensityMultiplyer = BlockUtility.getBlockHardness(potentialBlock).asInstanceOf[Int] * Settings.ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER
        if (antiMatterDensityMultiplyer <= 0)
        {
          antiMatterDensityMultiplyer = Settings.ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER
        }
        if (antiMatterDensityMultiplyer > 1000)
        {
          antiMatterDensityMultiplyer = 1000 * Settings.ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER
        }
      }
    }
  }

  /////////////////////////////////////////
  ///         Packet Handling           ///
  ////////////////////////////////////////

  override def read(buf: ByteBuf, id: Int, player: EntityPlayer, packet: PacketType): Boolean =
  {
    //Client only packets
    if (world.isRemote)
    {
      if (id == DESC_PACKET_ID)
      {
        this.velocity = buf.readFloat()
        this.totalEnergyConsumed = buf.readDouble()
        this.antimatter = buf.readInt()
        this.energy.setEnergy(buf.readDouble())
        return true
      }
    }

    return true
  }

  override def getDescPacket: PacketTile =
  {
    return new PacketTile(xi, yi, zi, Array[Any](DESC_PACKET_ID, velocity, totalEnergyConsumed, antimatter, energy.getEnergy))
  }

  /////////////////////////////////////////
  ///         Save handling             ///
  ////////////////////////////////////////

  override def readFromNBT(par1NBTTagCompound: NBTTagCompound)
  {
    super.readFromNBT(par1NBTTagCompound)
    totalEnergyConsumed = par1NBTTagCompound.getDouble("energyUsed")
    antimatter = par1NBTTagCompound.getInteger("antimatter")
  }

  override def writeToNBT(par1NBTTagCompound: NBTTagCompound)
  {
    super.writeToNBT(par1NBTTagCompound)
    par1NBTTagCompound.setDouble("energyUsed", totalEnergyConsumed)
    par1NBTTagCompound.setInteger("antimatter", antimatter)
  }

  /////////////////////////////////////////
  ///         Inventory Overrides      ///
  ////////////////////////////////////////

  override def canInsertItem(slotID: Int, itemStack: ItemStack, j: Int): Boolean =
  {
    return isItemValidForSlot(slotID, itemStack) && slotID != 2 && slotID != 3
  }

  override def canExtractItem(slotID: Int, itemstack: ItemStack, j: Int): Boolean =
  {
    return slotID == 2 || slotID == 3
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
  {
    i match
    {
      case 0 =>
        return true
      case 1 =>
        return AtomicContent.isItemStackEmptyCell(itemStack)
      case 2 =>
        return itemStack.getItem.isInstanceOf[ItemAntimatter]
      case 3 =>
        return itemStack.getItem != null && itemStack.getItem.getUnlocalizedName.contains("DarkMatter")
    }
    return false
  }

  /////////////////////////////////////////
  ///      Field Getters & Setters      ///
  ////////////////////////////////////////

  override def isRunning: Boolean =
  {
    return true
  }

  override def getDirection: ForgeDirection =
  {
    return ForgeDirection.getOrientation(getBlockMetadata)
  }

  override def setDirection(direction: ForgeDirection)
  {
    world.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal, 3)
  }

  /** get velocity for the particle and @return it as a float */
  def getParticleVel(): Float =
  {
    if (entityParticle != null)
      return entityParticle.getParticleVelocity.asInstanceOf[Float]
    else
      return 0
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == getDirection.getOpposite.ordinal())
    {
      return AtomicContent.blockElectromagnet.getIcon(side, meta)
    }
    return getIcon
  }
}