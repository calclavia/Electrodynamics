package com.calclavia.edx.optics.component

import java.util.Optional

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.core.{EDX, Placeholder}
import com.calclavia.edx.optics.api.machine.IActivatable
import com.calclavia.edx.optics.content.OpticsTextures
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful
import nova.core.component.renderer.{ItemRenderer, StaticRenderer}
import nova.core.component.transform.Orientation
import nova.core.game.InputManager.Key
import nova.core.network.NetworkTarget.Side
import nova.core.network.{Packet, Syncable}
import nova.core.render.pipeline.{BlockRenderer, RenderStream}
import nova.core.render.texture.Texture
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

	val redstoneNode = add(EDX.components.make(classOf[Redstone], this))

	val itemRenderer = add(new ItemRenderer(this))

	val staticRenderer = add(new StaticRenderer(this))

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

	staticRenderer.setOnRender(
		RenderStream.of(new BlockRenderer(this))
			.withTexture(func[Direction, Optional[Texture]]((side: Direction) => Optional.of(OpticsTextures.machine)))
			.build()
	)

	collider.isCube(false)
	collider.isOpaqueCube(false)

	events.on(classOf[RightClickEvent]).bind((evt: RightClickEvent) => onRightClick(evt))

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
		if (active != flag) {
			active = flag
			if (EDX.network.isServer) {
				EDX.network.sync(BlockPacketID.description, this)
			}
			world().markStaticRender(transform.position)
		}
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
