package mffs.util

import com.mojang.authlib.GameProfile
import mffs.Content
import mffs.field.TileElectromagneticProjector
import mffs.field.mode.ItemModeCustom
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import resonant.api.mffs.fortron.FrequencyGridRegistry
import resonant.api.mffs.machine.IProjector
import resonant.engine.grid.frequency.FrequencyGrid
import resonant.lib.access.java.Permission
import universalelectricity.core.transform.rotation.Rotation
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * A class containing some general helpful functions.
 *
 * @author Calclavia
 */
object MFFSUtility
{
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
          if (projector.getModuleCount(Content.moduleCamouflage) > 0)
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
  def getRelevantProjectors(world: World, position: Vector3): mutable.Set[TileElectromagneticProjector] =
  {
    return FrequencyGridRegistry.instance.asInstanceOf[FrequencyGrid].getNodes(classOf[TileElectromagneticProjector]) filter (_.isInField(position))
  }

  def hasPermission(world: World, position: Vector3, permission: Permission, player: EntityPlayer): Boolean =
  {
    return hasPermission(world, position, permission, player.getGameProfile())
  }

  def hasPermission(world: World, position: Vector3, permission: Permission, profile: GameProfile): Boolean =
  {
    return getRelevantProjectors(world, position).forall(_.hasPermission(profile, permission))
  }

  def hasPermission(world: World, position: Vector3, action: PlayerInteractEvent.Action, player: EntityPlayer): Boolean =
  {
    return getRelevantProjectors(world, position) forall (_.isAccessGranted(world, position, player, action))
  }
}