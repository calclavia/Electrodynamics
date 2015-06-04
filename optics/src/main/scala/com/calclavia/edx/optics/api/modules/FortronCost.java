package com.calclavia.edx.optics.api.modules;

/**
 * @author Calclavia
 */
public interface FortronCost {
	/**
	 * The amount of Fortron this module consumes per tick.
	 * @return
	 */
	public float getFortronCost(float amplifier);
}
