package com.calclavia.edx.electric.circuit.source

import java.util.function.Supplier
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.core.{CoreContent, EDX}
import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import nova.core.block.Stateful
import nova.core.block.component.StaticBlockRenderer
import nova.core.component.renderer.{StaticRenderer, ItemRenderer}
import nova.core.render.pipeline.{BlockRenderer, RenderStream}
import nova.core.render.texture.Texture
import nova.core.util.Direction
import nova.scala.component.IO
import nova.scala.util.ExtendedUpdater
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
class BlockThermopile extends BlockEDX with ExtendedUpdater with Stateful {
	/**
	 * The amount of ticks the thermopile will use the temperature differences before turning all
	 * adjacent sides to thermal equilibrium.
	 */
	private val maxTicks = 120 * 20
	private val electricNode = add(new NodeElectricComponent(this))
	private var ticksUsed = 0
	private val io = add(new IO(this))
	private val staticRenderer = add(new StaticRenderer(this))
	private val itemRenderer = add(new ItemRenderer(this))

	staticRenderer.setOnRender(
		RenderStream.of(new BlockRenderer(this))
			.withTexture(func[Direction, Optional[Texture]]((dir: Direction) => if (dir == Direction.UP) Optional.of(ElectricContent.thermopileTextureTop) else Optional.of(ElectricContent.thermopileTextureSide)))
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
	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (EDX.network.isServer) {
			var heatSources = 0
			var coolingSources = 0

			//TODO: Check blocks ids.

			for (dir <- Direction.DIRECTIONS) {
				val checkPos = position + dir.toVector
				val block = world.getBlock(checkPos).get

				if (block.getID.equals("water")) {
					// == Blocks.water || block == Blocks.flowing_water
					coolingSources += 1
				}
				else if (block.getID.equals("snow")) {
					//Blocks.snow
					coolingSources += 2
				}
				else if (block.getID.equals("ice")) {
					// Blocks.ice
					coolingSources += 2
				}
				else if (block.getID.equals("fire")) {
					// Blocks.fire
					heatSources += 1
				}
				else if (block.getID.equals("lava")) {
					// == Blocks.lava || block == Blocks.flowing_lava
					heatSources += 2
				}
			}
			val multiplier = 3 - Math.abs(heatSources - coolingSources)

			if (multiplier > 0 && coolingSources > 0 && heatSources > 0) {
				electricNode.generateVoltage(0.1 * multiplier)
				ticksUsed += 1

				if (ticksUsed >= maxTicks) {
					for (dir <- Direction.DIRECTIONS) {
						val checkPos = position + dir.toVector
						val block = world.getBlock(checkPos).get

						if (block.getID.equals("water")) {
							// == Blocks.water || block == Blocks.flowing_water
							world.removeBlock(checkPos)
						}
						else if (block.getID.equals("snow") || block.getID.equals("ice")) {
							//Blocks.snow
							//checkPos.setBlock(worldObj, Blocks.water)
						}
						else if (block.getID.equals("fire")) {
							// Blocks.fire
							world.removeBlock(checkPos)
						}
						else if (block.getID.equals("lava")) {
							// == Blocks.lava || block == Blocks.flowing_lava
							//checkPos.setBlock(worldObj, Blocks.stone)

						}
					}
					ticksUsed = 0
				}
			}
		}
	}

	override def getID: String = "thermopile"
}