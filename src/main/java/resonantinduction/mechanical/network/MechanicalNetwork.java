package resonantinduction.mechanical.network;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.net.IConnector;
import universalelectricity.core.net.ConnectionPathfinder;
import universalelectricity.core.net.Network;
import universalelectricity.core.net.NetworkTickHandler;

/**
 * A mechanical network for translate speed and force using mechanical rotations.
 * 
 * Useful Formula:
 * 
 * Power is the work per unit time.
 * Power (W) = Torque (Strength of the rotation, Newton Meters) x Speed (Angular Velocity, RADIAN
 * PER SECOND).
 * *OR*
 * Power = Torque / Time
 * 
 * Torque = r (Radius) * F (Force) * sin0 (Direction/Angle of the force applied. 90 degrees if
 * optimal.)
 * 
 * @author Calclavia
 */
public class MechanicalNetwork extends Network<IMechanicalNetwork, IMechanicalConnector, IMechanical> implements IMechanicalNetwork
{
	private long prevTorque = 0;
	private float prevAngularVelocity = 0;

	private long torque = 0;
	private float angularVelocity = 0;

	/** The cached resistance caused by all connectors */
	private float connectorResistance = 0;

	/** The current rotation of the network */
	private float rotation = 0;
	private long lastRotateTime;
	private boolean markPacketUpdate = true;

	/** The direction in which a conductor is placed relative to a specific conductor. */
	protected final HashMap<Object, EnumSet<ForgeDirection>> handlerDirectionMap = new LinkedHashMap<Object, EnumSet<ForgeDirection>>();

	@Override
	public void addConnector(IMechanicalConnector connector)
	{
		this.markPacketUpdate = true;
		super.addConnector(connector);
	}

	/**
	 * An network update called only server side.
	 */
	@Override
	public void update()
	{
		/**
		 * Calculate load
		 */
		if (getPower() > 0)
		{
			float division = connectorResistance;

			for (IMechanical node : this.getNodes())
			{
				for (ForgeDirection dir : handlerDirectionMap.get(node))
				{
					division += node.onReceiveEnergy(dir, torque, angularVelocity, false) / torque;
				}
			}

			if (division > 0)
			{
				torque /= division / 2;
				angularVelocity /= division / 2;
			}
		}

		/**
		 * Update all connectors
		 */
		if (markPacketUpdate || getPrevTorque() != getTorque() || getPrevAngularVelocity() != getAngularVelocity())
		{
			/**
			 * Send network update packet for connectors.
			 */
			for (IMechanicalConnector connector : this.getConnectors())
			{
				if (connector.sendNetworkPacket(torque, angularVelocity))
				{
					break;
				}
			}
		}

		/**
		 * Distribute energy to handlers
		 */
		if (getPower() > 0)
		{
			for (IMechanical node : this.getNodes())
			{
				for (ForgeDirection dir : handlerDirectionMap.get(node))
				{
					node.onReceiveEnergy(dir, torque, angularVelocity, true);
				}
			}
		}

		prevTorque = torque;
		prevAngularVelocity = angularVelocity;
		torque = 0;
		angularVelocity = 0;
	}

	/**
	 * Applies energy to the mechanical network this tick.
	 * Note: Server side only.
	 */
	@Override
	public long onReceiveEnergy(long torque, float angularVelocity)
	{
		this.torque += torque;
		this.angularVelocity += angularVelocity;
		NetworkTickHandler.addNetwork(this);
		return (long) (torque * angularVelocity);
	}

	@Override
	public long getPrevTorque()
	{
		return prevTorque;
	}

	@Override
	public float getPrevAngularVelocity()
	{
		return prevAngularVelocity;
	}

	@Override
	public long getTorque()
	{
		return torque;
	}

	@Override
	public float getAngularVelocity()
	{
		return angularVelocity;
	}

	@Override
	public long getPower()
	{
		return (long) (getTorque() * getAngularVelocity());
	}

	@Override
	public void setPower(long torque, float angularVelocity)
	{
		prevTorque = this.torque;
		prevAngularVelocity = this.angularVelocity;
		this.torque = torque;
		this.angularVelocity = angularVelocity;
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public boolean continueUpdate()
	{
		return true;
	}

	@Override
	public void reconstruct()
	{
		// Reset
		prevTorque = torque = 0;
		prevAngularVelocity = angularVelocity = 0;
		connectorResistance = 0;

		if (this.getConnectors().size() > 0)
		{
			// Reset all values related to wires
			this.getNodes().clear();

			// Iterate threw list of wires
			Iterator<IMechanicalConnector> it = this.getConnectors().iterator();

			while (it.hasNext())
			{
				IMechanicalConnector connector = it.next();

				if (connector != null)
				{
					reconstructConnector(connector);
				}
				else
				{
					it.remove();
				}
			}
		}
	}

	/** Segmented out call so overriding can be done when conductors are reconstructed. */
	protected void reconstructConnector(IMechanicalConnector connector)
	{
		connector.setNetwork(this);

		for (int i = 0; i < connector.getConnections().length; i++)
		{
			reconstructHandler(connector.getConnections()[i], ForgeDirection.getOrientation(i).getOpposite());
		}

		connectorResistance += connector.getResistance();
	}

	/** Segmented out call so overriding can be done when machines are reconstructed. */
	protected void reconstructHandler(Object obj, ForgeDirection side)
	{
		if (obj != null && !(obj instanceof IMechanicalConnector))
		{
			if (obj instanceof IMechanical)
			{
				EnumSet<ForgeDirection> set = this.handlerDirectionMap.get(obj);
				if (set == null)
				{
					set = EnumSet.noneOf(ForgeDirection.class);
				}
				this.getNodes().add((IMechanical) obj);
				set.add(side);
				this.handlerDirectionMap.put(obj, set);
			}
		}
	}

	@Override
	public IMechanicalNetwork merge(IMechanicalNetwork network)
	{
		if (network.getClass().isAssignableFrom(this.getClass()) && network != this)
		{
			MechanicalNetwork newNetwork = new MechanicalNetwork();
			newNetwork.getConnectors().addAll(this.getConnectors());
			newNetwork.getConnectors().addAll(network.getConnectors());
			network.getConnectors().clear();
			network.getNodes().clear();
			this.getConnectors().clear();
			this.getNodes().clear();

			newNetwork.reconstruct();
			return newNetwork;
		}

		return null;
	}

	@Override
	public void split(IMechanicalConnector splitPoint)
	{
		this.removeConnector(splitPoint);
		this.reconstruct();

		/**
		 * Loop through the connected blocks and attempt to see if there are connections between the
		 * two points elsewhere.
		 */
		Object[] connectedBlocks = splitPoint.getConnections();

		for (int i = 0; i < connectedBlocks.length; i++)
		{
			Object connectedBlockA = connectedBlocks[i];

			if (connectedBlockA instanceof IMechanicalConnector)
			{
				for (int ii = 0; ii < connectedBlocks.length; ii++)
				{
					final Object connectedBlockB = connectedBlocks[ii];

					if (connectedBlockA != connectedBlockB && connectedBlockB instanceof IMechanicalConnector)
					{
						ConnectionPathfinder finder = new ConnectionPathfinder((IConnector) connectedBlockB, splitPoint);
						finder.findNodes((IConnector) connectedBlockA);

						if (finder.results.size() <= 0)
						{
							try
							{
								/**
								 * The connections A and B are not connected anymore. Give them both
								 * a new common network.
								 */
								IMechanicalNetwork newNetwork = new MechanicalNetwork();
								for (IConnector node : finder.closedSet)
								{
									if (node != splitPoint && node instanceof IMechanicalConnector)
									{
										newNetwork.addConnector((IMechanicalConnector) node);
									}
								}
								newNetwork.reconstruct();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}

						}
					}
				}
			}
		}
	}

	@Override
	public void split(IMechanicalConnector connectorA, IMechanicalConnector connectorB)
	{
		this.reconstruct();

		/** Check if connectorA connects with connectorB. */
		ConnectionPathfinder finder = new ConnectionPathfinder(connectorB);
		finder.findNodes(connectorA);

		if (finder.results.size() <= 0)
		{
			/**
			 * The connections A and B are not connected anymore. Give them both a new common
			 * network.
			 */
			IMechanicalNetwork newNetwork = new MechanicalNetwork();

			for (IConnector node : finder.closedSet)
			{
				if (node instanceof IMechanicalConnector)
				{
					newNetwork.addConnector((IMechanicalConnector) node);
				}
			}

			newNetwork.reconstruct();
		}
	}

	@Override
	public float getRotation()
	{
		long deltaTime = System.currentTimeMillis() - lastRotateTime;

		if (deltaTime > 1)
		{
			rotation = (float) (((angularVelocity) * (deltaTime / 1000f) + rotation) % Math.PI);
			lastRotateTime = System.currentTimeMillis();
		}

		return rotation;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "[" + this.hashCode() + ", Handlers: " + getNodes().size() + ", Connectors: " + getConnectors().size() + ", Power:" + getPower() + "]";
	}

}
