package resonantinduction.atomic.machine.thermometer

import java.util.ArrayList

import cpw.mods.fml.common.Optional
import cpw.mods.fml.relauncher.{Side, SideOnly}
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.SimpleComponent
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import resonant.content.prefab.itemblock.ItemBlockSaved
import resonant.content.prefab.java.TileAdvanced
import resonant.engine.grid.thermal.ThermalGrid
import resonant.engine.network.Synced
import resonant.engine.network.discriminator.PacketAnnotation
import resonant.lib.transform.vector.{Vector3, VectorWorld}
import resonant.lib.utility.inventory.InventoryUtility
import resonantinduction.core.Reference

/**
 * Thermometer TileEntity
 */
object TileThermometer
{
  final val MAX_THRESHOLD: Int = 5000
  private var iconSide: IIcon = null
}

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
class TileThermometer extends TileAdvanced(Material.piston) with SimpleComponent
{
  @Synced var detectedTemperature: Float = 295
  @Synced var previousDetectedTemperature: Float = 295
  @Synced var trackCoordinate: Vector3 = null
  @Synced private var threshold: Int = 1000
  @Synced private var isProvidingPower: Boolean = false

  //Constructor
  providePower = true
  normalRender = false
  renderStaticBlock = true
  itemBlock = classOf[ItemBlockThermometer]
  isOpaqueCube = true

  override def getIcon(side: Int, meta: Int): IIcon =
  {
    return if (side == 1 || side == 0) super.getIcon(side, meta) else TileThermometer.iconSide
  }

  @SideOnly(Side.CLIENT) override def registerIcons(iconRegister: IIconRegister)
  {
    super.registerIcons(iconRegister)
    TileThermometer.iconSide = iconRegister.registerIcon(Reference.prefix + "machine")
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.isSneaking)
    {
      setThreshold(getThershold + 100)
    }
    else
    {
      setThreshold(getThershold - 100)
    }
    return true
  }

  override def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (player.isSneaking)
    {
      setThreshold(getThershold - 10)
    }
    else
    {
      setThreshold(getThershold + 10)
    }
    return true
  }

  def setThreshold(newThreshold: Int)
  {
    threshold = newThreshold % TileThermometer.MAX_THRESHOLD
    if (threshold <= 0)
    {
      threshold = TileThermometer.MAX_THRESHOLD
    }
    markUpdate
  }

  override def getStrongRedstonePower(access: IBlockAccess, side: Int): Int =
  {
    return if (isProvidingPower) 15 else 0
  }

  override def getDrops(metadata: Int, fortune: Int): ArrayList[ItemStack] =
  {
    return new ArrayList[ItemStack]
  }

  override def onRemove(block: Block, par6: Int)
  {
    val stack: ItemStack = ItemBlockSaved.getItemStackWithNBT(getBlockType, world, xi, yi, zi)
    InventoryUtility.dropItemStack(world, center, stack)
  }

  override def update
  {
    super.update
    if (!worldObj.isRemote)
    {
      if (ticks % 10 == 0)
      {
        if (trackCoordinate != null)
        {
          detectedTemperature = ThermalGrid.getTemperature(new VectorWorld(world, trackCoordinate))
        }
        else
        {
          detectedTemperature = ThermalGrid.getTemperature(toVectorWorld)
        }
        if (detectedTemperature != previousDetectedTemperature || isProvidingPower != this.isOverThreshold)
        {
          previousDetectedTemperature = detectedTemperature
          isProvidingPower = isOverThreshold
          notifyChange
          //sendPacket(getDescPacket)
        }
      }
    }
  }

  override def getDescPacket: PacketAnnotation =
  {
    return new PacketAnnotation(this)
  }

  def setTrack(track: Vector3)
  {
    trackCoordinate = track
  }

  /**
   * Reads a tile entity from NBT.
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    threshold = nbt.getInteger("threshold")
    if (nbt.hasKey("trackCoordinate"))
    {
      trackCoordinate = new Vector3(nbt.getCompoundTag("trackCoordinate"))
    }
    else
    {
      trackCoordinate = null
    }
  }

  /**
   * Writes a tile entity to NBT.
   */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("threshold", threshold)
    if (trackCoordinate != null)
    {
      nbt.setTag("trackCoordinate", this.trackCoordinate.writeNBT(new NBTTagCompound))
    }
  }

  @Callback
  @Optional.Method(modid = "OpenComputers")
  def getTemperature(context: Context, args: Arguments): Array[Any] =
  {
    return Array[Any](this.detectedTemperature)
  }

  @Callback
  @Optional.Method(modid = "OpenComputers")
  def getWarningTemperature(context: Context, args: Arguments): Array[Any] =
  {
    return Array[Any](this.getThershold)
  }

  def getThershold: Int =
  {
    return threshold
  }

  @Callback
  @Optional.Method(modid = "OpenComputers")
  def isAboveWarningTemperature(context: Context, args: Arguments): Array[Any] =
  {
    return Array[Any](this.isOverThreshold)
  }

  def isOverThreshold: Boolean =
  {
    return detectedTemperature >= getThershold
  }

  @Callback
  @Optional.Method(modid = "OpenComputers")
  def setWarningTemperature(context: Context, args: Arguments): Array[Any] =
  {
    if (args.count <= 0)
    {
      throw new IllegalArgumentException("Not enough Arguments. Must provide one argument")
    }
    if (args.count >= 2)
    {
      throw new IllegalArgumentException("Too many Arguments. Must provide one argument")
    }
    if (!args.isInteger(0))
    {
      throw new IllegalArgumentException("Invalid Argument. Must provide an Integer")
    }
    this synchronized
    {
      this.setThreshold(args.checkInteger(0))
    }
    return Array[Any](this.threshold == args.checkInteger(0))
  }

  def getComponentName: String =
  {
    return "Thermometer"
  }
}