package mffs.production

import com.resonant.core.graph.internal.electric.TTEBridge
import mffs.base.{BlockModuleAcceptor, PacketBlock}
import mffs.item.card.ItemCardFrequency
import mffs.util.FortronUtility
import mffs.{Content, Settings}

/**
 * A TileEntity that extract energy into Fortron.
 *
 * @author Calclavia
 */
object BlockCoercionDeriver
{
  val fuelProcessTime = 10 * 20
  val productionMultiplier = 4

  /**
   * Ration from UE to Fortron. Multiply J by this value to convert to Fortron.
   */
  val ueToFortronRatio = 0.005f
  val energyConversionPercentage = 1

  val slotFrequency = 0
  val slotBattery = 1
  val slotFuel = 2

  /**
   * The amount of power (watts) this machine uses.
   */
  val power = 5000000
}

class BlockCoercionDeriver extends BlockModuleAcceptor with TTEBridge
{
  var processTime: Int = 0
  var isInversed = false

  //Client
  var animationTween = 0f

  capacityBase = 30
  startModuleIndex = 3

  override def getSizeInventory = 6

  override def update()
  {
    super.update()

	  if (Game.instance.networkManager.isServer)
    {
      if (isActive)
      {
        if (isInversed && Settings.enableElectricity)
        {
			val withdrawnElectricity = addFortron(productionRate / 20, true) / BlockCoercionDeriver.ueToFortronRatio
			energy += withdrawnElectricity * BlockCoercionDeriver.energyConversionPercentage

          //          recharge(getStackInSlot(TileCoercionDeriver.slotBattery))
        }
        else
        {
			if (getFortron < getFortronCapacity)
          {
            //            discharge(getStackInSlot(TileCoercionDeriver.slotBattery))
            energy.max = getPower

			  if (energy >= getPower || (!Settings.enableElectricity && isItemValidForSlot(BlockCoercionDeriver.slotFuel, getStackInSlot(BlockCoercionDeriver.slotFuel))))
            {
              fortronTank.fill(FortronUtility.getFortron(productionRate), true)
              energy -= getPower

				if (processTime == 0 && isItemValidForSlot(BlockCoercionDeriver.slotFuel, getStackInSlot(BlockCoercionDeriver.slotFuel)))
              {
				  decrStackSize(BlockCoercionDeriver.slotFuel, 1)
				  processTime = BlockCoercionDeriver.fuelProcessTime * Math.max(this.getModuleCount(Content.moduleScale) / 20, 1)
              }

              if (processTime > 0)
              {
                processTime -= 1

                if (processTime < 1)
                  processTime = 0
              }
              else
              {
                processTime = 0
              }
            }
          }
        }
      }
    }
    else
    {
      /**
       * Handle animation
       */
      if (isActive)
      {
        animation += 1

        if (animationTween < 1)
          animationTween += 0.01f
      }
      else
      {
        if (animationTween > 0)
          animationTween -= 0.01f
      }
    }
  }

  /**
   * @return The Fortron production rate per tick!
   */
  def productionRate: Int =
  {
    if (this.isActive)
    {
		var production = (getPower.asInstanceOf[Float] / 20f * BlockCoercionDeriver.ueToFortronRatio * Settings.fortronProductionMultiplier).asInstanceOf[Int]

      if (processTime > 0)
      {
		  production *= BlockCoercionDeriver.productionMultiplier
      }

      return production
    }
    return 0
  }

	def getPower: Double = BlockCoercionDeriver.power + (BlockCoercionDeriver.power * (getModuleCount(Content.moduleSpeed) / 8d))

	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean =
  {
	  if (Item != null)
    {
      if (slotID >= startModuleIndex)
      {
		  return Item.getItem.isInstanceOf[IModule]
      }
      slotID match
      {
		  case BlockCoercionDeriver.slotFrequency =>
			return Item.getItem.isInstanceOf[ItemCardFrequency]
		  case BlockCoercionDeriver.slotBattery =>
			return Compatibility.isHandler(Item.getItem, null)
		  case BlockCoercionDeriver.slotFuel =>
			return Item.isItemEqual(new Item(Items.dye, 1, 4)) || Item.isItemEqual(new Item(Items.quartz))
      }
    }
    return false
  }

  def canConsume: Boolean =
  {
    if (this.isActive && !this.isInversed)
    {
      return FortronUtility.getAmount(this.fortronTank) < this.fortronTank.getCapacity
    }
    return false
  }

	override def write(buf: Packet, id: Int)
  {
    super.write(buf, id)

	  if (id == PacketBlock.description.id)
    {
      buf <<< isInversed
      buf <<< processTime
    }
  }

	override def read(buf: Packet, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    if (world.isRemote)
    {
		if (id == PacketBlock.description.id)
      {
        isInversed = buf.readBoolean()
        processTime = buf.readInt()
      }
    }
    else
    {
		if (id == PacketBlock.toggleMode.id)
      {
        isInversed = !isInversed
      }
    }
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    processTime = nbt.getInteger("processTime")
    isInversed = nbt.getBoolean("isInversed")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("processTime", processTime)
    nbt.setBoolean("isInversed", isInversed)
  }

  @SideOnly(Side.CLIENT)
  override def renderStatic(renderer: RenderBlocks, pos: Vector3d, pass: Int): Boolean =
  {
    return false
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3d, frame: Float, pass: Int)
  {
    RenderCoercionDeriver.render(this, pos.x, pos.y, pos.z, frame, isActive, false)
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(Item: Item)
  {
    RenderCoercionDeriver.render(this, -0.5, -0.5, -0.5, 0, true, true)
  }
}