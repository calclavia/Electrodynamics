package com.calclavia.edx.mffs.base

import java.util.Optional

import com.calclavia.edx.mffs.api.machine.IActivatable
import com.calclavia.edx.mffs.content.Textures
import com.calclavia.minecraft.redstone.Redstone
import com.resonant.lib.WrapFunctions
import WrapFunctions._
import com.resonant.wrapper.core.Placeholder
import nova.core.block.Block.RightClickEvent
import nova.core.block.component.StaticBlockRenderer
import nova.core.block.{BlockDefault, Stateful}
import nova.core.component.Component
import nova.core.component.misc.Collider
import nova.core.component.renderer.ItemRenderer
import nova.core.component.transform.Orientation
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
import nova.core.network.NetworkTarget.Side
import nova.core.network.{Packet, PacketHandler}
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction

/**
 * A base block class for all MFFS blocks to inherit.
 * @author Calclavia
 */
//TODO: Redstone state is not properly saved
abstract class BlockMachine extends BlockDefault with PacketHandler with IActivatable with Stateful with Storable {
	/**
	 * Used for client side animations.
	 */
	var animation = 0d

	var redstoneNode = Game.componentManager.make(classOf[Redstone], this)

	/**
	 * Is the machine active and working?
	 */
	@Stored
	private var active = false

	redstoneNode.onInputPowerChange((node: Redstone) => {
		if (node.getOutputWeakPower > 0)
			setActive(true)
		else
			setActive(false)
	})
	add(new CategoryMFFS)
	add(redstoneNode.asInstanceOf[Component])
	add(new ItemRenderer(this))
	add(new StaticBlockRenderer(this))
		.setTexture(func((side: Direction) => Optional.of(Textures.machine)))

	get(classOf[Collider])
		.isCube(false)
		.isOpaqueCube(false)

	rightClickEvent.add((evt: RightClickEvent) => onRightClick(evt))

	//	stepSound = Block.soundTypeMetal
	//	override def getExplosionResistance(entity: Entity): Float = 100

	override def getHardness: Double = Double.PositiveInfinity

	override def getResistance: Double = 100

	override def read(packet: Packet) {
		super.read(packet)

		if (Side.get().isClient) {
			if (packet.getID == PacketBlock.description) {
				val prevActive = active
				active = packet.readBoolean()

				if (prevActive != this.active)
					world.markStaticRender(transform.position)
			}
		}
	}

	override def write(packet: Packet) {
		super.write(packet)

		if (packet.getID == PacketBlock.description) {
			packet <<< active
		}
	}

	def isActive: Boolean = active

	def setActive(flag: Boolean) {
		active = flag
		Game.networkManager.sync(PacketBlock.description, this)
		world().markStaticRender(transform.position)
	}

	def onRightClick(evt: RightClickEvent) {
		active = !active
		if (Placeholder.isHoldingConfigurator(evt.entity)) {
			if (Game.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
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
