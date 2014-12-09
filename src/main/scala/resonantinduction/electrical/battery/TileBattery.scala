package resonantinduction.electrical.battery

import java.util.{ArrayList, Arrays, List}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._
import resonant.content.prefab.java.TileAdvanced
import resonant.lib.content.prefab.{TElectric, TEnergyStorage}
import resonant.engine.network.discriminator.{PacketTile, PacketType}
import resonant.engine.network.handle.IPacketReceiver
import resonant.engine.network.netty.AbstractPacket
import resonant.lib.grid.energy.EnergyStorage
import resonant.lib.render.RenderUtility
import resonant.lib.transform.vector.Vector3
import resonantinduction.core.Reference

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
    return Math.round(Math.pow(500000000, (tier / (maxTier + 0.7f)) + 1) / 500000000) * 500000000
  }

  /** Tiers: 0, 1, 2 */
  final val maxTier = 2
  /** The transfer rate **/
  final val defaultPower = getEnergyForTier(0)

  @SideOnly(Side.CLIENT)
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "battery/battery.tcn"))
}

class TileBattery extends TileAdvanced(Material.iron) with TElectric with IPacketReceiver with TEnergyStorage
{
  private var markClientUpdate: Boolean = false
  private var markDistributionUpdate: Boolean = false
  var renderEnergyAmount: Double = 0

  energy = new EnergyStorage
  textureName = "material_metal_side"
  ioMap = 0
  saveIOMap = true
  normalRender = false
  isOpaqueCube = false
  itemBlock = classOf[ItemBlockBattery]

  var doCharge = false

  override def update()
  {
    super.update()

    if (!world.isRemote)
    {
      //TODO: Test, remove this
      if (doCharge)
      {
        dcNode.buffer(100)
      }

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

  override def activate(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    super.activate(player, side, hit)

    if (!world.isRemote)
    {
      if (player.isSneaking)
      {
        doCharge = !doCharge
      }

      println(dcNode)
    }

    return true
  }

  override def getDescPacket: AbstractPacket = new PacketTile(this) <<< renderEnergyAmount <<< ioMap

  override def read(buf: ByteBuf, player: EntityPlayer, packet: PacketType)
  {
    energy.setEnergy(buf.readLong)
    ioMap == buf.readShort
  }

  override def setIO(dir: ForgeDirection, packet: Int)
  {
    super.setIO(dir, packet)

    //TODO: Not set during init
    dcNode.connectionMask = ForgeDirection.VALID_DIRECTIONS.filter(getIO(_) > 0).map(d => 1 << d.ordinal()).foldLeft(0)(_ | _)
    //TODO: Connection logic having an issue
    dcNode.positiveTerminals.clear()
    dcNode.positiveTerminals.addAll(getOutputDirections())
    notifyChange()
    dcNode.reconstruct()

    markUpdate()
  }

  override def onPlaced(entityliving: EntityLivingBase, itemStack: ItemStack)
  {
    if (!world.isRemote && itemStack.getItem.isInstanceOf[ItemBlockBattery])
    {
      energy.setCapacity(TileBattery.getEnergyForTier(ItemBlockBattery.getTier(itemStack)))
      energy.setEnergy(itemStack.getItem.asInstanceOf[ItemBlockBattery].getEnergy(itemStack))
      world.setBlockMetadataWithNotify(xi, yi, zi, ItemBlockBattery.getTier(itemStack), 3)
    }
  }

  override def getDrops(metadata: Int, fortune: Int): ArrayList[ItemStack] =
  {
    val ret: ArrayList[ItemStack] = new ArrayList[ItemStack]
    val itemStack: ItemStack = new ItemStack(getBlockType, 1)
    val itemBlock: ItemBlockBattery = itemStack.getItem.asInstanceOf[ItemBlockBattery]
    ItemBlockBattery.setTier(itemStack, world.getBlockMetadata(xi, yi, zi).asInstanceOf[Byte])
    itemBlock.setEnergy(itemStack, energy.getEnergy)
    ret.add(itemStack)
    return ret
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    glPushMatrix()
    val energyLevel = ((itemStack.getItem.asInstanceOf[ItemBlockBattery].getEnergy(itemStack) / itemStack.getItem.asInstanceOf[ItemBlockBattery].getEnergyCapacity(itemStack)) * 8).toInt
    RenderUtility.bind(Reference.domain, Reference.modelPath + "battery/battery.png")
    val disabledParts: List[String] = new ArrayList[String]
    disabledParts.addAll(Arrays.asList(Array[String]("connector", "connectorIn", "connectorOut"): _*))
    disabledParts.addAll(Arrays.asList(Array[String]("coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8"): _*))
    disabledParts.addAll(Arrays.asList(Array[String]("coil1lit", "coil2lit", "coil3lit", "coil4lit", "coil5lit", "coil6lit", "coil7lit", "coil8lit"): _*))
    disabledParts.addAll(Arrays.asList(Array[String]("frame1con", "frame2con", "frame3con", "frame4con"): _*))
    TileBattery.model.renderAllExcept(disabledParts.toArray(new Array[String](0)): _*)

    for (i <- 1 until 8)
    {
      if (i != 1 || !disabledParts.contains("coil1"))
      {
        if ((8 - i) <= energyLevel) TileBattery.model.renderOnly("coil" + i + "lit")
        else TileBattery.model.renderOnly("coil" + i)
      }
    }
    glPopMatrix()
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    val partToDisable: Array[Array[String]] = Array[Array[String]](Array[String]("bottom"), Array[String]("top"), Array[String]("frame1", "frame2"), Array[String]("frame3", "frame4"), Array[String]("frame4", "frame1"), Array[String]("frame2", "frame3"))
    val connectionPartToEnable: Array[Array[String]] = Array[Array[String]](null, null, Array[String]("frame1con", "frame2con"), Array[String]("frame3con", "frame4con"), Array[String]("frame4con", "frame1con"), Array[String]("frame2con", "frame3con"))
    glPushMatrix()
    glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
    val energyLevel: Int = Math.round((energy.getEnergy / TileBattery.getEnergyForTier(getBlockMetadata).asInstanceOf[Double]) * 8).asInstanceOf[Int]
    RenderUtility.bind(Reference.domain, Reference.modelPath + "battery/battery.png")

    val disabledParts = new ArrayList[String]
    val enabledParts = new ArrayList[String]

    for (check <- ForgeDirection.VALID_DIRECTIONS)
    {
      if (center.add(check).getTileEntity.isInstanceOf[TileBattery])
      {
        disabledParts.addAll(Arrays.asList(partToDisable(check.ordinal): _*))
        if (check eq ForgeDirection.UP)
        {
          enabledParts.addAll(Arrays.asList(partToDisable(check.ordinal): _*))
          enabledParts.add("coil1")
        }
        else if (check eq ForgeDirection.DOWN)
        {
          val connectionParts = new ArrayList[String]
          for (sideCheck <- ForgeDirection.VALID_DIRECTIONS) if (sideCheck.offsetY == 0) connectionParts.addAll(Arrays.asList(connectionPartToEnable(sideCheck.ordinal): _*))
          for (sideCheck <- ForgeDirection.VALID_DIRECTIONS)
          {
            if (sideCheck.offsetY == 0)
            {
              if (center.add(sideCheck).getTileEntity.isInstanceOf[TileBattery])
              {
                connectionParts.removeAll(Arrays.asList(connectionPartToEnable(sideCheck.ordinal)))
              }
            }
          }
          enabledParts.addAll(connectionParts)
        }
      }

      if (check.offsetY == 0)
      {
        GL11.glPushMatrix()
        RenderUtility.rotateBlockBasedOnDirection(check)

        if (check == ForgeDirection.NORTH)
        {
          glRotatef(0, 0, 1, 0)
        }
        if (check == ForgeDirection.SOUTH)
        {
          glRotatef(0, 0, 1, 0)
        }
        else if (check == ForgeDirection.WEST)
        {
          glRotatef(-180, 0, 1, 0)
        }
        else if (check == ForgeDirection.EAST)
        {
          glRotatef(180, 0, 1, 0)
        }
        GL11.glRotatef(-90, 0, 1, 0)
        val io: Int = getIO(check)
        if (io == 1)
        {
          TileBattery.model.renderOnly("connectorIn")
        }
        else if (io == 2)
        {
          TileBattery.model.renderOnly("connectorOut")
        }
        GL11.glPopMatrix()
      }
    }

    enabledParts.removeAll(disabledParts)

    for (i <- 1 to 8)
    {
      if (i != 1 || enabledParts.contains("coil1"))
      {
        if ((8 - i) < energyLevel) TileBattery.model.renderOnly("coil" + i + "lit")
        else TileBattery.model.renderOnly("coil" + i)
      }
    }

    disabledParts.addAll(Arrays.asList(Array[String]("connector", "connectorIn", "connectorOut"): _*))
    disabledParts.addAll(Arrays.asList(Array[String]("coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8"): _*))
    disabledParts.addAll(Arrays.asList(Array[String]("coil1lit", "coil2lit", "coil3lit", "coil4lit", "coil5lit", "coil6lit", "coil7lit", "coil8lit"): _*))
    disabledParts.addAll(Arrays.asList(Array[String]("frame1con", "frame2con", "frame3con", "frame4con"): _*))
    enabledParts.removeAll(Arrays.asList(Array[String]("coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8")))
    TileBattery.model.renderAllExcept(disabledParts.toArray(new Array[String](0)): _*)
    TileBattery.model.renderOnly(enabledParts.toArray(new Array[String](0)): _*)
    GL11.glPopMatrix()
  }

  override def toString: String =
  {
    return "[TileBattery]" + x + "x " + y + "y " + z + "z "
  }
}