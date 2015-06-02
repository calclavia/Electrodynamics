package com.calclavia.edx.electric.circuit.source

import java.util.function.Supplier
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.grid.NodeElectricComponent
import com.calclavia.edx.electric.grid.api.{ConnectionBuilder, Electric}
import com.resonant.core.prefab.block.{ExtendedUpdater, IO}
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.component.StaticBlockRenderer
import nova.core.game.Game
import nova.core.render.texture.Texture
import nova.core.util.Direction

class BlockThermopile extends Block with ExtendedUpdater {
	/**
	 * The amount of ticks the thermopile will use the temperature differences before turning all
	 * adjacent sides to thermal equilibrium.
	 */
	private val maxTicks = 120 * 20
	private val electricNode = add(new NodeElectricComponent(this))
	private var ticksUsed = 0
	private val io = add(new IO(this))
	private val renderer = add(new StaticBlockRenderer(this))

	io.mask = 728

	renderer.setTexture(func[Direction, Optional[Texture]]((dir: Direction) => if (dir == Direction.UP) Optional.of(ElectricContent.thermopileTexture) else Optional.empty[Texture]))

	electricNode.setPositiveConnections(new ConnectionBuilder(classOf[Electric], this).setConnectMask(io.inputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])
	electricNode.setNegativeConnections(new ConnectionBuilder(classOf[Electric], this).setConnectMask(io.outputMask).adjacentNodes().asInstanceOf[Supplier[JSet[Electric]]])

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (Game.network().isServer) {
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