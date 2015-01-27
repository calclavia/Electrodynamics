/**
 *
 */
package edx.electrical.circuit.component.tesla

import net.minecraft.tileentity.TileEntity

/**
 * @author Calclavia
 *
 */
abstract trait ITesla
{
  /**
   * @param transferEnergy - The energy amount in kilojoules.
   * @param doTransfer - Actually transfer
   * @return Energy actually transfered.
   */
  def teslaTransfer(transferEnergy: Double, doTransfer: Boolean): Double

  def canTeslaTransfer(transferTile: TileEntity): Boolean
}