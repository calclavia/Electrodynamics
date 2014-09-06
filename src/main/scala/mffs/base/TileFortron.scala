package mffs.base

import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import mffs.util.{FortronUtility, TransferMode}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.api.mffs.fortron.{FrequencyGridRegistry, IFortronFrequency}
import resonant.lib.network.ByteBufWrapper.ByteBufWrapper
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.netty.PacketManager
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAll._

/**
 * A TileEntity that is powered by FortronHelper.
 *
 * @author Calclavia
 */
abstract class TileFortron extends TileFrequency with IFluidHandler with IFortronFrequency
{
	var markSendFortron = true
	protected var fortronTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME)

	override def update()
	{
		super.update()

		if (!worldObj.isRemote && ticks % 60 == 0)
		{
			sendFortronToClients
		}
	}

	override def invalidate()
	{
		if (this.markSendFortron)
		{
			FortronUtility.transferFortron(this, FrequencyGridRegistry.instance.getNodes(classOf[IFortronFrequency], worldObj, new Vector3(this), 100, this.getFrequency), TransferMode.drain, Integer.MAX_VALUE)
		}

		super.invalidate()
	}

	/**
	 * Packets
	 */
	override def write(buf: ByteBuf, id: Int)
	{
		super.write(buf, id)

		if (id == TilePacketType.fortron.id)
		{
			buf <<< fortronTank
		}
	}

	override def read(buf: ByteBuf, id: Int, player: EntityPlayer, packet: PacketType): Boolean =
	{
		super.read(buf, id, player, packet)

		if (id == TilePacketType.fortron.id)
		{
			fortronTank = buf.readTank()
		}


		return false
	}

	def sendFortronToClients
	{
		ModularForceFieldSystem.packetHandler.sendToAllAround(PacketManager.request(this, TilePacketType.fortron.id), this.worldObj, new Vector3(this), 25)
	}

	/**
	 * NBT Methods
	 */
	override def readFromNBT(nbt: NBTTagCompound)
	{
		super.readFromNBT(nbt)
		fortronTank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fortron")))
	}

	override def writeToNBT(nbt: NBTTagCompound)
	{
		super.writeToNBT(nbt)

		if (fortronTank.getFluid != null)
		{
			nbt.setTag("fortron", this.fortronTank.getFluid.writeToNBT(new NBTTagCompound))
		}
	}

	/**
	 * Fluid Functions.
	 */
	override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
	{
		if (resource.isFluidEqual(FortronUtility.FLUIDSTACK_FORTRON))
		{
			return this.fortronTank.fill(resource, doFill)
		}
		return 0
	}

	override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
	{
		if (resource == null || !resource.isFluidEqual(fortronTank.getFluid))
		{
			return null
		}
		return fortronTank.drain(resource.amount, doDrain)
	}

	override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
	{
		return fortronTank.drain(maxDrain, doDrain)
	}

	override def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
	{
		return true
	}

	override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
	{
		return true
	}

	override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
	{
		return Array[FluidTankInfo](this.fortronTank.getInfo)
	}

	override def getFortronEnergy: Int =
	{
		return FortronUtility.getAmount(this.fortronTank)
	}

	override def setFortronEnergy(energy: Int)
	{
		this.fortronTank.setFluid(FortronUtility.getFortron(energy))
	}

	override def getFortronCapacity: Int =
	{
		return this.fortronTank.getCapacity
	}

	override def requestFortron(energy: Int, doUse: Boolean): Int =
	{
		return FortronUtility.getAmount(this.fortronTank.drain(energy, doUse))
	}

	override def provideFortron(energy: Int, doUse: Boolean): Int =
	{
		return this.fortronTank.fill(FortronUtility.getFortron(energy), doUse)
	}

	/**
	 * Gets the amount of empty space this tank has.
	 */
	def getFortronEmpty = fortronTank.getCapacity - fortronTank.getFluidAmount
}