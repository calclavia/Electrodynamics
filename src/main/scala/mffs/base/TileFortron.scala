package mffs.base

import java.util.Optional

import com.resonant.core.prefab.block.Updater
import mffs.ModularForceFieldSystem
import mffs.api.fortron.IFortronFrequency
import mffs.util.{FortronUtility, TransferMode}
import nova.core.fluid.{Fluid, Tank, TankProvider, TankSimple}
import nova.core.game.Game
import nova.core.network.Packet
import nova.core.util.Direction

/**
 * A TileEntity that is powered by FortronHelper.
 *
 * @author Calclavia
 */
abstract class TileFortron extends BlockFrequency with TankProvider with IFortronFrequency with Updater {
	var markSendFortron = true
	protected var fortronTank = new TankSimple(Fluid.bucketVolume)

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (Game.instance.networkManager.isServer && ticks % 60 == 0) {
			sendFortronToClients
		}
	}

	def sendFortronToClients {
		ModularForceFieldSystem.packetHandler.sendToAllAround(PacketManager.request(this, TilePacketType.fortron.id), this.worldObj, position, 25)
	}

	override def invalidate() {
		if (this.markSendFortron) {
			FortronUtility.transferFortron(this, FrequencyGridRegistry.instance.getNodes(classOf[IFortronFrequency], worldObj, position, 100, this.getFrequency), TransferMode.drain, Integer.MAX_VALUE)
		}

		super.invalidate()
	}

	override def write(id: Int, packet: Packet) {
		super.write(id, packet)
		if (id == TilePacketType.fortron.id) {
			packet <<< fortronTank
		}
	}

	override def read(id: Int, packet: Packet) {
		super.read(id, packet)

		if (id == TilePacketType.fortron.id) {
			fortronTank = packet.readTank()
		}
	}

	/**
	 * NBT Methods
	 */
	override def readFromNBT(nbt: NBTTagCompound) {
		super.readFromNBT(nbt)
		fortronTank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fortron")))
	}

	override def writeToNBT(nbt: NBTTagCompound) {
		super.writeToNBT(nbt)

		if (fortronTank.getFluid != null) {
			nbt.setTag("fortron", this.fortronTank.getFluid.writeToNBT(new NBTTagCompound))
		}
	}

	/**
	 * Fluid Functions.
	 */
	override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = {
		if (resource.isFluidEqual(FortronUtility.fluidstackFortron)) {
			return this.fortronTank.fill(resource, doFill)
		}
		return 0
	}

	override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = {
		if (resource == null || !resource.isFluidEqual(fortronTank.getFluid)) {
			return null
		}
		return fortronTank.drain(resource.amount, doDrain)
	}

	override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = {
		return fortronTank.drain(maxDrain, doDrain)
	}

	override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = {
		return true
	}

	override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = {
		return true
	}

	override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = {
		return Array[FluidTankInfo](this.fortronTank.getInfo)
	}

	override def getFortronEnergy: Int = {
		return FortronUtility.getAmount(this.fortronTank)
	}

	override def setFortronEnergy(energy: Int) {
		this.fortronTank.setFluid(FortronUtility.getFortron(energy))
	}

	override def getFortronCapacity: Int = {
		return this.fortronTank.getCapacity
	}

	override def requestFortron(energy: Int, doUse: Boolean): Int = {
		return FortronUtility.getAmount(this.fortronTank.drain(energy, doUse))
	}

	override def provideFortron(energy: Int, doUse: Boolean): Int = {
		return this.fortronTank.fill(FortronUtility.getFortron(energy), doUse)
	}

	/**
	 * Gets the amount of empty space this tank has.
	 */
	def getFortronEmpty = fortronTank.getCapacity - fortronTank.getFluidAmount

	override def getTank(dir: Direction): Optional[Tank] = fortronTank
}