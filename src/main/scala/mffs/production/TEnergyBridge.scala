package mffs.production

import cofh.api.energy.IEnergyHandler
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.content.prefab.TElectric
import universalelectricity.compatibility.module.ModuleThermalExpansion

/**
 * A temporary energy bridge between TE and UE
 * @author Calclavia
 */
trait TEnergyBridge extends TElectric with IEnergyHandler
{
  override def receiveEnergy(from: ForgeDirection, maxReceive: Int, simulate: Boolean): Int =
  {
    return (electricNode.addEnergy(from, maxReceive * ModuleThermalExpansion.reciprocal_ratio, !simulate) * ModuleThermalExpansion.ratio).asInstanceOf[Int]
  }

  override def extractEnergy(from: ForgeDirection, maxExtract: Int, simulate: Boolean): Int =
  {
    return (electricNode.removeEnergy(from, maxExtract * ModuleThermalExpansion.reciprocal_ratio, !simulate) * ModuleThermalExpansion.ratio).asInstanceOf[Int]
  }

  override def getEnergyStored(from: ForgeDirection): Int =
  {
    return (electricNode.buffer().getEnergy * ModuleThermalExpansion.ratio).asInstanceOf[Int]
  }

  override def getMaxEnergyStored(from: ForgeDirection): Int =
  {
    return (electricNode.buffer().getEnergyCapacity * ModuleThermalExpansion.ratio).asInstanceOf[Int]
  }

  override def canConnectEnergy(from: ForgeDirection): Boolean =
  {
    return electricNode.canConnect(from)
  }
}
