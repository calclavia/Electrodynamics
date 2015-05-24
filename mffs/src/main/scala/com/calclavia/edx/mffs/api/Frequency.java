package com.calclavia.edx.mffs.api;

/**
 * Applied to all blocks that has a frequency.
 * @author Calclavia
 */

public interface Frequency {
	/**
	 * @return The frequency of this object.
	 */
	public int getFrequency();

	/**
	 * Sets the frequency
	 * @param frequency - The frequency of this object.
	 */
	public void setFrequency(int frequency);
}
