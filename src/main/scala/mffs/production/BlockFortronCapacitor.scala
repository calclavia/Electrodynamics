package mffs.production

import java.util.{HashSet => JHashSet, Set => JSet}

import com.resonant.lib.wrapper.WrapFunctions._
import mffs.api.card.CoordLink
import mffs.api.fortron.{Fortron, FortronFrequency}
import mffs.api.modules.Module
import mffs.base.{BlockModuleHandler, PacketBlock}
import mffs.content.{Content, Models, Textures}
import mffs.item.card.ItemCardFrequency
import mffs.util.{FortronUtility, TransferMode}
import mffs.{GraphFrequency, Reference}
import nova.core.block.Block
import nova.core.block.components.StaticRenderer
import nova.core.entity.Entity
import nova.core.fluid.TankProvider
import nova.core.game.Game
import nova.core.inventory.InventorySimple
import nova.core.item.Item
import nova.core.network.NetworkTarget.Side
import nova.core.network.Sync
import nova.core.render.model.Model
import nova.core.retention.Stored
import nova.core.util.transform.{MatrixStack, Vector3d}

import scala.collection.convert.wrapAll._

class BlockFortronCapacitor extends BlockModuleHandler with StaticRenderer {

	override val inventory: InventorySimple = new InventorySimple(3 + 4 * 2 + 1)
	private var tickAccumulator = 0d

	@Sync(ids = Array(PacketBlock.description, PacketBlock.toggleMode))
	@Stored
	private var transferMode = TransferMode.equalize

	inventory.isItemValidForSlot = biFunc((slot: Integer, item: Item) => {
		if (slot == 0) {
			item.isInstanceOf[ItemCardFrequency]
		}
		else if (slot < 4) {
			item.isInstanceOf[Module]
		}
		true
	})

	capacityBase = 700
	capacityBoost = 10
	startModuleIndex = 1

	override def onRightClick(entity: Entity, side: Int, hit: Vector3d): Boolean = {
		if (!super.onRightClick(entity, side, hit)) {
			if (Side.get().isServer) {
				Game.instance.guiFactory.showGui(Reference.id, getID, entity, position)
			}
		}

		return true
	}

	override def getID: String = "fortronCapacitor"

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
						case tank if tank.hasFluidType(Fortron.fortronID) => tank
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
							case tank if tank.hasFluidType(Fortron.fortronID) => tank
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

	override def getAmplifier: Float = 0f

	def getDeviceCount = getFrequencyDevices.size + getInputDevices.size + getOutputDevices.size

	def getFrequencyDevices: Set[FortronFrequency] =
		GraphFrequency.instance.get(getFrequency)
			.view
			.collect { case f: FortronFrequency with Block => f }
			.filter(_.world.equals(world()))
			.filter(_.position().distance(position()) < getTransmissionRange)
			.toSet[FortronFrequency]

	def getTransmissionRange: Int = 15 + getModuleCount(Content.moduleScale)

	def getInputDevices: Set[FortronFrequency] = getDevicesFromStacks(getInputStacks)

	def getInputStacks: Set[Item] =
		(4 to 7)
			.map(inventory.get)
			.collect { case op if op.isPresent => op.get }
			.toSet

	def getDevicesFromStacks(stacks: Set[Item]): Set[FortronFrequency] =
		stacks
			.view
			.collect { case item: CoordLink if item.getLink != null => item.getLink }
			.map(linkPos => linkPos._1.getBlock(linkPos._2))
			.collect { case op if op.isPresent => op.get() }
			.collect { case freqBlock: FortronFrequency => freqBlock }
			.toSet

	def getOutputDevices: Set[FortronFrequency] = getDevicesFromStacks(getOutputStacks)

	def getOutputStacks: Set[Item] =
		(8 to 11)
			.map(inventory.get)
			.collect { case op if op.isPresent => op.get }
			.toSet

	override def isCube: Boolean = false

	def getTransferMode: TransferMode = transferMode

	def toggleTransferMode() {
		transferMode = transferMode.toggle()
	}

	override def renderStatic(model: Model) {
		model.matrix = new MatrixStack()
			.loadMatrix(model.matrix)
			.translate(0, 0.15, 0)
			.scale(1.3, 1.3, 1.3)
			.getMatrix

		model.children.add(Models.fortronCapacitor.getModel)
		model.bindAll(if (isActive) Textures.fortronCapacitorOn else Textures.fortronCapacitorOff)
	}
}