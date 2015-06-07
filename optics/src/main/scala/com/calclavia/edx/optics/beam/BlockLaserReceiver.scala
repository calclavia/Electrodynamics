package com.calclavia.edx.optics.beam

import java.util.function.Supplier
import java.util.{Set => JSet}

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import com.calclavia.edx.optics.content.{OpticsModels, OpticsTextures}
import com.calclavia.edx.optics.grid.OpticHandler
import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block.{BlockPlaceEvent, RightClickEvent}
import nova.core.block.Stateful
import nova.core.block.component.{LightEmitter, StaticBlockRenderer}
import nova.core.component.renderer.ItemRenderer
import nova.core.component.transform.Orientation
import nova.core.network.Syncable
import nova.core.render.model.Model
import nova.core.retention.Storable
import nova.core.util.Direction
import nova.core.util.transform.matrix.Quaternion
import nova.core.util.transform.vector.Vector3d
import nova.scala.component.IO
import nova.scala.util.ExtendedUpdater

/**
 * A block that receives laser light and generates a voltage.
 * @author Calclavia
 */
class BlockLaserReceiver extends BlockEDX with Stateful with ExtendedUpdater with Storable with Syncable {
	private val electricNode = new NodeElectricComponent(this)
	private val orientation = add(new Orientation(this)).hookBlockEvents()
	private val laserHandler = add(new OpticHandler(this))
	private val io = add(new IO(this))
	private val renderer = add(new StaticBlockRenderer(this))
	private val itemRenderer = add(new ItemRenderer(this))
	private val lightEmitter = add(new LightEmitter())

	orientation.setMask(63)

	collider.isCube(false)
	collider.isOpaqueCube(false)

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

	electricNode.setResistance(100)

	placeEvent.add((evt: BlockPlaceEvent) => {
		io.setIOAlternatingOrientation()
		world.markStaticRender(position)
	})

	rightClickEvent.add((evt: RightClickEvent) => {
		io.setIOAlternatingOrientation()
		world.markStaticRender(position)
	})

	laserHandler.onReceive.add((evt: ReceiveBeamEvent) => {
		//if (hit.sideHit == getDirection.ordinal)
		{
			//TODO: Change voltage until power = energy
			electricNode.generateVoltage(evt.receivingPower)
		}
	})

	renderer.setOnRender(
		(model: Model) => {
			val rot = orientation.orientation match {
				case Direction.UP => Quaternion.fromAxis(Vector3d.xAxis, -Math.PI / 2)
				case Direction.DOWN => Quaternion.fromAxis(Vector3d.xAxis, Math.PI / 2)
				case Direction.NORTH => Quaternion.fromAxis(Vector3d.yAxis, Math.PI / 2)
				case Direction.SOUTH => Quaternion.fromAxis(Vector3d.yAxis, -Math.PI / 2)
				case Direction.WEST => Quaternion.fromAxis(Vector3d.yAxis, Math.PI)
				case Direction.EAST => Quaternion.fromAxis(Vector3d.yAxis, 0)
				case _ => Quaternion.identity
			}

			model.rotate(rot)

			if (orientation.orientation.y == 0) {
				model.rotate(Vector3d.yAxis, -Math.PI / 2)
			}
			else {
				model.rotate(Vector3d.xAxis, Math.PI)
			}

			model.children.add(OpticsModels.laserReceiverModel.getModel)
			model.bindAll(OpticsTextures.laserReceiverTexture)
		}
	)

	override def getID: String = "laserReceiver"
}
