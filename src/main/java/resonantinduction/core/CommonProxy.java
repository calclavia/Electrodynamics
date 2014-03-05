/**
 * 
 */
package resonantinduction.core;

import resonantinduction.core.grid.ThreadedGridTicker;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.ProxyBase;

/**
 * @author Calclavia
 * 
 */
public class CommonProxy extends ProxyBase
{
	@Override
	public void postInit()
	{
		ThreadedGridTicker.INSTANCE.start();
	}

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
