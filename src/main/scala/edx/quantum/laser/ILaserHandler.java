package edx.quantum.laser;

import net.minecraft.util.MovingObjectPosition;
import resonant.lib.transform.vector.Vector3;

/**
 * @author Calclavia
 */
public interface ILaserHandler
{
	public boolean onLaserHit(Vector3 renderStart, Vector3 incident, MovingObjectPosition hit, Vector3 color, double energy);
}
