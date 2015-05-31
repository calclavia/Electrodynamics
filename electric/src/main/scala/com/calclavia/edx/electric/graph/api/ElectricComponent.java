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

	void setPositiveConnections(Supplier<Set<Electric>> supplier);

	void setNegativeConnections(Supplier<Set<Electric>> supplier);
}
