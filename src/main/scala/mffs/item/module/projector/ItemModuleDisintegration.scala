package mffs.item.module.projector

import java.util.Set
import mffs.IDelayedEventHandler
import mffs.MFFSHelper
import mffs.ModularForceFieldSystem
import mffs.base.TileMFFS.TilePacketType
import mffs.base.TileMFFSInventory
import mffs.event.BlockDropDelayedEvent
import mffs.event.BlockInventoryDropDelayedEvent
import mffs.item.module.ItemModule
import mffs.tile.TileForceFieldProjector
import net.minecraft.block.Block
import net.minecraft.block.BlockFluid
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.IFluidBlock
import universalelectricity.api.vector.Vector3
import calclavia.api.mffs.Blacklist
import calclavia.api.mffs.IProjector
import resonant.lib.network.PacketHandler
import calclavia.api.mffs.fortron.FrequencyGrid
import scala.collection.convert.wrapAll._
import calclavia.api.mffs.security.IInterdictionMatrix

class ItemModuleDisintegration(id: Integer) extends ItemModule(id, "moduleDisintegration")
{
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
			val blockID = position.getBlockID(tileEntity.worldObj)
			val block = Block.blocksList(blockID)

			if (block != null)
			{
				val blockMetadata = position.getBlockMetadata(tileEntity.worldObj)

				if (FrequencyGrid.instance().get().filter(i => i.isInstanceOf[IInterdictionMatrix] && i.asInstanceOf[IInterdictionMatrix].isActive() && new
								Vector3(i.asInstanceOf[TileEntity]).distance(position) <= i.asInstanceOf[IInterdictionMatrix].getActionRange() && i.asInstanceOf[IInterdictionMatrix].getFrequency() != projector.getFrequency()).exists(_.asInstanceOf[IInterdictionMatrix].getModuleCount(ModularForceFieldSystem.itemModuleBlockAlter) > 0))
					return 1

				val filterMatch = !projector.getModuleSlots.exists(
					i =>
					{
						val filterStack = projector.getStackInSlot(i)

						MFFSHelper.getFilterBlock(filterStack) != null &&
								(filterStack.isItemEqual(new
												ItemStack(blockID, 1, blockMetadata)) ||
										((filterStack.getItem.asInstanceOf[ItemBlock]).getBlockID == blockID && projector.getModuleCount(ModularForceFieldSystem.itemModuleApproximation) > 0))
					}
				)

				if (projector.getModuleCount(ModularForceFieldSystem.itemModuleCamouflage) > 0 == !filterMatch)
					return 1

				if (Blacklist.disintegrationBlacklist.contains(block) || block.isInstanceOf[BlockFluid] || block.isInstanceOf[IFluidBlock])
					return 1

				PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(projector.asInstanceOf[TileEntity], Int.box(TilePacketType.FXS.ordinal), Int.box(2), Int.box(position.intX), Int.box(position.intY), Int.box(position.intZ)), projector.asInstanceOf[TileEntity].worldObj)

				if (projector.getModuleCount(ModularForceFieldSystem.itemModuleCollection) > 0)
				{
					(projector.asInstanceOf[TileForceFieldProjector]).queueEvent(new
									BlockInventoryDropDelayedEvent(projector.asInstanceOf[IDelayedEventHandler], 39, block, tileEntity.worldObj, position, projector.asInstanceOf[TileMFFSInventory]))
				}
				else
				{
					(projector.asInstanceOf[TileForceFieldProjector]).queueEvent(new
									BlockDropDelayedEvent(projector.asInstanceOf[IDelayedEventHandler], 39, block, tileEntity.worldObj, position))
				}

				blockCount += 1

				if (blockCount >= projector.getModuleCount(ModularForceFieldSystem.itemModuleSpeed) / 3)
					return 2
				else
					return 1
			}
		}

		return 1
	}

	override def getFortronCost(amplifier: Float): Float =
	{
		return super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier)
	}

	private var blockCount: Int = 0
}