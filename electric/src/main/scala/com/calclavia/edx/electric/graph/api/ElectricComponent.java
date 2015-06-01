package com.calclavia.edx.electric.graph.api;

import nova.core.block.Block;

import java.util.Set;
import java.util.function.Supplier;

/**
 * An electric node that acts as a component in an electric circuit.
 * Constructor requirement: Provider (An instance of {@link Block}
 * @author Calclavia
 */
public interface ElectricComponent extends Electric {

	/**
	 * @return The positive connections
	 */
	Set<Electric> positives();

	/**
	 * @return The negative connections
	 */
	Set<Electric> negatives();

	/**
	 * Sets the positive connection supplier
	 * @param supplier The function to determine to connections
	 */
	void setPositiveConnections(Supplier<Set<Electric>> supplier);

	/**
	 * Sets the negative connection supplier
	 * @param supplier The function to determine to connections
	 */
	void setNegativeConnections(Supplier<Set<Electric>> supplier);
}
