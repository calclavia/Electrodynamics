package mffs.production

import java.util.{HashSet => JHashSet, Set => JSet}

import mffs.Content
import mffs.api.fortron.{Fortron, FortronCapacitor, FortronFrequency}
import mffs.base.{BlockModuleHandler, PacketBlock}
import mffs.item.card.ItemCardFrequency
import mffs.util.TransferMode.TransferMode
import mffs.util.{FortronUtility, TransferMode}
import net.minecraftforge.fluids.IFluidContainerItem
import nova.core.fluid.TankProvider
import nova.core.inventory.InventorySimple
import nova.core.network.Sync

import scala.collection.convert.wrapAll._
class BlockFortronCapacitor extends BlockModuleHandler with FortronCapacitor {

	override protected val inventory: InventorySimple = new InventorySimple(3 + 4 * 2 + 1)
	private val tickRate = 10

	capacityBase = 700
	capacityBoost = 10
	startModuleIndex = 1

	@Sync(ids = Array(PacketBlock.description.ordinal(), PacketBlock.toggleMode.ordinal()))
	private var transferMode = TransferMode.equalize

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		consumeCost()

		if (isActive) {
			/**
			 * Handle fortron item inputs
			 */
			getInputStacks
				.collect { case p: TankProvider if p.getTanks.exists(_.hasFluidType(Fortron)) => p}
				.foreach(stack => addFortron(stack.drain(stack, Math.min(getFortronEmpty, getTransmissionRate), true), true))

			/**
			 * Handle fortron item outputs
			 */
			if (fortronTank.getFluidAmount > 0) {
				val transferFluid = fortronTank.getFluid.copy()
				transferFluid.amount = Math.min(transferFluid.amount, getTransmissionRate)
				getOutputStacks filter (_.getItem.isInstanceOf[IFluidContainerItem]) foreach (stack => fortronTank.drain(stack.getItem.asInstanceOf[IFluidContainerItem].fill(stack, transferFluid, true), true))
			}

			if (ticks % tickRate == 0) {
				/**
				 * Transfer based on input/output slots
				 */
				FortronUtility.transferFortron(this, getInputDevices, TransferMode.fill, getTransmissionRate * tickRate)
				FortronUtility.transferFortron(this, getOutputDevices, TransferMode.drain, getTransmissionRate * tickRate)

				/**
				 * Transfer based on frequency
				 */
				FortronUtility.transferFortron(this, getFrequencyDevices, transferMode, getTransmissionRate * tickRate)
			}
		}
	}

	def getTransmissionRate: Int = 300 + 60 * getModuleCount(Content.moduleSpeed)

	override def getFrequencyDevices: JSet[FortronFrequency] = FrequencyGridRegistry.instance.getNodes(classOf[FortronFrequency], world, position, getTransmissionRange, getFrequency)

	def getTransmissionRange: Int = 15 + getModuleCount(Content.moduleScale)

	def getInputDevices: JSet[FortronFrequency] = getDevicesFromStacks(getInputStacks)

	def getDevicesFromStacks(stacks: Set[Item]): JSet[FortronFrequency] = {
		val devices = new JHashSet[FortronFrequency]()

		stacks
			.filter(_.getItem.isInstanceOf[ICoordLink])
			.map(Item => Item.getItem.asInstanceOf[ICoordLink].getLink(Item))
			.filter(linkPosition => linkPosition != null && linkPosition.getTileEntity(world).isInstanceOf[FortronFrequency])
			.foreach(linkPosition => devices.add(linkPosition.getTileEntity(world).asInstanceOf[FortronFrequency]))

		return devices
	}

	def getInputStacks: Set[Item] = ((4 to 7) map (i => getStackInSlot(i)) filter (_ != null)).toSet

	def getOutputDevices: JSet[FortronFrequency] = getDevicesFromStacks(getOutputStacks)

	def getOutputStacks: Set[Item] = ((8 to 11) map (i => getStackInSlot(i)) filter (_ != null)).toSet

	override def getAmplifier: Float = 0f

	override def readFromNBT(nbt: NBTTagCompound) {
		super.readFromNBT(nbt)
		this.transferMode = TransferMode(nbt.getInteger("transferMode"))
	}

	override def writeToNBT(nbttagcompound: NBTTagCompound) {
		super.writeToNBT(nbttagcompound)
		nbttagcompound.setInteger("transferMode", this.transferMode.id)
	}

	def getDeviceCount = getFrequencyDevices.size + getInputDevices.size + getOutputDevices.size

	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean = {
		if (slotID == 0) {
			return Item.getItem.isInstanceOf[ItemCardFrequency]
		}
		else if (slotID < 4) {
			return Item.getItem.isInstanceOf[IModule]
		}

		return true
	}

	def getTransferMode: TransferMode = transferMode

	@SideOnly(Side.CLIENT)
	override def renderStatic(renderer: RenderBlocks, pos: Vector3d, pass: Int): Boolean = {
		return false
	}

	@SideOnly(Side.CLIENT)
	override def renderDynamic(pos: Vector3d, frame: Float, pass: Int) {
		RenderFortronCapacitor.render(this, pos.x, pos.y, pos.z, frame, isActive, false)
	}

	@SideOnly(Side.CLIENT)
	override def renderInventory(Item: Item) {
		RenderFortronCapacitor.render(this, -0.5, -0.5, -0.5, 0, true, true)
	}
}