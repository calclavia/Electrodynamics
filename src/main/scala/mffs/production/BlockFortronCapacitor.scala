package mffs.production

import java.util.{HashSet => JHashSet, Set => JSet}

import mffs.GraphFrequency
import mffs.api.card.CoordLink
import mffs.api.fortron.{Fortron, FortronCapacitor, FortronFrequency}
import mffs.base.{BlockModuleHandler, PacketBlock}
import mffs.content.{Content, Models, Textures}
import mffs.util.{FortronUtility, TransferMode}
import nova.core.block.Block
import nova.core.fluid.TankProvider
import nova.core.inventory.InventorySimple
import nova.core.item.Item
import nova.core.network.Sync
import nova.core.render.model.Model
import nova.core.retention.Stored
import nova.core.util.transform.MatrixStack

import scala.collection.convert.wrapAll._

class BlockFortronCapacitor extends BlockModuleHandler with FortronCapacitor {

	override protected val inventory: InventorySimple = new InventorySimple(3 + 4 * 2 + 1)
	private var tickAccumulator = 0d

	capacityBase = 700
	capacityBoost = 10
	startModuleIndex = 1

	@Sync(ids = Array(PacketBlock.description.ordinal(), PacketBlock.toggleMode.ordinal()))
	@Stored
	private var transferMode = TransferMode.equalize

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		consumeCost()

		if (isActive) {
			/**
			 * Handle fortron item inputs
			 */
			getInputStacks
				.collect {
				case provider: TankProvider =>
					provider.getTanks.collect {
						case tank if tank.hasFluidType(Fortron.fortronFactory) => tank
					}
			}
				.flatten
				.foreach(tank => {
				val fluid = tank.removeFluid(Math.min(getFortronEmpty, getTransmissionRate), true)
				if (fluid.isPresent) {
					addFortron(fluid.get().amount(), true)
				}
			})

			/**
			 * Handle fortron item outputs
			 */
			if (fortronTank.getFluidAmount > 0) {

				getOutputStacks
					.collect {
					case provider: TankProvider =>
						provider.getTanks.collect {
							case tank if tank.hasFluidType(Fortron.fortronFactory) => tank
						}
				}
					.flatten
					.foreach(
						tank => {
							val fluid = fortronTank.removeFluid(Math.min(fortronTank.getFluidAmount, getTransmissionRate), true)
							if (fluid.isPresent) {
								tank.addFluid(fluid.get, true)
							}
						}
					)
			}

			tickAccumulator += deltaTime
			if (tickAccumulator % 0.5 < tickAccumulator) {
				tickAccumulator %= 0.5

				/**
				 * Transfer based on input/output slots
				 */
				FortronUtility.transferFortron(this, getInputDevices, TransferMode.fill, getTransmissionRate / 2)
				FortronUtility.transferFortron(this, getOutputDevices, TransferMode.drain, getTransmissionRate / 2)

				/**
				 * Transfer based on frequency
				 */
				FortronUtility.transferFortron(this, getFrequencyDevices, transferMode, getTransmissionRate / 2)
			}
		}
	}

	def getTransmissionRate: Int = 500 + 100 * getModuleCount(Content.moduleSpeed)

	override def getFrequencyDevices: Set[FortronFrequency] =
		GraphFrequency.instance.get(getFrequency)
			.view
			.collect { case f: FortronFrequency with Block => f}
			.filter(_.world.equals(world()))
			.filter(_.position().distance(position()) < getTransmissionRange)
			.toSet[FortronFrequency]

	def getTransmissionRange: Int = 15 + getModuleCount(Content.moduleScale)

	def getInputDevices: Set[FortronFrequency] = getDevicesFromStacks(getInputStacks)

	def getDevicesFromStacks(stacks: Set[Item]): Set[FortronFrequency] =
		stacks
			.view
			.collect { case item: CoordLink if item.getLink != null => item.getLink}
			.map(linkPos => linkPos._1.getBlock(linkPos._2))
			.collect { case op if op.isPresent => op.get()}
			.collect { case freqBlock: FortronFrequency => freqBlock}
			.toSet

	def getInputStacks: Set[Item] =
		(4 to 7)
			.map(inventory.get)
			.collect { case op if op.isPresent => op.get}
			.toSet

	def getOutputDevices: Set[FortronFrequency] = getDevicesFromStacks(getOutputStacks)

	def getOutputStacks: Set[Item] =
		(8 to 11)
			.map(inventory.get)
			.collect { case op if op.isPresent => op.get}
			.toSet

	override def getAmplifier: Float = 0f

	def getDeviceCount = getFrequencyDevices.size + getInputDevices.size + getOutputDevices.size

	/*
	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean = {
		if (slotID == 0) {
			return Item.getItem.isInstanceOf[ItemCardFrequency]
		}
		else if (slotID < 4) {
			return Item.getItem.isInstanceOf[IModule]
		}

		return true
	}*/

	def getTransferMode: TransferMode = transferMode

	override def renderDynamic(model: Model) {
		model.matrix = new MatrixStack()
			.loadMatrix(model.matrix)
			.translate(0, 0.15, 0)
			.scale(1.3, 1.3, 1.3)
			.getMatrix

		model.children.add(Models.fortronCapacitor.getModel)

		if (isActive) {
			model.bind(Textures.fortronCapacitorOn)
		}
		else {
			model.bind(Textures.fortronCapacitorOff)
		}
	}

	override def renderStatic(model: Model) {

	}

	override def renderItem(model: Model) = renderDynamic(model)

	override def getID: String = "fortronCapacitor"
}