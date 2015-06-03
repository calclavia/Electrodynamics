package com.calclavia.edx.electric.api;

import com.google.common.collect.Sets;
import nova.core.block.Block;

import java.util.Set;
import java.util.function.Supplier;

/**
 * An electric node that acts as a component in an electric circuit.
 * Constructor requirement: Provider (An instance of {@link Block}
 * @author Calclavia
 */
public abstract class ElectricComponent extends Electric {

	public ElectricComponent() {
		connections = () -> Sets.union(positives(), negatives());
	}

	/**
	 * Asks the component to generate a potential difference.
	 * @param voltage The voltage in volts.
	 */
	public abstract void generateVoltage(double voltage);

	/**
	 * Asks the component to generate current.
	 * @param current The current in amps.
	 */
	public abstract void generateCurrent(double current);

	/**
	 * @return The positive connections
	 */
	public abstract Set<Electric> positives();

	/**
	 * @return The negative connections
	 */
	public abstract Set<Electric> negatives();

	/**
	 * Sets the positive connection supplier
	 * @param supplier The function to determine to connections
	 */
	public abstract void setPositiveConnections(Supplier<Set<Electric>> supplier);

	/**
	 * Sets the negative connection supplier
	 * @param supplier The function to determine to connections
	 */
	public abstract void setNegativeConnections(Supplier<Set<Electric>> supplier);
}
