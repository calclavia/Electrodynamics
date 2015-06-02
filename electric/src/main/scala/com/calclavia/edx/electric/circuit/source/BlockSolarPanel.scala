package com.calclavia.edx.electrical.circuit.source

import java.util.function.Supplier
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.grid.NodeElectricComponent
import com.calclavia.edx.electric.grid.api.{ConnectionBuilder, Electric}
import com.resonant.core.prefab.block.{ExtendedUpdater, IO}
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.component.ConnectedTextureRenderer
import nova.core.component.misc.Collider
import nova.core.game.Game
import nova.core.util.Direction
import nova.core.util.transform.shape.Cuboid

class BlockSolarPanel extends Block with ExtendedUpdater {

	private val electricNode = add(new NodeElectricComponent(this))
	private val io = add(new IO(this))
	private val collider = add(new Collider())
	private val renderer = add(new ConnectedTextureRenderer(this, ElectricContent.solarPanelTextureEdge))

	io.mask = 728

	collider.setBoundingBox(new Cuboid(0, 0, 0, 1, 0.3f, 1))
	collider.isCube(false)
	collider.isOpaqueCube(false)

	electricNode.setPositiveConnections(new ConnectionBuilder(classOf[Electric], this).setConnectMask(io.inputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])
	electricNode.setNegativeConnections(new ConnectionBuilder(classOf[Electric], this).setConnectMask(io.outputMask).adjacentNodes().asInstanceOf[Supplier[JSet[Electric]]])

	renderer.setTexture(
		func((dir: Direction) => {
			dir match {
				case Direction.DOWN => Optional.of(ElectricContent.solarPanelTextureBottom)
				case _ => Optional.of(ElectricContent.solarPanelTextureSide)
			}
		})
	)

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (Game.network().isServer) {
			//if (world.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord) && !this.worldObj.provider.hasNoSky) {
			//if (world.isDaytime) {
			//if (!(world.isThundering || world.isRaining)) {
			electricNode.generateVoltage(15)
			//}
			//}
			//}
		}
	}

	override def getID: String = "solarPanel"
}