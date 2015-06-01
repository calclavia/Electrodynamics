package com.calclavia.edx.electric.graph.api;

import nova.core.block.component.Connectable;

/**
 * An abstract interface extended by NodeElectricComponent and NodeElectricJunction.
 * This interface is NOT registered.
 * @author Calclavia
 */
public abstract class Electric extends Connectable<Electric> {

	/**
	 * @return The resistance of the electric component in ohms.
	 */
	public abstract double resistance();

	/**
	 * @return The voltage (potential difference) of the component in volts.
	 */
	public abstract double voltage();

	/**
	 * @return The current of the component in amperes.
	 */
	public abstract double current();

	/**
	 * @return The power dissipated in the component.
	 */
	public double power() {
		return current() * voltage();
	}
}
