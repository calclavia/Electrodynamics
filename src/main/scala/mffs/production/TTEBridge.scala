package mffs.production

import cofh.api.energy.IEnergyHandler
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.content.prefab.TElectric
import resonant.lib.grid.energy.EnergyStorage
import resonant.lib.mod.compat.energy.Compatibility

/**
 * A temporary energy bridge between TE and UE
 * @author Calclavia
 */
trait TTEBridge extends TElectric with IEnergyHandler
{
  val energyStorage = new EnergyStorage(10000)

  override def receiveEnergy(from: ForgeDirection, maxReceive: Int, simulate: Boolean): Int =
  {
    return (energyStorage.receiveEnergy(maxReceive / Compatibility.redstoneFluxRatio, !simulate) * Compatibility.redstoneFluxRatio).asInstanceOf[Int]
  }

  override def extractEnergy(from: ForgeDirection, maxExtract: Int, simulate: Boolean): Int =
  {
    return (energyStorage.receiveEnergy(maxExtract / Compatibility.redstoneFluxRatio, !simulate) * Compatibility.redstoneFluxRatio).asInstanceOf[Int]
  }

  override def getEnergyStored(from: ForgeDirection): Int =
  {
    return (energyStorage.getEnergy / Compatibility.redstoneFluxRatio).asInstanceOf[Int]
  }

  override def getMaxEnergyStored(from: ForgeDirection): Int =
  {
    return (energyStorage.getEnergyCapacity / Compatibility.redstoneFluxRatio).asInstanceOf[Int]
  }

  override def canConnectEnergy(from: ForgeDirection): Boolean =
  {
    return dcNode.canConnect(from)
  }
}
