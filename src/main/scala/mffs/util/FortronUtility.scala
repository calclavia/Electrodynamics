package mffs.util

import mffs.render.FieldColor
import mffs.util.TransferMode._
import mffs.{Content, ModularForceFieldSystem, Settings}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTank}
import nova.core.util.transform.Vector3d
import resonantengine.api.mffs.fortron.IFortronFrequency
import resonantengine.api.mffs.modules.IModuleProvider
import resonantengine.nova.wrapper._

import scala.collection.mutable

/**
 * A class with useful functions related to Fortron.
 *
 * @author Calclavia
 */
object FortronUtility
{
  lazy val fluidFortron = new Fluid("fortron")
  lazy val fluidstackFortron = new FluidStack(FortronUtility.fluidFortron, 0)

  def getFortron(amount: Int): FluidStack =
  {
    val stack: FluidStack = new FluidStack(fluidFortron, amount)
    return stack
  }

  def getAmount(fortronTank: FluidTank): Int =
  {
    if (fortronTank != null)
    {
      return getAmount(fortronTank.getFluid)
    }
    return 0
  }

  def getAmount(liquidStack: FluidStack): Int =
  {
    if (liquidStack != null)
    {
      return liquidStack.amount
    }
    return 0
  }

  def transferFortron(source: IFortronFrequency, frequencyTiles: mutable.Set[IFortronFrequency], transferMode: TransferMode, limit: Int)
  {
    if (frequencyTiles.size > 1 && Settings.allowFortronTeleport)
    {
      var totalFortron = 0
      var totalCapacity = 0

      for (machine <- frequencyTiles)
      {
        if (machine != null)
        {
          totalFortron += machine.getFortronEnergy
          totalCapacity += machine.getFortronCapacity
        }
      }
      if (totalFortron > 0 && totalCapacity > 0)
      {
        transferMode match
        {
          case TransferMode.`equalize` =>
          {
            for (machine <- frequencyTiles)
            {
              if (machine != null)
              {
                val capacityPercentage: Double = machine.getFortronCapacity.asInstanceOf[Double] / totalCapacity.asInstanceOf[Double]
                val amountToSet: Int = (totalFortron * capacityPercentage).asInstanceOf[Int]
                doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy, limit)
              }
            }
          }
          case TransferMode.`distribute` =>
          {
            val amountToSet: Int = totalFortron / frequencyTiles.size
            for (machine <- frequencyTiles)
            {
              if (machine != null)
              {
                doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy, limit)
              }
            }
          }
          case TransferMode.`drain` =>
          {
            frequencyTiles.remove(source)

            for (machine <- frequencyTiles)
            {
              if (machine != null)
              {
                val capacityPercentage: Double = machine.getFortronCapacity.asInstanceOf[Double] / totalCapacity.asInstanceOf[Double]
                val amountToSet: Int = (totalFortron * capacityPercentage).asInstanceOf[Int]

                if (amountToSet - machine.getFortronEnergy > 0)
                {
                  doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy, limit)
                }
              }
            }
          }
          case TransferMode.`fill` =>
          {
            if (source.getFortronEnergy < source.getFortronCapacity)
            {
              frequencyTiles.remove(source)
              val requiredFortron: Int = source.getFortronCapacity - source.getFortronEnergy

              for (machine <- frequencyTiles)
              {
                if (machine != null)
                {
                  val amountToConsume: Int = Math.min(requiredFortron, machine.getFortronEnergy)
                  val amountToSet: Int = -machine.getFortronEnergy - amountToConsume
                  if (amountToConsume > 0)
                  {
                    doTransferFortron(source, machine, amountToSet - machine.getFortronEnergy, limit)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Tries to transfer Fortron to a specific machine from this capacitor. Renders an animation on
   * the client side.
   *
   * @param receiver : The machine to be transfered to.
   * @param joules   : The amount of energy to be transfered.
   */
  def doTransferFortron(transferer: IFortronFrequency, receiver: IFortronFrequency, joules: Int, limit: Int)
  {
    if (transferer != null && receiver != null)
    {
      val tileEntity = transferer.asInstanceOf[TileEntity]
      val world: World = tileEntity.getWorldObj()
      var isCamo = false

      if (transferer.isInstanceOf[IModuleProvider])
      {
        isCamo = (transferer.asInstanceOf[IModuleProvider]).getModuleCount(Content.moduleCamouflage) > 0
      }

      if (joules > 0)
      {
        val transferEnergy = Math.min(joules, limit)
        var toBeInjected: Int = receiver.provideFortron(transferer.requestFortron(transferEnergy, false), false)
        toBeInjected = transferer.requestFortron(receiver.provideFortron(toBeInjected, true), true)
        if (world.isRemote && toBeInjected > 0 && !isCamo)
        {
          ModularForceFieldSystem.proxy.renderBeam(world, new Vector3d(tileEntity) + 0.5, new Vector3d(receiver.asInstanceOf[TileEntity]) + 0.5, FieldColor.blue, 20)
        }
      }
      else
      {
        val transferEnergy = Math.min(Math.abs(joules), limit)
        var toBeEjected: Int = transferer.provideFortron(receiver.requestFortron(transferEnergy, false), false)
        toBeEjected = receiver.requestFortron(transferer.provideFortron(toBeEjected, true), true)
        if (world.isRemote && toBeEjected > 0 && !isCamo)
        {
          ModularForceFieldSystem.proxy.renderBeam(world, new Vector3d(receiver.asInstanceOf[TileEntity]) + 0.5, new Vector3d(tileEntity) + 0.5, FieldColor.blue, 20)
        }
      }
    }
  }
}
