package mffs.field.module

import java.util.Set

import mffs.base.{ItemModule, TileMFFSInventory, TilePacketType}
import mffs.field.TileElectromagneticProjector
import mffs.field.mobilize.event.{BlockDropDelayedEvent, BlockInventoryDropDelayedEvent, IDelayedEventHandler}
import mffs.util.MFFSUtility
import mffs.{Content, ModularForceFieldSystem}
import net.minecraft.block.BlockLiquid
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.IFluidBlock
import resonant.api.mffs.Blacklist
import resonant.api.mffs.machine.IProjector
import resonant.lib.network.discriminator.PacketTile
import universalelectricity.core.transform.vector.Vector3

class ItemModuleDisintegration extends ItemModule
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
    val proj = projector.asInstanceOf[TileElectromagneticProjector]
    val world = proj.world

    val tileEntity = projector.asInstanceOf[TileEntity]
    val block = position.getBlock(world)

    if (block != null)
    {
      val blockMetadata = position.getBlockMetadata(tileEntity.getWorldObj)

      val filterMatch = !proj.getFilterStacks.exists(
        itemStack =>
        {
          MFFSUtility.getFilterBlock(itemStack) != null &&
          (itemStack.isItemEqual(new ItemStack(block, 1, blockMetadata)) || (itemStack.asInstanceOf[ItemBlock].field_150939_a == block && projector.getModuleCount(Content.moduleApproximation) > 0))
        })

      if (proj.isInvertedFilter != filterMatch)
        return 1

      if (Blacklist.disintegrationBlacklist.contains(block) || block.isInstanceOf[BlockLiquid] || block.isInstanceOf[IFluidBlock])
        return 1

      ModularForceFieldSystem.packetHandler.sendToAllInDimension(new PacketTile(proj) <<< TilePacketType.effect.id <<< 2 <<< position.xi <<< position.yi <<< position.zi, world)

      if (projector.getModuleCount(Content.moduleCollection) > 0)
      {
        proj.queueEvent(new BlockInventoryDropDelayedEvent(projector.asInstanceOf[IDelayedEventHandler], 39, block, world, position, projector.asInstanceOf[TileMFFSInventory]))
      }
      else
      {
        proj.queueEvent(new BlockDropDelayedEvent(projector.asInstanceOf[IDelayedEventHandler], 39, block, world, position))
      }

      blockCount += 1

      if (blockCount >= projector.getModuleCount(Content.moduleSpeed) / 3)
        return 2
      else
        return 1
    }

    return 1
  }

  override def getFortronCost(amplifier: Float): Float = super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier)

}