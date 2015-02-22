package mffs.base

import java.util.Optional

import com.resonant.wrapper.core.api.tile.IPlayerUsing
import mffs.api.machine.IActivatable
import mffs.item.card.ItemCardLink
import mffs.{Content, ModularForceFieldSystem}
import nova.core.block.Block
import nova.core.block.components.Stateful
import nova.core.network.{PacketReceiver, PacketSender}
import nova.core.render.texture.Texture
import nova.core.util.Direction
import nova.core.util.components.Storable

/**
 * A base block class for all MFFS blocks to inherit.
 * @author Calclavia
 */
abstract class BlockMFFS extends Block with PacketReceiver with PacketSender with IActivatable with IPlayerUsing with Stateful with Storable {
	/**
	 * Used for client side animations.
	 */
	var animation = 0f

	/**
	 * Is this machine switched on internally via GUI?
	 */
	var isRedstoneActive = false

	/**
	 * Is the machine active and working?
	 */
	private var active = false

	//	blockHardness = Float.MaxValue
	//	blockResistance = 100f
	//	stepSound = Block.soundTypeMetal

	override def getTexture(side: Direction): Optional[Texture] = Optional.of(Content.machineTexture)

	override def isOpaqueCube: Boolean = false

	override def update() {
		super.update()

		if (!world.isRemote && ticks % 3 == 0 && playersUsing.size > 0) {
			playersUsing foreach (player => ModularForceFieldSystem.packetHandler.sendToPlayer(getDescPacket, player.asInstanceOf[EntityPlayerMP]))
		}
	}

	//	override def getExplosionResistance(entity: Entity): Float = 100
	11111

	override def getDescPacket: PacketType = PacketManager.request(this, TilePacketType.description.id)

	override def getDescriptionPacket: Packet = {
		return ModularForceFieldSystem.packetHandler.toMCPacket(getDescPacket)
	}

	override def read(buf: Packet, id: Int, packetType: PacketType) {
		if (id == TilePacketType.description.id) {
			val prevActive = active
			active = buf.readBoolean()
			isRedstoneActive = buf.readBoolean()

			if (prevActive != this.active) {
				markRender()
			}
		}
		else if (id == TilePacketType.toggleActivation.id) {
			isRedstoneActive = !isRedstoneActive

			if (isRedstoneActive) {
				setActive(true)
			}
			else {
				setActive(false)
			}
		}
	}

	//TODO: Implement redstone support
	/*
		override def onNeighborChanged(block: Block) {
			if (!world.isRemote) {
				if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
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
			if (!this.isRedstoneActive && !this.worldObj.isRemote) {
				this.setActive(false)
			}
		}
	*/
	def setActive(flag: Boolean) {
		active = flag
		world().markStaticRender(position())
	}

	override def write(buf: Packet, id: Int) {
		super.write(buf, id)

		if (id == TilePacketType.description.id) {
			buf <<< active
			buf <<< isRedstoneActive
		}
	}

	def isPoweredByRedstone: Boolean = world.isBlockIndirectlyGettingPowered(x, y, z)

	override def readFromNBT(nbt: NBTTagCompound) {
		super.readFromNBT(nbt)
		this.active = nbt.getBoolean("isActive")
		this.isRedstoneActive = nbt.getBoolean("isRedstoneActive")
	}

	override def writeToNBT(nbt: NBTTagCompound) {
		super.writeToNBT(nbt)
		nbt.setBoolean("isActive", this.active)
		nbt.setBoolean("isRedstoneActive", this.isRedstoneActive)
	}

	def isActive: Boolean = active

	override protected def use(player: EntityPlayer, side: Int, hit: Vector3d): Boolean = {
		if (!world.isRemote) {
			if (player.getCurrentEquippedItem != null) {
				if (player.getCurrentEquippedItem().getItem().isInstanceOf[ItemCardLink]) {
					return false
				}
			}

			player.openGui(ModularForceFieldSystem, 0, world, x, y, z)
		}
		return true
	}

	override protected def configure(player: EntityPlayer, side: Int, hit: Vector3d): Boolean = {
		if (player.isSneaking) {
			if (!world.isRemote) {
				InventoryUtility.dropBlockAsItem(world, position)
				world.setBlock(x, y, z, Blocks.air)
				return true
			}
			return false
		}

		if (this.isInstanceOf[TRotatable]) {
			return this.asInstanceOf[TRotatable].rotate(side, hit)
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
