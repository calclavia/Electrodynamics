package com.calclavia.edx.quantum.machine.accelerator

class TileAccelerator extends ResonantTile(Material.iron) with TInventory with IElectromagnet with IRotatable with TPacketReceiver with TPacketSender with TEnergyProvider
{
  final val DESC_PACKET_ID = 2
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
  /**
   * Multiplier that is used to give extra anti-matter based on density (hardness) of a given ore.
   */
  private var antiMatterDensityMultiplyer: Int = Settings.ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER

  //Constructor
  //TODO: Dummy
  energy = new EnergyStorage

  override def getSizeInventory: Int = 4

  override def update
  {
    super.update
    if (!worldObj.isRemote)
    {
      clientEnergy = energy.value
      velocity = getParticleVel()
      outputAntimatter()

      if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
      {
        if (energy >= Settings.ACCELERATOR_ENERGY_COST_PER_TICK)
        {
          if (entityParticle == null)
          {
	          //Create new particle if we have materials to spawn it withPriority
            if (getStackInSlot(0) != null && lastSpawnTick >= 40)
            {
              val spawn_vec: Vector3 = position
              spawn_vec.add(getDirection.getOpposite)
              spawn_vec.add(0.5f)
              if (EntityParticle.canSpawnParticle(worldObj, spawn_vec))
              {
                totalEnergyConsumed = 0
                entityParticle = new EntityParticle(worldObj, spawn_vec, position, getDirection.getOpposite)
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
                  incrStackSize(3, new ItemStack(QuantumContent.itemDarkMatter))
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
          energy -= Settings.ACCELERATOR_ENERGY_COST_PER_TICK
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
    if (QuantumContent.isItemStackEmptyCell(getStackInSlot(1)) && getStackInSlot(1).stackSize > 0)
    {
      // Each cell can only hold 125mg of antimatter TODO maybe a config for this?
      if (antimatter >= 125)
      {
        if (getStackInSlot(2) != null)
        {
          // If the output slot is not empty we must increase stack size
          if (getStackInSlot(2).getItem == QuantumContent.itemAntimatter)
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
          setInventorySlotContents(2, new ItemStack(QuantumContent.itemAntimatter))
        }
      }
    }
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

  override def getDescPacket: PacketTile =
  {
    return new PacketTile(x, y, z, Array[Any](DESC_PACKET_ID, velocity, totalEnergyConsumed, antimatter, energy.value))
  }

  /////////////////////////////////////////
  ///         Packet Handling           ///
  ////////////////////////////////////////

  /** get velocity for the particle and @return it as a float */
  def getParticleVel(): Float =
  {
    if (entityParticle != null)
      return entityParticle.getParticleVelocity.asInstanceOf[Float]
    else
      return 0
  }

  override def activate(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    player.openGui(Electrodynamics, 0, world, x, y, z)
    return true
  }

  /////////////////////////////////////////
  ///         Save handling             ///
  ////////////////////////////////////////

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    //Client only packets
    if (world.isRemote)
    {
      if (id == DESC_PACKET_ID)
      {
        this.velocity = buf.readFloat()
        this.totalEnergyConsumed = buf.readDouble()
        this.antimatter = buf.readInt()
        this.energy.value = buf.readDouble()
      }
    }

  }

  override def readFromNBT(par1NBTTagCompound: NBTTagCompound)
  {
    super.readFromNBT(par1NBTTagCompound)
    totalEnergyConsumed = par1NBTTagCompound.getDouble("energyUsed")
    antimatter = par1NBTTagCompound.getInteger("antimatter")
  }

  /////////////////////////////////////////
  ///         Inventory Overrides      ///
  ////////////////////////////////////////

  override def writeToNBT(par1NBTTagCompound: NBTTagCompound)
  {
    super.writeToNBT(par1NBTTagCompound)
    par1NBTTagCompound.setDouble("energyUsed", totalEnergyConsumed)
    par1NBTTagCompound.setInteger("antimatter", antimatter)
  }

  override def canInsertItem(slotID: Int, itemStack: ItemStack, j: Int): Boolean =
  {
    return isItemValidForSlot(slotID, itemStack) && slotID != 2 && slotID != 3
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
  {
    i match
    {
      case 0 =>
        return true
      case 1 =>
        return QuantumContent.isItemStackEmptyCell(itemStack)
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

  override def canExtractItem(slotID: Int, itemstack: ItemStack, j: Int): Boolean =
  {
    return slotID == 2 || slotID == 3
  }

  override def isRunning: Boolean =
  {
    return true
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == getDirection.getOpposite.ordinal())
    {
      return QuantumContent.blockElectromagnet.getIcon(side, meta)
    }
    return getIcon
  }

  override def getDirection: ForgeDirection =
  {
    return ForgeDirection.getOrientation(getBlockMetadata)
  }

  override def setDirection(direction: ForgeDirection)
  {
    world.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal, 3)
  }
}