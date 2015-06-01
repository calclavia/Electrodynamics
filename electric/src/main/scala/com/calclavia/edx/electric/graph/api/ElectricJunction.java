package com.calclavia.edx.electric.graph.api;

import nova.core.block.Block;

import java.util.Set;

/**
 * An electric node that acts as a component in an electric circuit.
 * Constructor requirement: Provider (An instance of {@link Block}
 * @author Calclavia
 */
public interface ElectricJunction extends Electric {
	Set<Electric> connections();
}
