package com.calclavia.edx.electrical.circuit.component.laser;

import net.minecraft.util.MovingObjectPosition;
import resonantengine.lib.transform.vector.Vector3;

/**
 * @author Calclavia
 */
public interface ILaserHandler
{
	public boolean onLaserHit(Vector3 renderStart, Vector3 incident, MovingObjectPosition hit, Vector3 color, double energy);
}
