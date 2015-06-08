package com.calclavia.edx.optics.beam

import com.calclavia.edx.core.EDX
import nova.scala.wrapper.FunctionalWrapper
import FunctionalWrapper._
import nova.core.block.Block
import nova.core.block.Block.{BlockPlaceEvent, RightClickEvent}
import nova.core.component.Component
import nova.core.gui.InputManager.Key
import nova.core.network.{Sync, Syncable}
import nova.core.retention.{Storable, Store}
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
		normal = evt.placer.rotation.toForwardVector
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