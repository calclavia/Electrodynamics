package com.calclavia.edx.mffs.base

import java.util.Optional

import com.calclavia.edx.mffs.api.machine.IActivatable
import com.calclavia.edx.mffs.content.Textures
import com.calclavia.graph.api.energy.NodeRedstone
import com.resonant.lib.wrapper.WrapFunctions._
import com.resonant.wrapper.core.Placeholder
import nova.core.block.component.Oriented
import nova.core.block.{Block, Stateful}
import nova.core.component.renderer.ItemRenderer
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
import nova.core.network.NetworkTarget.Side
import nova.core.network.{Packet, PacketHandler}
import nova.core.render.texture.Texture
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction
import nova.core.util.transform.vector.Vector3d
/**
 * A base block class for all MFFS blocks to inherit.
 * @author Calclavia
 */
//TODO: Redstone state is not properly saved
abstract class BlockMachine extends Block with PacketHandler with IActivatable with Stateful with Storable with CategoryMFFS {
	/**
	 * Used for client side animations.
	 */
	var animation = 0d

	var redstoneNode = Game.instance.componentManager.make(classOf[NodeRedstone], this)

	/**
	 * Is the machine active and working?
	 */
	@Stored
	private var active = false

	redstoneNode.onInputPowerChange((node: NodeRedstone) => {
		if (node.getWeakPower > 0)
			setActive(true)
		else
			setActive(false)
	})

	add(redstoneNode)
	add(new ItemRenderer(this))

	//	stepSound = Block.soundTypeMetal

	//	override def getExplosionResistance(entity: Entity): Float = 100

	override def getHardness: Double = Double.PositiveInfinity

	override def getResistance: Double = 100

	override def getTexture(side: Direction): Optional[Texture] = Optional.of(Textures.machine)

	override def isOpaqueCube: Boolean = false

	override def read(packet: Packet) {
		super.read(packet)

		if (Side.get().isClient) {
			if (packet.getID == PacketBlock.description) {
				val prevActive = active
				active = packet.readBoolean()

				if (prevActive != this.active)
					world.markStaticRender(position())
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
		Game.instance.networkManager.sync(PacketBlock.description, this)
		world().markStaticRender(position())
	}
	override def onRightClick(entity: Entity, side: Int, hit: Vector3d): Boolean = {
		active = !active
		if (Placeholder.isHoldingConfigurator(entity)) {
			if (Game.instance.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
				if (Side.get().isServer) {
					//TODO: Fix this
					// InventoryUtility.dropBlockAsItem(world, position)
					world.setBlock(position, null)
					return true
				}
				return false
			}
		}

		val opOriented = getComponent(classOf[Oriented])

		if (opOriented.isPresent) {
			return opOriented.get().rotate(side, hit)
		}

		return false
	}
}
