package mffs.field.module

import java.util.Set

import mffs.ModularForceFieldSystem
import mffs.base.{ItemModule, TileMFFSInventory, TilePacketType}
import mffs.field.TileElectromagnetProjector
import mffs.mobilize.event.{BlockDropDelayedEvent, BlockInventoryDropDelayedEvent, IDelayedEventHandler}
import mffs.util.MFFSUtility
import net.minecraft.block.BlockLiquid
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.IFluidBlock
import resonant.api.mffs.fortron.FrequencyGridRegistry
import resonant.api.mffs.security.IInterdictionMatrix
import resonant.api.mffs.{Blacklist, IProjector}
import resonant.lib.network.PacketTile
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAll._

class ItemModuleDisintegration extends ItemModule("moduleDisintegration")
{
  private var blockCount: Int = 0
  setMaxStackSize(1)
  setCost(20)

  override def onProject(projector: IProjector, fields: Set[Vector3]): Boolean =
  {
    this.blockCount = 0
    return false
  }

  override def onProject(projector: IProjector, position: Vector3): Int =
  {
    if (projector.getTicks % 40 == 0)
    {
      val tileEntity = projector.asInstanceOf[TileEntity]
      val block = position.getBlock(tileEntity.getWorldObj)

      if (block != null)
      {
        val blockMetadata = position.getBlockMetadata(tileEntity.getWorldObj)

        if (FrequencyGridRegistry.instance.getNodes(classOf[IInterdictionMatrix]) filter ((i: IInterdictionMatrix) => i.isActive() && new Vector3(i.asInstanceOf[TileEntity]).distance(position) <= i.getActionRange() && i.getFrequency() != projector.getFrequency()) exists (_.getModuleCount(ModularForceFieldSystem.Items.moduleBlockAlter) > 0))
          return 1

        val filterMatch = !projector.getModuleSlots().exists(
          i =>
          {
            val filterStack = projector.getStackInSlot(i)

            MFFSUtility.getFilterBlock(filterStack) != null &&
                    (filterStack.isItemEqual(new ItemStack(block, 1, blockMetadata)) ||
                            (filterStack.getItem.asInstanceOf[ItemBlock].field_150939_a == block && projector.getModuleCount(ModularForceFieldSystem.Items.moduleApproximation) > 0))
          })

        if (projector.getModuleCount(ModularForceFieldSystem.Items.moduleCamouflage) > 0 == !filterMatch)
          return 1

        if (Blacklist.disintegrationBlacklist.contains(block) || block.isInstanceOf[BlockLiquid] || block.isInstanceOf[IFluidBlock])
          return 1

        ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(projector.asInstanceOf[TileEntity], TilePacketType.FXS.id: Integer, 2: Integer, position.xi: Integer, position.yi: Integer, position.zi: Integer))

        if (projector.getModuleCount(ModularForceFieldSystem.Items.moduleCollection) > 0)
        {
          (projector.asInstanceOf[TileElectromagnetProjector]).queueEvent(new BlockInventoryDropDelayedEvent(projector.asInstanceOf[IDelayedEventHandler], 39, block, tileEntity.getWorldObj, position, projector.asInstanceOf[TileMFFSInventory]))
        }
        else
        {
          (projector.asInstanceOf[TileElectromagnetProjector]).queueEvent(new BlockDropDelayedEvent(projector.asInstanceOf[IDelayedEventHandler], 39, block, tileEntity.getWorldObj, position))
        }

        blockCount += 1

        if (blockCount >= projector.getModuleCount(ModularForceFieldSystem.Items.moduleSpeed) / 3)
          return 2
        else
          return 1
      }
    }

    return 1
  }

  override def getFortronCost(amplifier: Float): Float = super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier)

}