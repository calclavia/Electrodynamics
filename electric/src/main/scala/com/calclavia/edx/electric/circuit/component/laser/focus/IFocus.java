package com.calclavia.edx.electrical.circuit.component.laser.focus;

import resonantengine.lib.transform.vector.Vector3d;

import java.util.List;

/**
 * Devices that can focus on specific angles
 *
 * @author Calclavia
 */
public interface IFocus
{
	/**
	 * Tells the block to look at a specific position
	 *
	 * @param position
	 */
	public void focus(Vector3d position);

	public Vector3d getFocus();

	public void setFocus(Vector3d focus);

	public List<Vector3d> getCacheDirections();
}
