package com.calclavia.edx.electric.circuit.source

import java.util.function.Supplier
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import nova.scala.wrapper.FunctionalWrapper
import FunctionalWrapper._
import nova.core.block.Stateful
import nova.core.block.component.ConnectedTextureRenderer
import nova.core.component.renderer.ItemRenderer
import com.calclavia.edx.core.EDX
import nova.core.render.model.{BlockModelUtil, Model}
import nova.core.util.Direction
import nova.core.util.transform.shape.Cuboid
import nova.scala.component.IO
import nova.scala.util.ExtendedUpdater

class BlockSolarPanel extends BlockEDX with ExtendedUpdater with Stateful {

	private val electricNode = add(new NodeElectricComponent(this))
	private val io = add(new IO(this))
	private val renderer = add(new ConnectedTextureRenderer(this, ElectricContent.solarPanelTextureEdge)).setFaceMask(2)
	private val itemRenderer = add(new ItemRenderer(this))

	collider.setBoundingBox(new Cuboid(0, 0, 0, 1, 0.3f, 1))
	collider.isCube(false)
	collider.isOpaqueCube(false)

	//TODO: Solar panels should only connect to wires o nthe same side
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

		if (EDX.network.isServer) {
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