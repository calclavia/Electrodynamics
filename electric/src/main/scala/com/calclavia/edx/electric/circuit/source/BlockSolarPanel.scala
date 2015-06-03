package com.calclavia.edx.electric.circuit.source

import java.util.function.Supplier
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import com.resonant.lib.WrapFunctions._
import nova.core.block.Stateful
import nova.core.block.component.ConnectedTextureRenderer
import nova.core.component.renderer.ItemRenderer
import nova.core.game.Game
import nova.core.render.model.{BlockModelUtil, Model}
import nova.core.util.Direction
import nova.core.util.transform.shape.Cuboid
import nova.scala.{ExtendedUpdater, IO}

class BlockSolarPanel extends BlockEDX with ExtendedUpdater with Stateful {

	private val electricNode = add(new NodeElectricComponent(this))
	private val io = add(new IO(this))
	private val renderer = add(new ConnectedTextureRenderer(this, ElectricContent.solarPanelTextureEdge)).setFaceMask(2)
	private val itemRenderer = add(new ItemRenderer(this))

	io.mask = 728

	collider.setBoundingBox(new Cuboid(0, 0, 0, 1, 0.3f, 1))
	collider.isCube(false)
	collider.isOpaqueCube(false)

	electricNode.setPositiveConnections(new ConnectionBuilder(classOf[Electric]).setBlock(this).setConnectMask(io.inputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])
	electricNode.setNegativeConnections(new ConnectionBuilder(classOf[Electric]).setBlock(this).setConnectMask(io.outputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])

	renderer.setTexture(
		func((dir: Direction) => {
			dir match {
				case Direction.DOWN => Optional.of(ElectricContent.solarPanelTextureBottom)
				case Direction.UP => Optional.of(ElectricContent.solarPanelTextureTop)
				case _ => Optional.of(ElectricContent.solarPanelTextureSide)
			}
		})
	)

	itemRenderer.setOnRender((model: Model) => BlockModelUtil.drawBlock(model, this))

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