package com.calclavia.edx.electric.circuit.source

import java.util.function.Supplier
import java.util.{Collections, Set => JSet}

import com.calclavia.edx.core.EDX
import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import nova.core.block.Block.{DropEvent, PlaceEvent, RightClickEvent}
import nova.core.component.renderer.{ItemRenderer, StaticRenderer}
import nova.core.component.transform.Orientation
import nova.core.event.bus.Event
import nova.core.network.{Sync, Syncable}
import nova.core.render.model.Model
import nova.core.retention.{Storable, Store}
import nova.energy.EnergyStorage
import nova.minecraft.redstone.Redstone
import nova.scala.component.IO
import nova.scala.util.ExtendedUpdater
import nova.scala.wrapper.FunctionalWrapper._

/** A modular battery box that allows shared connections withPriority boxes next to it.
  *
  * @author Calclavia
  */
object BlockBattery {
	/** Tiers: 0, 1, 2 */
	final val maxTier = 2

	/**
	 * @param tier - 0, 1, 2
	 * @return
	 */
	def getEnergyForTier(tier: Int) = Math.round(Math.pow(500000000, (tier / (maxTier + 0.7f)) + 1) / 500000000) * 500000000
}

class BlockBattery extends BlockEDX with Syncable with Storable with ExtendedUpdater {

	private val electricNode = add(new NodeElectricComponent(this))
	private val orientation = add(new Orientation(this)).hookBasedOnEntity().hookRightClickRotate()
	@Store
	private val io = add(new IO(this))
	private val redstone = add(classOf[Redstone])
	private val staticRenderer = add(new StaticRenderer())
	private val itemRenderer = add(new ItemRenderer(this))
	//TODO: Remove debug
	@Store
	var mode = 0
	@Store
	@Sync
	private var tier = 0
	private var energyRenderLevel = 0

	/**
	 * Components
	 */
	electricNode.setPositiveConnections(
		new ConnectionBuilder(classOf[Electric])
			.setBlock(this)
			.setConnectMask(supplier(() => io.inputMask))
			.adjacentWireSupplier()
			.asInstanceOf[Supplier[JSet[Electric]]]
	)
	electricNode.setNegativeConnections(
		new ConnectionBuilder(classOf[Electric])
			.setBlock(this)
			.setConnectMask(supplier(() => io.outputMask))
			.adjacentWireSupplier()
			.asInstanceOf[Supplier[JSet[Electric]]]
	)

	electricNode.setResistance(10)
	electricNode.asInstanceOf[NodeElectricComponent]

	collider.isCube(false)
	collider.isOpaqueCube(false)

	events.add(
		(evt: PlaceEvent) => {
			io.setIOAlternatingOrientation()
		},
		classOf[PlaceEvent]
	)

	staticRenderer.onRender(
		(model: Model) => {
			//TODO: Switch the model
			val subModel = ElectricContent.batteryModel.getModel
			model.children.add(subModel)
			subModel.bindAll(ElectricContent.batteryTexture)
		}
	)
	/*
		@SideOnly(Side.CLIENT)
		override def renderInventoryItem(`type`: ItemRenderType, itemStack: ItemStack, data: AnyRef*) {
			glPushMatrix()
			val energyLevel = ((itemStack.getItem.asInstanceOf[ItemBlockBattery].getEnergy(itemStack) / itemStack.getItem.asInstanceOf[ItemBlockBattery].getEnergyCapacity(itemStack)) * 8).toInt
			RenderUtility.bind(Reference.domain, Reference.modelPath + "battery/battery.png")
			var disabledParts = Set.empty[String]
			disabledParts ++= Set("connector", "connectorIn", "connectorOut")
			disabledParts ++= Set("coil1", "coil2", "coil3", "coil4", "coil5", "coil6", "coil7", "coil8")
			disabledParts ++= Set("coil1lit", "coil2lit", "coil3lit", "coil4lit", "coil5lit", "coil6lit", "coil7lit", "coil8lit")
			disabledParts ++= Set("frame1con", "frame2con", "frame3con", "frame4con")
			BlockBattery.model.renderAllExcept(disabledParts.toList: _*)

			for (i <- 1 until 8) {
				if (i != 1 || !disabledParts.contains("coil1")) {
					if ((8 - i) <= energyLevel)
						BlockBattery.model.renderOnly("coil" + i + "lit")
					else
						BlockBattery.model.renderOnly("coil" + i)
				}
			}
			glPopMatrix()
		}

		@SideOnly(Side.CLIENT)
		override def renderDynamic(pos: Vector3, frame: Float, pass: Int) {

		}*/

	/**
	 * Events
	 */
	io.changeEvent.add((evt: Event) => electricNode.rebuild())

	events.add(
		(evt: PlaceEvent) => {
			if (EDX.network.isServer) {
				val item = evt.item.asInstanceOf[ItemBlockBattery]
				tier = item.tier
				energy = item.energy
			}
		},
		classOf[PlaceEvent]
	)

	events.add((evt: DropEvent) => {
		val item = new ItemBlockBattery(factory())
		item.tier = tier
		item.energy = energy
		evt.drops = Collections.singleton(item)
	},
		classOf[DropEvent]
	)
	@Store
	private var energy = add(new EnergyStorage)
	events.add((evt: RightClickEvent) => if (EDX.network.isServer) mode = (mode + 1) % 10, classOf[RightClickEvent])

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (EDX.network.isServer) {
			if (redstone.getInputWeakPower > 0) {
				//TODO: Remove free energy
				energy = new EnergyStorage().setMax(BlockBattery.getEnergyForTier(tier))
				energy.value = energy.max

				//Discharge battery when current is flowing positive direction
				val voltage = Math.min(energy.max * 0.0001, energy.value)
				//TODO: Fix voltage
				electricNode.generateVoltage(Math.pow(3, mode) + 500 * mode)
				val dissipatedEnergy = electricNode.power / 20
				energy -= dissipatedEnergy
			}
			else {
				//Recharge battery when current is flowing negative direction
				energy += electricNode.power / 20
				electricNode.generateVoltage(0)
			}

			if (energy.prev != energy.value) {
				energyRenderLevel = Math.round((energy.value / BlockBattery.getEnergyForTier(tier).toDouble) * 8).toInt
				EDX.network.sync(this)
			}

			/**
			 * Update packet when energy level changes.

      val prevEnergyLevel = energyRenderLevel
      energyRenderLevel = Math.round((energy.value / TileBattery.getEnergyForTier(getBlockMetadata).toDouble) * 8).toInt

      if (prevEnergyLevel != energyRenderLevel)
      {
        markUpdate()
      }
			 */
		}
	}

	override def getID: String = "battery"

}