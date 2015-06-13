package com.calclavia.edx.optics.beam

import java.util.function.Supplier
import java.util.{Set => JSet}

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import com.calclavia.edx.optics.content.{OpticsModels, OpticsTextures}
import com.calclavia.edx.optics.grid.OpticHandler
import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import nova.core.block.Block.{PlaceEvent, RightClickEvent}
import nova.core.block.Stateful
import nova.core.block.component.{LightEmitter, StaticBlockRenderer}
import nova.core.component.renderer.ItemRenderer
import nova.core.component.transform.Orientation
import nova.core.network.Syncable
import nova.core.render.model.Model
import nova.core.retention.Storable
import nova.core.util.Direction
import nova.scala.component.IO
import nova.scala.util.ExtendedUpdater
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}

/**
 * A block that receives laser light and generates a voltage.
 * @author Calclavia
 */
class BlockLaserReceiver extends BlockEDX with Stateful with ExtendedUpdater with Storable with Syncable {
	private val electricNode = new NodeElectricComponent(this)
	private val orientation = add(new Orientation(this)).hookBasedOnEntity().hookRightClickRotate()
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

	events.add(
		(evt: PlaceEvent) => {
			io.setIOAlternatingOrientation()
			world.markStaticRender(position)
		}, classOf[PlaceEvent]
	)

	events.add(
		(evt: RightClickEvent) => {
			io.setIOAlternatingOrientation()
			world.markStaticRender(position)
		},
		classOf[RightClickEvent]
	)

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
				case Direction.UP => new Rotation(Vector3D.PLUS_I, -Math.PI / 2)
				case Direction.DOWN => new Rotation(Vector3D.PLUS_I, Math.PI / 2)
				case Direction.NORTH => new Rotation(Vector3D.PLUS_J, Math.PI / 2)
				case Direction.SOUTH => new Rotation(Vector3D.PLUS_J, -Math.PI / 2)
				case Direction.WEST => new Rotation(Vector3D.PLUS_J, Math.PI)
				case Direction.EAST => new Rotation(Vector3D.PLUS_J, 0)
				case _ => Rotation.IDENTITY
			}

			model.matrix.rotate(rot)

			if (orientation.orientation.y == 0) {
				model.matrix.rotate(Vector3D.PLUS_J, -Math.PI / 2)
			}
			else {
				model.matrix.rotate(Vector3D.PLUS_I, Math.PI)
			}

			model.children.add(OpticsModels.laserReceiver.getModel)
			model.bindAll(OpticsTextures.laserReceiver)
		}
	)

	override def getID: String = "laserReceiver"
}
