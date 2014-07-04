package mffs

import com.mojang.authlib.GameProfile
import mffs.field.TileElectromagnetProjector
import mffs.field.mode.ItemModeCustom
import mffs.fortron.TransferMode
import mffs.fortron.TransferMode.TransferMode
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import resonant.api.mffs.IProjector
import resonant.api.mffs.fortron.{FrequencyGridRegistry, IFortronFrequency}
import resonant.api.mffs.modules.IModuleAcceptor
import resonant.api.mffs.security.IInterdictionMatrix
import resonant.engine.grid.frequency.FrequencyGrid
import resonant.lib.access.Permission
import universalelectricity.core.transform.rotation.Rotation
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * A class containing some general helpful functions.
 *
 * @author Calclavia
 */
object MFFSHelper
{
  def transferFortron(transferer: IFortronFrequency, frequencyTiles: Set[IFortronFrequency], transferMode: TransferMode, limit: Int)
  {
    if (transferer != null && frequencyTiles.size > 1 && Settings.allowFortronTeleport)
    {
      var totalFortron: Int = 0
      var totalCapacity: Int = 0

      for (machine <- frequencyTiles)
      {
        if (machine != null)
        {
          totalFortron += machine.getFortronEnergy
          totalCapacity += machine.getFortronCapacity
        }
      }
      if (totalFortron > 0 && totalCapacity > 0)
      {
        transferMode match
        {
          case TransferMode.EQUALIZE =>
          {
            for (machine <- frequencyTiles)
            {
              if (machine != null)
              {
                val capacityPercentage: Double = machine.getFortronCapacity.asInstanceOf[Double] / totalCapacity.asInstanceOf[Double]
                val amountToSet: Int = (totalFortron * capacityPercentage).asInstanceOf[Int]
                doTransferFortron(transferer, machine, amountToSet - machine.getFortronEnergy, limit)
              }
            }
          }
          case TransferMode.DISTRIBUTE =>
          {
            val amountToSet: Int = totalFortron / frequencyTiles.size
            for (machine <- frequencyTiles)
            {
              if (machine != null)
              {
                doTransferFortron(transferer, machine, amountToSet - machine.getFortronEnergy, limit)
              }
            }
          }
          case TransferMode.DRAIN =>
          {
            frequencyTiles.remove(transferer)

            for (machine <- frequencyTiles)
            {
              if (machine != null)
              {
                val capacityPercentage: Double = machine.getFortronCapacity.asInstanceOf[Double] / totalCapacity.asInstanceOf[Double]
                val amountToSet: Int = (totalFortron * capacityPercentage).asInstanceOf[Int]

                if (amountToSet - machine.getFortronEnergy > 0)
                {
                  doTransferFortron(transferer, machine, amountToSet - machine.getFortronEnergy, limit)
                }
              }
            }
          }
          case TransferMode.FILL =>
          {
            if (transferer.getFortronEnergy < transferer.getFortronCapacity)
            {
              frequencyTiles.remove(transferer)
              val requiredFortron: Int = transferer.getFortronCapacity - transferer.getFortronEnergy

              for (machine <- frequencyTiles)
              {
                if (machine != null)
                {
                  val amountToConsume: Int = Math.min(requiredFortron, machine.getFortronEnergy)
                  val amountToSet: Int = -machine.getFortronEnergy - amountToConsume
                  if (amountToConsume > 0)
                  {
                    doTransferFortron(transferer, machine, amountToSet - machine.getFortronEnergy, limit)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Tries to transfer Fortron to a specific machine from this capacitor. Renders an animation on
   * the client side.
   *
   * @param receiver : The machine to be transfered to.
   * @param joules   : The amount of energy to be transfered.
   */
  def doTransferFortron(transferer: IFortronFrequency, receiver: IFortronFrequency, joules: Int, limit: Int)
  {
    if (transferer != null && receiver != null)
    {
      val tileEntity = transferer.asInstanceOf[TileEntity]
      val world: World = tileEntity.getWorldObj()
      var isCamo = false

      if (transferer.isInstanceOf[IModuleAcceptor])
      {
        isCamo = (transferer.asInstanceOf[IModuleAcceptor]).getModuleCount(ModularForceFieldSystem.Items.moduleCamouflage) > 0
      }

      if (joules > 0)
      {
        val transferEnergy = Math.min(joules, limit)
        var toBeInjected: Int = receiver.provideFortron(transferer.requestFortron(transferEnergy, false), false)
        toBeInjected = transferer.requestFortron(receiver.provideFortron(toBeInjected, true), true)
        if (world.isRemote && toBeInjected > 0 && !isCamo)
        {
          ModularForceFieldSystem.proxy.renderBeam(world, new Vector3(tileEntity) + 0.5, new Vector3(receiver.asInstanceOf[TileEntity]) + 0.5, 0.6f, 0.6f, 1, 20)
        }
      }
      else
      {
        val transferEnergy = Math.min(Math.abs(joules), limit)
        var toBeEjected: Int = transferer.provideFortron(receiver.requestFortron(transferEnergy, false), false)
        toBeEjected = receiver.requestFortron(transferer.provideFortron(toBeEjected, true), true)
        if (world.isRemote && toBeEjected > 0 && !isCamo)
        {
          ModularForceFieldSystem.proxy.renderBeam(world, new Vector3(receiver.asInstanceOf[TileEntity]) + 0.5, new Vector3(tileEntity) + 0.5, 0.6f, 0.6f, 1, 20)
        }
      }
    }
  }



  /**
   * Gets the first itemStack that is an ItemBlock in this TileEntity or in nearby chests.
   */
  def getFirstItemBlock(tileEntity: TileEntity, itemStack: ItemStack): ItemStack =
  {
    return getFirstItemBlock(tileEntity, itemStack, true)
  }

  def getFirstItemBlock(tileEntity: TileEntity, itemStack: ItemStack, recur: Boolean): ItemStack =
  {
    if (tileEntity.isInstanceOf[IProjector])
    {
      val projector = tileEntity.asInstanceOf[IProjector]

      projector.getModuleSlots().find(getFirstItemBlock(_, projector, itemStack) != null) match
      {
        case Some(entry) => return getFirstItemBlock(entry, projector, itemStack)
        case _ =>
      }
    }
    else if (tileEntity.isInstanceOf[IInventory])
    {
      val inventory = tileEntity.asInstanceOf[IInventory]

      (0 until inventory.getSizeInventory()).view map (getFirstItemBlock(_, inventory, itemStack)) headOption match
      {
        case Some(entry) => return entry
        case _ =>
      }
    }

    if (recur)
    {
      ForgeDirection.VALID_DIRECTIONS.foreach(
        direction =>
        {
          val vector = new Vector3(tileEntity) + direction
          val checkTile = vector.getTileEntity(tileEntity.getWorldObj())

          if (checkTile != null)
          {
            val checkStack: ItemStack = getFirstItemBlock(checkTile, itemStack, false)

            if (checkStack != null)
            {
              return checkStack
            }
          }
        })
    }
    return null
  }

  def getFirstItemBlock(i: Int, inventory: IInventory, itemStack: ItemStack): ItemStack =
  {
    val checkStack: ItemStack = inventory.getStackInSlot(i)
    if (checkStack != null && checkStack.getItem.isInstanceOf[ItemBlock])
    {
      if (itemStack == null || checkStack.isItemEqual(itemStack))
      {
        return checkStack
      }
    }
    return null
  }

  def getFilterBlock(itemStack: ItemStack): Block =
  {
    if (itemStack != null)
    {
      if (itemStack.getItem.isInstanceOf[ItemBlock])
      {
        return itemStack.getItem().asInstanceOf[ItemBlock].field_150939_a
      }
    }
    return null
  }

  def getCamoBlock(projector: IProjector, position: Vector3): ItemStack =
  {
    val tile = projector.asInstanceOf[TileEntity]

    if (projector != null)
    {
      if (!tile.getWorldObj().isRemote)
      {
        if (projector != null)
        {
          if (projector.getModuleCount(ModularForceFieldSystem.Items.moduleCamouflage) > 0)
          {
            if (projector.getMode.isInstanceOf[ItemModeCustom])
            {
              val fieldMap = (projector.getMode.asInstanceOf[ItemModeCustom]).getFieldBlockMap(projector, projector.getModeStack)

              if (fieldMap != null)
              {
                val fieldCenter: Vector3 = new Vector3(projector.asInstanceOf[TileEntity]) + projector.getTranslation()
                var relativePosition: Vector3 = position.clone.subtract(fieldCenter)
                relativePosition = relativePosition.apply(new Rotation(-projector.getRotationYaw, -projector.getRotationPitch, 0))

                val blockInfo = fieldMap(relativePosition.round)

                if (blockInfo != null && blockInfo._1.isAir(tile.getWorldObj(), position.xi, position.yi, position.zi))
                {
                  return new ItemStack(blockInfo._1, 1, blockInfo._2)
                }
              }
            }

            for (i <- projector.getModuleSlots)
            {
              val checkStack: ItemStack = projector.getStackInSlot(i)
              val block: Block = getFilterBlock(checkStack)
              if (block != null)
              {
                return checkStack
              }
            }
          }
        }
      }
    }
    return null
  }

  /**
   * Gets the set of projectors that have an effect in this position.
   */
  def getRelevantProjectors(world: World, position: Vector3): mutable.Set[TileElectromagnetProjector] =
  {
    return FrequencyGridRegistry.instance.asInstanceOf[FrequencyGrid].getNodes(classOf[TileElectromagnetProjector]) filter (_.isInField(position))
  }

  def hasPermission(world: World, position: Vector3, permission: Permission, player: EntityPlayer): Boolean =
  {
    return hasPermission(world, position, permission, player.getGameProfile())
  }

  def hasPermission(world: World, position: Vector3, permission: Permission, profile: GameProfile): Boolean =
  {
    return getRelevantProjectors(world, position).forall(_.isAccessGranted(profile, permission))
  }

  def hasPermission(world: World, position: Vector3, action: PlayerInteractEvent.Action, player: EntityPlayer): Boolean =
  {
    return hasPermission(world, position, action, player.getGameProfile)
  }

  def hasPermission(world: World, position: Vector3, action: PlayerInteractEvent.Action, profile: GameProfile): Boolean =
  {
    return getRelevantProjectors(world, position).forall(_.isAccessGranted(world, position, profile, action))
  }
}