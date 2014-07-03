package mffs

import java.util.Set

import mffs.field.mode.ItemModeCustom
import mffs.fortron.TransferMode
import mffs.fortron.TransferMode.TransferMode
import mffs.security.access.MFFSPermissions
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
import universalelectricity.core.transform.rotation.Rotation
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

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
   * Gets the nearest active Interdiction Matrix.
   */
  def getNearestInterdictionMatrix(world: World, position: Vector3): IInterdictionMatrix =
  {
    for (frequencyTile <- FrequencyGridRegistry.instance().getNodes())
    {
      if ((frequencyTile.asInstanceOf[TileEntity]).getWorldObj() == world && frequencyTile.isInstanceOf[IInterdictionMatrix])
      {
        val interdictionMatrix: IInterdictionMatrix = frequencyTile.asInstanceOf[IInterdictionMatrix]

        if (interdictionMatrix.isActive)
        {
          if (position.distance(new Vector3(interdictionMatrix.asInstanceOf[TileEntity])) <= interdictionMatrix.getActionRange)
          {
            return interdictionMatrix
          }
        }
      }
    }
    return null
  }

  /**
   * Returns true of the interdictionMatrix has a specific set of permissions.
   *
   * @param interdictionMatrix
   * @param username
   * @param permissions
   * @return
   */
  def isPermittedByInterdictionMatrix(interdictionMatrix: IInterdictionMatrix, username: String, permissions: Permission*): Boolean =
  {
    if (interdictionMatrix != null)
    {
      if (interdictionMatrix.isActive)
      {
        if (interdictionMatrix.getBiometricIdentifier != null)
        {
          for (permission <- permissions)
          {
            if (!interdictionMatrix.getBiometricIdentifier.isAccessGranted(username, permission))
            {
              if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.Items.moduleInvert) > 0)
              {
                return true
              }
              else
              {
                return false
              }
            }
          }
        }
      }
    }
    if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.Items.moduleInvert) > 0)
    {
      return false
    }
    else
    {
      return true
    }
  }

  /**
   * Gets the first itemStack that is an ItemBlock in this TileEntity or in nearby chests.
   *
   * @param itemStack
   * @return
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
      val inventory: IInventory = tileEntity.asInstanceOf[IInventory]

      var i = 0

      while (i < inventory.getSizeInventory())
      {
        val checkStack: ItemStack = getFirstItemBlock(i, inventory, itemStack)

        if (checkStack != null)
        {
          return checkStack
        }

        i += 1
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

  def hasPermission(world: World, position: Vector3, permission: MFFSPermissions, player: EntityPlayer): Boolean =
  {
    return hasPermission(world, position, permission, player.getGameProfile().getName())
  }

  def hasPermission(world: World, position: Vector3, permission: MFFSPermissions, username: String): Boolean =
  {
    val interdictionMatrix: IInterdictionMatrix = getNearestInterdictionMatrix(world, position)

    if (interdictionMatrix != null)
    {
      return isPermittedByInterdictionMatrix(interdictionMatrix, username, permission)
    }

    return true
  }

  def hasPermission(world: World, position: Vector3, action: PlayerInteractEvent.Action, player: EntityPlayer): Boolean =
  {
    val interdictionMatrix: IInterdictionMatrix = getNearestInterdictionMatrix(world, position)
    if (interdictionMatrix != null)
    {
      return MFFSHelper.hasPermission(world, position, interdictionMatrix, action, player)
    }
    return true
  }

  def hasPermission(world: World, position: Vector3, interdictionMatrix: IInterdictionMatrix, action: PlayerInteractEvent.Action, player: EntityPlayer): Boolean =
  {
    var hasPermission: Boolean = true

    if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && position.getTileEntity(world) != null)
    {
      if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.Items.moduleBlockAccess) > 0)
      {
        hasPermission = false
        if (isPermittedByInterdictionMatrix(interdictionMatrix, player.getGameProfile().getName(), MFFSPermissions.blockAccess))
        {
          hasPermission = true
        }
      }
    }
    if (hasPermission)
    {
      if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.Items.moduleBlockAlter) > 0 && (player.getCurrentEquippedItem != null || action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
      {
        hasPermission = false
        if (isPermittedByInterdictionMatrix(interdictionMatrix, player.getGameProfile().getName(), MFFSPermissions.blockAlter))
        {
          hasPermission = true
        }
      }
    }
    return hasPermission
  }
}