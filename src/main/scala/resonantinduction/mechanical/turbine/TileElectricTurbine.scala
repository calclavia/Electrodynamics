package resonantinduction.mechanical.turbine

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidTank, Fluid, FluidStack, FluidTankInfo}
import resonant.api.IBoilHandler
import resonant.lib.content.prefab.TElectric
import resonantinduction.core.{Reference, Settings}

class TileElectricTurbine extends TileTurbine with IBoilHandler with TElectric {

  var tank : FluidTank = new FluidTank(1000);
  maxPower = 5000000

  override def update {
    if (getMultiBlock.isConstructed) {
      mechanicalNode.torque = defaultTorque * 500 * getArea
    }
    else {
      mechanicalNode.torque = defaultTorque * 500
    }
    super.updateEntity
  }

  def onProduce {
    energy.receiveEnergy((power * Settings.turbineOutputMultiplier).asInstanceOf[Long], true)
  }

  override def playSound {
    if (this.worldObj.getWorldTime % 1200 == 0) {
      val maxVelocity: Double = (getMaxPower / mechanicalNode.torque) * 4
      val percentage: Float = Math.max(mechanicalNode.angularVelocity * 4 / maxVelocity.asInstanceOf[Float], 1.0f).asInstanceOf[Float]
      this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, Reference.prefix + "turbine", percentage, 1.0f)
    }
  }

  def canFill(from: ForgeDirection, fluid: Fluid): Boolean = {
    return from == ForgeDirection.DOWN && fluid.getName.contains("steam")
  }

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = tank.fill(resource, doFill)

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = null

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = null

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = {
    val re : Array[FluidTankInfo] = new Array[FluidTankInfo](1)
    re(1) = tank.getInfo
    return re
  }
}