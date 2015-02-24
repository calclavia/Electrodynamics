package mffs.base

import java.util.Optional

import com.resonant.core.prefab.block.Rotatable
import com.resonant.wrapper.core.Placeholder
import com.resonant.wrapper.core.api.tile.IPlayerUsing
import mffs.Content
import mffs.api.machine.IActivatable
import nova.core.block.Block
import nova.core.block.components.Stateful
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
import nova.core.network.{Packet, PacketHandler}
import nova.core.render.texture.Texture
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction
import nova.core.util.transform.{Vector3d, Vector3i}

/**
 * A base block class for all MFFS blocks to inherit.
 * @author Calclavia
 */
abstract class BlockMFFS extends Block with PacketHandler with IActivatable with IPlayerUsing with Stateful with Storable {
	/**
	 * Used for client side animations.
	 */
	var animation = 0f

	/**
	 * Is this machine switched on internally via GUI?
	 */
	@Stored
	var isRedstoneActive = false

	/**
	 * Is the machine active and working?
	 */
	@Stored
	private var active = false

	//	blockHardness = Float.MaxValue
	//	blockResistance = 100f
	//	stepSound = Block.soundTypeMetal

	override def getTexture(side: Direction): Optional[Texture] = Optional.of(Content.machineTexture)

	override def isOpaqueCube: Boolean = false

	//	override def getExplosionResistance(entity: Entity): Float = 100

	override def read(id: Int, buf: Packet) {
		if (id == PacketBlock.description.id) {
			val prevActive = active
			active = buf.readBoolean()
			isRedstoneActive = buf.readBoolean()

			if (prevActive != this.active) {
				world.markStaticRender(position())
			}
		}
		else if (id == PacketBlock.toggleActivation.id) {
			isRedstoneActive = !isRedstoneActive

			if (isRedstoneActive) {
				setActive(true)
			}
			else {
				setActive(false)
			}
		}
	}

	def setActive(flag: Boolean) {
		active = flag
		world().markStaticRender(position())
	}

	//TODO: Implement redstone support

	override def write(id: Int, packet: Packet) {
		super.write(id, packet)

		if (id == PacketBlock.description.id) {
			packet <<< active
			packet <<< isRedstoneActive
		}
	}

	override def onNeighborChange(neighborPosition: Vector3i) = {
		if (Game.instance.networkManager.isServer) {
			if (isPoweredByRedstone) {
				powerOn()
			}
			else {
				powerOff()
			}
		}
	}

	def powerOn() {
		this.setActive(true)
	}

	def powerOff() {
		if (!this.isRedstoneActive && Game.instance.networkManager.isServer) {
			this.setActive(false)
		}
	}

	def isPoweredByRedstone: Boolean = false

	def isActive: Boolean = active

	override def onRightClick(entity: Entity, side: Int, hit: Vector3d): Boolean = {
		if (Placeholder.isHoldingConfigurator(entity)) {
			if (Placeholder.isKeyDown(Key.KEY_LSHIFT)) {
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
	 * ComputerCraft

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
  }*/
}
