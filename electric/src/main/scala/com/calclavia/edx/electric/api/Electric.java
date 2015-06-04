package com.calclavia.edx.electric.api;

import nova.core.block.component.Connectable;
import nova.core.event.EventBus;

import java.util.Set;

/**
 * An abstract interface extended by NodeElectricComponent and NodeElectricJunction.
 * This interface is NOT registered.
 * @author Calclavia
 */
public abstract class Electric extends Connectable<Electric> {

	/**
	 * Called when the voltage changes
	 */
	public final EventBus<ElectricChangeEvent> onVoltageChange = new EventBus<>();
	/**
	 * Called when the current changes
	 */
	public final EventBus<ElectricChangeEvent> onCurrentChange = new EventBus<>();
	/**
	 * Called when the resistance changes
	 */
	public final EventBus<ElectricChangeEvent> onResistanceChange = new EventBus<>();
	/**
	 * Called when the electric grid finished building all connections.
	 */
	public final EventBus<GraphBuiltEvent> onGridBuilt = new EventBus<>();

	/**
	 * Sets the resistance.
	 * @param resistance Resistance in ohms
	 */
	public abstract Electric setResistance(double resistance);

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

	public static class ElectricChangeEvent {

	}

	public static class GraphBuiltEvent{

		/**
		 * The set of connections this node has actually connected to.
		 * In order for a connection to occur, the connection has to be mutual (A connects to B and B connects to A)
		 */
		public final Set<Electric> connections;

		public GraphBuiltEvent(Set<Electric> connections) {
			this.connections = connections;
		}
	}
}
