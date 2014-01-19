/**
 * 
 */
package resonantinduction.core;

import net.minecraft.world.World;
import resonantinduction.core.prefab.ProxyBase;
import universalelectricity.api.vector.Vector3;

/**
 * @author Calclavia
 * 
 */
public class CommonProxy extends ProxyBase
{
	public boolean isPaused()
	{
		return false;
	}

	public boolean isGraphicsFancy()
	{
		return false;
	}

	public void renderBlockParticle(World world, Vector3 position, Vector3 velocity, int blockID, float scale)
	{

	}

}
