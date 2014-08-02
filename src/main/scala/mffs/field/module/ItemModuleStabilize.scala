package mffs.field.module

import java.util.Set

import mffs.base.{ItemModule, TilePacketType}
import mffs.field.mode.ItemModeCustom
import mffs.{Content, ModularForceFieldSystem, Reference}
import net.minecraft.block.{Block, BlockLiquid}
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidBlock
import resonant.api.mffs.Blacklist
import resonant.api.mffs.event.EventStabilize
import resonant.api.mffs.machine.IProjector
import resonant.lib.network.discriminator.PacketTile
import universalelectricity.core.transform.vector.{Vector3, VectorWorld}

class ItemModuleStabilize extends ItemModule
{
  private var blockCount: Int = 0

  setMaxStackSize(1)
  setCost(20)

  override def onProject(projector: IProjector, fields: Set[Vector3]): Boolean =
  {
    blockCount = 0
    return false
  }

  override def onProject(projector: IProjector, position: Vector3): Int =
  {
    val tile = projector.asInstanceOf[TileEntity]
    val world = tile.getWorldObj
    var blockInfo: (Block, Int) = null

    if (projector.getTicks % 40 == 0)
    {
      if (projector.getMode.isInstanceOf[ItemModeCustom] && !(projector.getModuleCount(Content.moduleCamouflage) > 0))
      {
        val fieldBlocks = projector.getMode.asInstanceOf[ItemModeCustom].getFieldBlockMap(projector, projector.getModeStack)
        val fieldCenter: Vector3 = new Vector3(tile) + projector.getTranslation
        val relativePosition: Vector3 = position.clone.subtract(fieldCenter)
        relativePosition.apply(new Rotation(-projector.getRotationYaw, -projector.getRotationPitch, 0))
        blockInfo = fieldBlocks(relativePosition.round)
      }

      for (direction <- ForgeDirection.VALID_DIRECTIONS)
      {
        val tileEntity = (new VectorWorld(tile) + direction).getTileEntity

        if (tileEntity.isInstanceOf[IInventory])
        {
          val inventory: IInventory = (tileEntity.asInstanceOf[IInventory])

          var i: Int = 0

          while (i < inventory.getSizeInventory)
          {
            val checkStack: ItemStack = inventory.getStackInSlot(i)

            if (checkStack != null)
            {
              val evt: EventStabilize = new EventStabilize(world, position.xi, position.yi, position.zi, checkStack)
              MinecraftForge.EVENT_BUS.post(evt)

              if (!evt.isCanceled)
              {
                if (checkStack.getItem.isInstanceOf[ItemBlock])
                {
                  val itemBlock = checkStack.getItem.asInstanceOf[ItemBlock].field_150939_a

                  if (blockInfo == null || (blockInfo._1 == itemBlock && (blockInfo._2 == checkStack.getItemDamage || projector.getModuleCount(Content.moduleApproximation) > 0)) || (projector.getModuleCount(Content.moduleApproximation) > 0 && isApproximationEqual(blockInfo._1, checkStack)))
                  {
                    try
                    {
                      if (world.canPlaceEntityOnSide(itemBlock, position.xi, position.yi, position.zi, false, 0, null, checkStack))
                      {
                        val metadata = if (blockInfo != null) blockInfo._2 else (if (checkStack.getHasSubtypes) checkStack.getItemDamage else 0)
                        val block = if (blockInfo != null) blockInfo._1 else null

                        if (Blacklist.stabilizationBlacklist.contains(block) || block.isInstanceOf[BlockLiquid] || block.isInstanceOf[IFluidBlock])
                        {
                          return 1
                        }

                        val copyStack = checkStack.copy
                        inventory.decrStackSize(i, 1)
                        (copyStack.getItem.asInstanceOf[ItemBlock]).placeBlockAt(copyStack, null, world, position.xi, position.yi, position.zi, 0, 0, 0, 0, metadata)
                        ModularForceFieldSystem.packetHandler.sendToAllInDimension(new PacketTile(tile, TilePacketType.effect.id: Integer, 1: Integer, position.xi: Integer, position.yi: Integer, position.zi: Integer), world)

                        blockCount += 1;

                        if (blockCount >= projector.getModuleCount(Content.moduleSpeed) / 3)
                        {
                          return 2
                        }
                        else
                        {
                          return 1
                        }
                      }
                    }
                    catch
                      {
                        case e: Exception =>
                        {
                          Reference.logger.error("Stabilizer failed to place item '" + checkStack + "'. The item or block may not have correctly implemented the placement methods.")
                          e.printStackTrace
                        }
                      }
                  }
                }
              }
              else
              {
                return 1
              }
            }
            i += 1
          }

        }
      }

    }
    return 1
  }

  private def isApproximationEqual(block: Block, checkStack: ItemStack): Boolean =
  {
    return block == Blocks.grass && (checkStack.getItem.asInstanceOf[ItemBlock]).field_150939_a == Blocks.dirt
  }

  override def getFortronCost(amplifier: Float): Float =
  {
    return super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier)
  }
}