package com.calclavia.edx.electric.circuit.component.laser

import java.util.function.Supplier
import java.util.{Set => JSet}

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import com.resonant.lib.WrapFunctions._
import nova.core.block.Stateful
import nova.core.block.component.{LightEmitter, StaticBlockRenderer}
import nova.core.component.renderer.ItemRenderer
import nova.core.component.transform.Orientation
import nova.core.event.Event
import nova.core.render.model.Model
import nova.core.util.Direction
import nova.core.util.transform.matrix.Quaternion
import nova.core.util.transform.vector.Vector3d
import nova.scala.IO
/**
 * A block that receives laser light and generates a voltage.
 * @author Calclavia
 */
class BlockLaserReceiver extends BlockEDX with Stateful
{
	private val electricNode = new NodeElectricComponent(this)
	private val orientation = add(new Orientation(this)).hookBlockEvents()
	private val laserHandler = add(new LaserHandler(this))
	private val io = add(new IO(this))
	private val renderer = add(new StaticBlockRenderer(this))
	private val itemRenderer = add(new ItemRenderer(this))
	private val lightEmitter = add(new LightEmitter())

	orientation.setMask(63)

	collider.isCube(false)
	collider.isOpaqueCube(false)

	electricNode.setPositiveConnections(new ConnectionBuilder(classOf[Electric]).setBlock(this).setConnectMask(io.inputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])
	electricNode.setNegativeConnections(new ConnectionBuilder(classOf[Electric]).setBlock(this).setConnectMask(io.outputMask).adjacentSupplier().asInstanceOf[Supplier[JSet[Electric]]])
	electricNode.setResistance(100)

	laserHandler.onPowerChange.add((evt: Event) => {
		//if (hit.sideHit == getDirection.ordinal)
		{
			//TODO: Change voltage until power = energy
			electricNode.generateVoltage(laserHandler.receivingPower)
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

			if (orientation.orientation.y == 0)
				model.rotate(Vector3d.yAxis, -Math.PI / 2)
			else
				model.rotate(Vector3d.xAxis, Math.PI)

			model.children.add(ElectricContent.laserReceiverModel.getModel)
			model.bindAll(ElectricContent.laserReceiverTexture)
		}
	)

	override def getID: String = "laserReceiver"
}
