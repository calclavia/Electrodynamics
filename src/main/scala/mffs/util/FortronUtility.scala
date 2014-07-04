package mffs.util

import mffs.{ModularForceFieldSystem, Settings}
import mffs.util.TransferMode._
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTank}
import resonant.api.mffs.fortron.IFortronFrequency
import resonant.api.mffs.modules.IModuleAcceptor
import universalelectricity.core.transform.vector.Vector3

/**
 * A class with useful functions related to Fortron.
 *
 * @author Calclavia
 */
object FortronUtility
{
  var FLUID_FORTRON: Fluid = null
  var FLUIDSTACK_FORTRON: FluidStack = null

  def getFortron(amount: Int): FluidStack =
  {
    val stack: FluidStack = new FluidStack(FLUID_FORTRON, amount)
    return stack
  }

  def getAmount(liquidStack: FluidStack): Int =
  {
    if (liquidStack != null)
    {
      return liquidStack.amount
    }
    return 0
  }

  def getAmount(fortronTank: FluidTank): Int =
  {
    if (fortronTank != null)
    {
      return getAmount(fortronTank.getFluid)
    }
    return 0
  }

  def transferFortron(source: IFortronFrequency, frequencyTiles: Set[IFortronFrequency], transferMode: TransferMode, limit: Int)
  {
    if (source != null && frequencyTiles.size > 1 && Settings.allowFortronTeleport)
    {
      var totalFortron: Int = 0
      var totalCapacity: Int = 0

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
          case TransferMode.EQUALIZE =>
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
          case TransferMode.DISTRIBUTE =>
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
          case TransferMode.DRAIN =>
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
          case TransferMode.FILL =>
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

      if (transferer.isInstanceOf[IModuleAcceptor])
      {
        isCamo = (transferer.asInstanceOf[IModuleAcceptor]).getModuleCount(ModularForceFieldSystem.Items.moduleCamouflage) > 0
      }

      if (joules > 0)
      {
        val transferEnergy = Math.min(joules, limit)
        var toBeInjected: Int = receiver.provideFortron(transferer.requestFortron(transferEnergy, false), false)
        toBeInjected = transferer.requestFortron(receiver.provideFortron(toBeInjected, true), true)
        if (world.isRemote && toBeInjected > 0 && !isCamo)
        {
          ModularForceFieldSystem.proxy.renderBeam(world, new Vector3(tileEntity) + 0.5, new Vector3(receiver.asInstanceOf[TileEntity]) + 0.5, 0.6f, 0.6f, 1, 20)
        }
      }
      else
      {
        val transferEnergy = Math.min(Math.abs(joules), limit)
        var toBeEjected: Int = transferer.provideFortron(receiver.requestFortron(transferEnergy, false), false)
        toBeEjected = receiver.requestFortron(transferer.provideFortron(toBeEjected, true), true)
        if (world.isRemote && toBeEjected > 0 && !isCamo)
        {
          ModularForceFieldSystem.proxy.renderBeam(world, new Vector3(receiver.asInstanceOf[TileEntity]) + 0.5, new Vector3(tileEntity) + 0.5, 0.6f, 0.6f, 1, 20)
        }
      }
    }
  }
}
