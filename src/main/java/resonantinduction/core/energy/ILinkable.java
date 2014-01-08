package resonantinduction.core.energy;

import net.minecraft.entity.player.EntityPlayer;
import universalelectricity.api.vector.VectorWorld;

/**
 * @author Calclavia
 * 
 */
public interface ILinkable
{
	/**
	 * 
	 * @param player
	 * @param vector
	 * @return True to clear the link.
	 */
	public boolean onLink(EntityPlayer player, VectorWorld vector);
}
