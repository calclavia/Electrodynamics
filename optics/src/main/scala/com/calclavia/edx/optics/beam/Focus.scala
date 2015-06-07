package com.calclavia.edx.optics.beam

import com.calclavia.edx.core.EDX
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Block.{BlockPlaceEvent, RightClickEvent}
import nova.core.component.Component
import nova.core.gui.InputManager.Key
import nova.core.network.{Sync, Syncable}
import nova.core.retention.{Storable, Store}
import nova.core.util.transform.vector.Vector3d

/**
 * Devices that can focus on specific angles
 *
 * @author Calclavia
 */
class Focus(val block: Block) extends Component with Storable with Syncable {

	//The normal representing the direction of focus
	@Store
	@Sync
	var normal = Vector3d.zero

	block.placeEvent.add((evt: BlockPlaceEvent) => {
		normal = evt.placer.rotation.toForwardVector
	})

	block.rightClickEvent.add((evt: RightClickEvent) => {
		if (EDX.input.isKeyDown(Key.KEY_LSHIFT)) {
			lookAt(evt.side.toVector.toDouble + block.position.toDouble)
		}
		lookAt(evt.position + block.position.toDouble)
	})

	def lookAt(pos: Vector3d) {
		normal = ((pos - block.position.toDouble) - 0.5).normalize
	}
}