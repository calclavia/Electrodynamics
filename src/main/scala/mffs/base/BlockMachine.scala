package mffs.base

import java.util.Optional

import com.calclavia.graph.api.energy.NodeRedstone
import com.resonant.core.prefab.block.Rotatable
import com.resonant.lib.wrapper.WrapFunctions._
import com.resonant.wrapper.core.Placeholder
import mffs.api.machine.IActivatable
import mffs.content.Textures
import nova.core.block.Block
import nova.core.block.components.{ItemRenderer, Stateful}
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
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
abstract class BlockMachine extends Block with PacketHandler with IActivatable with Stateful with Storable with ItemRenderer with CategoryMFFS {
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

	add(redstoneNode)

	redstoneNode.onInputPowerChange((node: NodeRedstone) => {
		if (node.getWeakPower > 0)
			setActive(true)
		else
			setActive(false)
	})

	//	stepSound = Block.soundTypeMetal

	//	override def getExplosionResistance(entity: Entity): Float = 100

	override def getHardness: Double = Double.PositiveInfinity

	override def getResistance: Double = 100

	override def getTexture(side: Direction): Optional[Texture] = Optional.of(Textures.machine)

	override def isOpaqueCube: Boolean = false

	override def read(packet: Packet) {
		super.read(packet)

		if (packet.getID == PacketBlock.description) {
			val prevActive = active
			active = packet.readBoolean()

			if (prevActive != this.active)
				world.markStaticRender(position())
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
		if (Placeholder.isHoldingConfigurator(entity)) {
			if (Game.instance.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
				if (Game.instance.networkManager.isServer) {
					//TODO: Fix this
					// InventoryUtility.dropBlockAsItem(world, position)
					world.setBlock(position, null)
					return true
				}
				return false
			}
		}

		if (this.isInstanceOf[Rotatable]) {
			return this.asInstanceOf[Rotatable].rotate(side, hit)
		}

		return false
	}

	/**
	 *
	ComputerCraft
	  def getType: String =
	  {
		return this.getInvName
	  }

	  def getMethodNames: Array[String] =
	  {
		return Array[String]("isActivate", "setActivate")
	  }

	  def callMethod(computer: Nothing, context: Nothing, method: Int, arguments: Array[AnyRef]): Array[AnyRef] =
	  {
		method match
		{
		  case 0 =>
		  {
			return Array[AnyRef](this.isActive)
		  }
		  case 1 =>
		  {
			this.setActive(arguments(0).asInstanceOf[Boolean])
			return null
		  }
		}
		throw new Exception("Invalid method.")
	  }

	  def attach(computer: Nothing)
	  {
	  }

	  def detach(computer: Nothing)
	  {
	  }
	 */
}
