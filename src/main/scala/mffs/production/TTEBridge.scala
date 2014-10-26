package mffs.production

import cofh.api.energy.IEnergyHandler
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.content.prefab.TElectric
import universalelectricity.api.EnergyStorage
import universalelectricity.compatibility.module.ModuleThermalExpansion

/**
 * A temporary energy bridge between TE and UE
 * @author Calclavia
 */
trait TTEBridge extends TElectric with IEnergyHandler
{
  val energyStorage = new EnergyStorage(10000)

  override def receiveEnergy(from: ForgeDirection, maxReceive: Int, simulate: Boolean): Int =
  {
    return (energyStorage.receiveEnergy(maxReceive * ModuleThermalExpansion.reciprocal_ratio, !simulate) * ModuleThermalExpansion.ratio).asInstanceOf[Int]
  }

  override def extractEnergy(from: ForgeDirection, maxExtract: Int, simulate: Boolean): Int =
  {
    return (energyStorage.receiveEnergy(maxExtract * ModuleThermalExpansion.reciprocal_ratio, !simulate) * ModuleThermalExpansion.ratio).asInstanceOf[Int]
  }

  override def getEnergyStored(from: ForgeDirection): Int =
  {
    return (energyStorage.getEnergy * ModuleThermalExpansion.ratio).asInstanceOf[Int]
  }

  override def getMaxEnergyStored(from: ForgeDirection): Int =
  {
    return (energyStorage.getEnergyCapacity * ModuleThermalExpansion.ratio).asInstanceOf[Int]
  }

  override def canConnectEnergy(from: ForgeDirection): Boolean =
  {
    return dcNode.canConnect(from)
  }
}
