/**
 * 
 */
package resonantinduction.electrical.tesla;

import net.minecraft.tileentity.TileEntity;

/**
 * @author Calclavia
 * 
 */
public interface ITesla
{
	/**
	 * @param transferEnergy - The energy amount in kilojoules.
	 * @param doTransfer - Actually transfer
	 * @return Energy actually transfered.
	 */
	public double teslaTransfer(double transferEnergy, boolean doTransfer);

	public boolean canTeslaTransfer(TileEntity transferTile);

}
