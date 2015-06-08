package com.calclavia.edx.optics.beam

import com.calclavia.edx.core.EDX
import nova.core.block.Block
import nova.core.block.Block.{BlockPlaceEvent, RightClickEvent}
import nova.core.component.Component
import nova.core.gui.InputManager.Key
import nova.core.network.{Sync, Syncable}
import nova.core.retention.{Storable, Store}
import nova.core.util.math.Vector3DUtil
import nova.scala.wrapper.FunctionalWrapper
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
/**
 * Devices that can focus on specific angles
 *
 * @author Calclavia
 */
class Focus(val block: Block) extends Component with Storable with Syncable {

	//The normal representing the direction of focus
	@Store
	@Sync
	var normal = Vector3D.ZERO

	block.placeEvent.add((evt: BlockPlaceEvent) => {
		normal = evt.placer.rotation.applyTo(Vector3DUtil.FORWARD)
	})

	block.rightClickEvent.add((evt: RightClickEvent) => {
		if (EDX.input.isKeyDown(Key.KEY_LSHIFT)) {
			lookAt(evt.side.toVector + block.position)
		}
		lookAt(evt.position + block.position)
	})

	def lookAt(pos: Vector3D) {
		normal = ((pos - block.position) - 0.5).normalize
	}
}