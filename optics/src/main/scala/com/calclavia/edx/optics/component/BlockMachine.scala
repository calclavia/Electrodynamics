package com.calclavia.edx.optics.component

import java.util.Optional

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.core.{EDX, Placeholder}
import com.calclavia.edx.optics.api.machine.IActivatable
import com.calclavia.edx.optics.content.OpticsTextures
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful
import nova.core.block.component.StaticBlockRenderer
import nova.core.component.Component
import nova.core.component.misc.Collider
import nova.core.component.renderer.ItemRenderer
import nova.core.component.transform.Orientation
import nova.core.gui.InputManager.Key
import nova.core.network.NetworkTarget.Side
import nova.core.network.{Packet, Syncable}
import nova.core.retention.{Storable, Store}
import nova.core.util.Direction
import nova.minecraft.redstone.Redstone
import nova.scala.util.ExtendedUpdater
import nova.scala.wrapper.FunctionalWrapper._

/**
 * A base block class for all MFFS blocks to inherit.
 * @author Calclavia
 */
//TODO: Redstone state is not properly saved
abstract class BlockMachine extends BlockEDX with Syncable with IActivatable with Stateful with Storable with ExtendedUpdater {
	/**
	 * Used for client side animations.
	 */
	var animation = 0d

	var redstoneNode = EDX.components.make(classOf[Redstone], this)

	/**
	 * Is the machine active and working?
	 */
	@Store
	private var active = false

	redstoneNode.onInputPowerChange((node: Redstone) => {
		if (node.getOutputWeakPower > 0) {
			setActive(true)
		}
		else {
			setActive(false)
		}
	})
	add(new CategoryEDXOptics)
	add(redstoneNode.asInstanceOf[Component])
	add(new ItemRenderer(this))
	add(new StaticBlockRenderer(this))
		.setTexture(func((side: Direction) => Optional.of(OpticsTextures.machine)))

	get(classOf[Collider])
		.isCube(false)
		.isOpaqueCube(false)

	events.add((evt: RightClickEvent) => onRightClick(evt), classOf[RightClickEvent])

	//	stepSound = Block.soundTypeMetal
	//	override def getExplosionResistance(entity: Entity): Float = 100

	override def getHardness: Double = Double.PositiveInfinity

	override def getResistance: Double = 100

	override def read(packet: Packet) {
		super.read(packet)

		if (Side.get().isClient) {
			if (packet.getID == BlockPacketID.description) {
				val prevActive = active
				active = packet.readBoolean()

				if (prevActive != this.active) {
					world.markStaticRender(transform.position)
				}
			}
		}
	}

	override def write(packet: Packet) {
		super.write(packet)

		if (packet.getID == BlockPacketID.description) {
			packet <<< active
		}
	}

	def isActive: Boolean = active

	def setActive(flag: Boolean) {
		active = flag
		EDX.network.sync(BlockPacketID.description, this)
		world().markStaticRender(transform.position)
	}

	def onRightClick(evt: RightClickEvent) {
		active = !active
		if (Placeholder.isHoldingConfigurator(evt.entity)) {
			if (EDX.input.isKeyDown(Key.KEY_LSHIFT)) {
				if (Side.get().isServer) {
					//TODO: Fix this
					// InventoryUtility.dropBlockAsItem(world, position)
					world.setBlock(transform.position, null)
					evt.result = true
					return
				}
				evt.result = false
				return
			}
		}

		val opOriented = getOp(classOf[Orientation])

		if (opOriented.isPresent) {
			evt.result = opOriented.get().rotate(evt.side.ordinal(), evt.position)
			return
		}

		evt.result = false
	}
}
