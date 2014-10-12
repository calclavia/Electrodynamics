package resonantinduction.electrical.battery

import java.util.ArrayList

import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.content.prefab.java.TileElectric
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.IPacketReceiver
import resonant.lib.network.netty.AbstractPacket
import universalelectricity.simulator.dc.micro.DCNode

/** A modular battery box that allows shared connections with boxes next to it.
  *
  * @author Calclavia
  */
object TileBattery
{
  /**
   * @param tier - 0, 1, 2
   * @return
   */
  def getEnergyForTier(tier: Int): Long =
  {
    return Math.round(Math.pow(500000000, (tier / (MAX_TIER + 0.7f)) + 1) / (500000000)) * (500000000)
  }

  /** Tiers: 0, 1, 2 */
  final val MAX_TIER: Int = 2
  /** The transfer rate **/
  final val DEFAULT_WATTAGE: Long = getEnergyForTier(0)
}

class TileBattery extends TileElectric(Material.iron) with IPacketReceiver
{
  private var markClientUpdate: Boolean = false
  private var markDistributionUpdate: Boolean = false
  var renderEnergyAmount: Double = 0
  private var network: GridBattery = null

  //Constructor
  setTextureName("material_metal_side")
  ioMap = 0.toShort
  saveIOMap = true
  normalRender(false)
  isOpaqueCube(false)
  itemBlock(classOf[ItemBlockBattery])

  //TODO: Test, remove this
  private val node = new DCNode(this)
  {
    override def charge(terminal: ForgeDirection): Double = 0

    /*
    {
      if (getInputDirections().contains(terminal))
        return 0
      else if (getOutputDirections().contains(terminal))
        return 0

      return super.charge
    }
     */
  }

  override def update()
  {
    super.update()

    if (!world.isRemote)
    {
      //TODO: Test, remove this
      node.buffer(100)

      if (markDistributionUpdate && ticks % 5 == 0)
      {
        markDistributionUpdate = false
      }
      if (markClientUpdate && ticks % 5 == 0)
      {
        markClientUpdate = false
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
      }
    }
  }

  override def getDescPacket: AbstractPacket =
  {
    return new PacketTile(this, Array[Any](renderEnergyAmount, ioMap))
  }

  def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
  {
    this.energy.setEnergy(data.readLong)
    this.ioMap_$eq(data.readShort)
  }

  override def setIO(dir: ForgeDirection, `type`: Int)
  {
    super.setIO(dir, `type`)
    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
  }

  override def onPlaced(entityliving: EntityLivingBase, itemStack: ItemStack)
  {
    if (!world.isRemote && itemStack.getItem.isInstanceOf[ItemBlockBattery])
    {
      energy.setCapacity(TileBattery.getEnergyForTier(ItemBlockBattery.getTier(itemStack)))
      energy.setEnergy((itemStack.getItem.asInstanceOf[ItemBlockBattery]).getEnergy(itemStack))
      world.setBlockMetadataWithNotify(x, y, z, ItemBlockBattery.getTier(itemStack), 3)
    }
  }

  override def getDrops(metadata: Int, fortune: Int): ArrayList[ItemStack] =
  {
    val ret: ArrayList[ItemStack] = new ArrayList[ItemStack]
    val itemStack: ItemStack = new ItemStack(getBlockType, 1)
    val itemBlock: ItemBlockBattery = itemStack.getItem.asInstanceOf[ItemBlockBattery]
    ItemBlockBattery.setTier(itemStack, world.getBlockMetadata(x, y, z).asInstanceOf[Byte])
    itemBlock.setEnergy(itemStack, energy.getEnergy)
    ret.add(itemStack)
    return ret
  }

  override def toString: String =
  {
    return "[TileBattery]" + x + "x " + y + "y " + z + "z "
  }
}