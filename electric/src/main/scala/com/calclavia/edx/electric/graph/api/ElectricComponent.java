package com.calclavia.edx.electric.graph.api;

import nova.core.block.Block;

import java.util.Set;
import java.util.function.Supplier;

/**
 * An electric node that acts as a component in an electric circuit.
 * Constructor requirement: Provider (An instance of {@link Block}
 * @author Calclavia
 */
public abstract class ElectricComponent extends Electric {

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
