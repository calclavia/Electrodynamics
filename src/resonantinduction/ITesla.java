/**
 * 
 */
package resonantinduction;

import net.minecraft.tileentity.TileEntity;

/**
 * @author Calclavia
 * 
 */
public interface ITesla
{
	public void transfer(float transferEnergy);

	public boolean canReceive(TileEntity tileEntity);

}
