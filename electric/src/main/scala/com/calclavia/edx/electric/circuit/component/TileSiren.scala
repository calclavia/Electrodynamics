package com.calclavia.edx.electrical.circuit.component

import java.util.function.Supplier
import java.util.{Set => JSet}

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.grid.api.{ConnectionBuilder, Electric}
import com.calclavia.minecraft.redstone.Redstone
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful
import nova.core.game.Game
import nova.core.retention.Stored
import nova.core.util.Direction
import nova.scala.{ExtendedUpdater, IO}

/**
 * Siren block
 */
class TileSiren extends BlockEDX with ExtendedUpdater with Stateful {
	private val electricNode = add(new NodeElectricComponent(this))
	private val io = add(new IO(this))
	private val redstone = add(Game.components().make(classOf[Redstone], this))

	@Stored
	private var metadata = 0

	electricNode.setPositiveConnections(new ConnectionBuilder(classOf[Electric]).setBlock(this).setConnectMask(io.inputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])
	electricNode.setNegativeConnections(new ConnectionBuilder(classOf[Electric]).setBlock(this).setConnectMask(io.outputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])

	rightClickEvent((evt: RightClickEvent) => metadata = (metadata + 1) % 10)

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (ticks % 30 == 0) {
			if (world != null) {
				if (redstone.getOutputWeakPower() > 0) {
					var volume: Float = 0.5f
					for (i <- 0 to 6) {
						val check: Vector3 = position.add(Direction.getOrientation(i))
						if (check.getBlock(world) == getBlockType) {
							volume *= 1.5f
						}
					}
					//TODO: Add sound
					//world.playSoundAtPosition(position(), Reference.prefix + "siren", volume, 1f - 0.18f * (metadata / 15f))
				}
			}

			if (!world.isRemote) {
				val volume = electricNode.power.toFloat / 1000f
				world.playSoundEffect(x, y, z, Reference.prefix + "siren", volume, 1f - 0.18f * (metadata / 15f))
			}
		}
	}
}