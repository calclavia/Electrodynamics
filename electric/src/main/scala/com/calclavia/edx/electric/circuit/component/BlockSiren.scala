package com.calclavia.edx.electric.circuit.component

import java.util.function.Supplier
import java.util.{Set => JSet}

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful
import nova.core.component.renderer.{ItemRenderer, StaticRenderer}
import nova.core.render.pipeline.BlockRenderStream
import nova.core.retention.Store
import nova.core.util.Direction
import nova.minecraft.redstone.Redstone
import nova.scala.component.IO
import nova.scala.util.ExtendedUpdater
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._

/**
 * Siren block
 */
class BlockSiren extends BlockEDX with ExtendedUpdater with Stateful {
	private val electricNode = add(new NodeElectricComponent(this))
	private val io = add(new IO(this))
	private val redstone = add(classOf[Redstone])
	private val renderer = add(new StaticRenderer())
	private val itemRenderer = add(new ItemRenderer(this))

	@Store
	private var metadata = 0

	renderer.onRender(
		new BlockRenderStream(this)
			.withTexture(ElectricContent.sirenTexture)
			.build()
	)

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

	events.add((evt: RightClickEvent) => metadata = (metadata + 1) % 10, classOf[RightClickEvent])

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (ticks % 30 == 0) {
			if (world != null) {
				if (redstone.getOutputWeakPower > 0) {
					var volume: Float = 0.5f
					for (i <- 0 to 6) {
						val check = position + Direction.fromOrdinal(i).toVector
						if (world.getBlock(check).get().sameType(this)) {
							volume *= 1.5f
						}
					}
					//TODO: Add sound
					//world.playSoundAtPosition(position(), Reference.prefix + "siren", volume, 1f - 0.18f * (metadata / 15f))
				}
			}

			/*
			if (!world.isRemote) {
				val volume = electricNode.power.toFloat / 1000f
				world.playSoundEffect(x, y, z, Reference.prefix + "siren", volume, 1f - 0.18f * (metadata / 15f))
			}*/
		}
	}

	override def getID: String = "siren"
}