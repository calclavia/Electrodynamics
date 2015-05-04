package mffs.production

import java.util

import com.resonant.core.energy.EnergyStorage
import com.resonant.core.graph.internal.Node
import com.resonant.core.graph.internal.electric.TTEBridge
import mffs.Settings
import mffs.api.modules.Module
import mffs.base.{BlockModuleHandler, PacketBlock}
import mffs.content.{Content, Models, Textures}
import mffs.item.card.ItemCardFrequency
import nova.core.fluid.Tank
import nova.core.inventory.InventorySimple
import nova.core.item.Item
import nova.core.network.NetworkTarget.Side
import nova.core.network.Packet
import nova.core.render.model.Model
import nova.core.retention.Stored
import nova.core.util.Direction
import nova.core.util.transform.Vector3d

import scala.collection.convert.wrapAll._

/**
 * A TileEntity that extract energy into Fortron.
 *
 * @author Calclavia
 */
object BlockCoercionDeriver {
	val fuelProcessTime = 10 * 20
	val productionMultiplier = 4

	/**
	 * Ration from UE to Fortron. Multiply J by this value to convert to Fortron.
	 */
	val ueToFortronRatio = 0.005f
	val energyConversionPercentage = 1

	val slotFrequency = 0
	val slotBattery = 1
	val slotFuel = 2

	/**
	 * The amount of power (watts) this machine uses.
	 */
	val power = 5000000
}

class BlockCoercionDeriver extends BlockModuleHandler with TTEBridge {
	@Stored
	var processTime: Int = 0
	@Stored
	var isInversed = false

	//Client
	var animationTween = 0f

	capacityBase = 30
	startModuleIndex = 3

	override protected val inventory = new InventorySimple(6)

	override val energy = new EnergyStorage(BlockCoercionDeriver.power)

	override def getID: String = "coercionDeriver"

	override def getTank(dir: Direction): util.Set[Tank] = Set.empty[Tank]

	//TODO: Implement this
	override def getNodes(from: Direction): util.Set[Node[_ <: Node[_]]] = null

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (Side.get().isServer) {
			if (isActive) {
				if (isInversed && Settings.enableElectricity) {
					val withdrawnElectricity = removeFortron(productionRate / 20, true) / BlockCoercionDeriver.ueToFortronRatio
					energy += withdrawnElectricity * BlockCoercionDeriver.energyConversionPercentage
					//recharge(getStackInSlot(TileCoercionDeriver.slotBattery))
				}
				else {
					if (getFortron < getFortronCapacity) {
						// discharge(getStackInSlot(TileCoercionDeriver.slotBattery))
						energy.max = getPower

						if (energy >= getPower || (!Settings.enableElectricity && isItemValidForSlot(BlockCoercionDeriver.slotFuel, inventory.get(BlockCoercionDeriver.slotFuel).orElse(null)))) {
							addFortron(productionRate, true)
							energy -= getPower

							if (processTime == 0 && isItemValidForSlot(BlockCoercionDeriver.slotFuel, inventory.get(BlockCoercionDeriver.slotFuel).orElse(null))) {
								inventory.remove(BlockCoercionDeriver.slotFuel, 1)
								processTime = BlockCoercionDeriver.fuelProcessTime * Math.max(this.getModuleCount(Content.moduleScale) / 20, 1)
							}

							if (processTime > 0) {
								processTime -= 1

								if (processTime < 1) {
									processTime = 0
								}
							}
							else {
								processTime = 0
							}
						}
					}
				}
			}
		}
		else {
			/**
			 * Handle animation
			 */
			if (isActive) {
				animation += 1

				if (animationTween < 1) {
					animationTween += 0.01f
				}
			}
			else {
				if (animationTween > 0) {
					animationTween -= 0.01f
				}
			}
		}
	}

	/**
	 * @return The Fortron production rate per tick!
	 */
	def productionRate: Int = {
		if (this.isActive) {
			var production = (getPower.asInstanceOf[Float] / 20f * BlockCoercionDeriver.ueToFortronRatio * Settings.fortronProductionMultiplier).asInstanceOf[Int]

			if (processTime > 0) {
				production *= BlockCoercionDeriver.productionMultiplier
			}

			return production
		}
		return 0
	}

	def getPower: Double = BlockCoercionDeriver.power + (BlockCoercionDeriver.power * (getModuleCount(Content.moduleSpeed) / 8d))

	def isItemValidForSlot(slotID: Int, item: Item): Boolean = {
		if (item != null) {
			if (slotID >= startModuleIndex) {
				return item.isInstanceOf[Module]
			}
			slotID match {
				case BlockCoercionDeriver.slotFrequency =>
					return item.isInstanceOf[ItemCardFrequency]
				//				case BlockCoercionDeriver.slotBattery =>
				//					return Compatibility.isHandler(item.getItem, null)
				//				case BlockCoercionDeriver.slotFuel =>
				//					return item.isItemEqual(new Item(Items.dye, 1, 4)) || item.isItemEqual(new Item(Items.quartz))
			}
		}
		return false
	}

	def canConsume: Boolean = {
		if (this.isActive && !this.isInversed) {
			return getFortron < getFortronCapacity
		}
		return false
	}

	override def write(packet: Packet) {
		super.write(packet)

		if (packet.getID == PacketBlock.description) {
			packet <<< isInversed
			packet <<< processTime
		}
	}

	override def read(packet: Packet) {
		super.read(packet)

		if (Side.get().isClient) {
			if (packet.getID == PacketBlock.description) {
				isInversed = packet.readBoolean()
				processTime = packet.readInt()
			}
		}
		else {
			if (packet.getID == PacketBlock.toggleMode) {
				isInversed = !isInversed
			}
		}
	}

	override def renderDynamic(model: Model) {
		val originalModel = Models.deriver.getModel
		val capacitorModel = new Model
		capacitorModel.children.addAll(originalModel.filterNot(_.name.equals("crystal")))
		model.children.add(capacitorModel)

		val crystalModel = new Model
		crystalModel.children.addAll(originalModel.filter(_.name.equals("crystal")))
		crystalModel.translate(0, (0.3 + Math.sin(Math.toRadians(animation)) * 0.08) * animationTween - 0.1, 0)
		crystalModel.rotate(Vector3d.yAxis, animation)
		//Enable Blending
		model.children.add(crystalModel)
		//Disable Blending
		model.bindAll(if (isActive) Textures.coercionDeriverOn else Textures.coercionDeriverOff)
	}

	override def renderItem(model: Model) = renderDynamic(model)
}