package resonantinduction.mechanical.network;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.net.IUpdate;
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
public class MechanicalNetwork extends Network<IMechanicalNetwork, IMechanical> implements IMechanicalNetwork, IUpdate
{
	private long prevTorque = 0;
	private float prevAngularVelocity = 0;

	private long torque = 0;
	private float angularVelocity = 0;

	/** The cached resistance caused by all connectors */
	private float load = 0;

	/** The current rotation of the network */
	private float rotation = 0;
	private long lastRotateTime;
	private boolean markPacketUpdate = true;

	/** The direction in which a conductor is placed relative to a specific conductor. */
	protected final HashMap<Object, EnumSet<ForgeDirection>> handlerDirectionMap = new LinkedHashMap<Object, EnumSet<ForgeDirection>>();

	private Set<IMechanical> prevGenerators = new LinkedHashSet<IMechanical>();
	private Set<IMechanical> generators = new LinkedHashSet<IMechanical>();

	@Override
	public void addConnector(IMechanical connector)
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
		 * Calculation rotations of all generators.
		 */
		prevGenerators = generators;

		Set<IMechanical> closedSet = new LinkedHashSet<IMechanical>();

		for (IMechanical generatorNode : generators)
		{
			PathfinderRotationManager rotationPathfinder = new PathfinderRotationManager(generatorNode, closedSet);
			rotationPathfinder.findNodes(generatorNode);
			closedSet.addAll(rotationPathfinder.closedSet);
		}

		generators.clear();

		/**
		 * Calculate load
		 */
		if (load > 0)
		{
			torque /= load / 2;
			angularVelocity /= load / 2;
		}

		/**
		 * Update all connectors
		 */
		if (markPacketUpdate || getPrevTorque() != getTorque() || getPrevAngularVelocity() != getAngularVelocity())
		{
			/**
			 * Send network update packet for connectors.
			 */
			for (IMechanical connector : this.getConnectors())
			{
				if (connector.sendNetworkPacket(torque, angularVelocity))
				{
					break;
				}
			}
		}

		prevTorque = torque;
		prevAngularVelocity = angularVelocity;
		torque *= 0.5;
		angularVelocity *= 0.5;
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public boolean continueUpdate()
	{
		return canUpdate();
	}

	/**
	 * Applies energy to the mechanical network this tick.
	 * Note: Server side only.
	 */
	@Override
	public long onReceiveEnergy(IMechanical source, long torque, float angularVelocity)
	{
		this.torque += torque;
		this.angularVelocity += angularVelocity;
		this.generators.add(source);
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
	public void reconstruct()
	{
		// Reset
		prevTorque = torque = 0;
		prevAngularVelocity = angularVelocity = 0;
		load = 0;

		super.reconstruct();
	}

	@Override
	protected void reconstructConnector(IMechanical connector)
	{
		connector.setNetwork(this);
		load += connector.getResistance();
	}

	/** Segmented out call so overriding can be done when machines are reconstructed. */
	protected void reconstructHandler(Object obj, ForgeDirection side)
	{
		if (obj != null && !(obj instanceof IMechanical))
		{
			if (obj instanceof IMechanical)
			{
				EnumSet<ForgeDirection> set = this.handlerDirectionMap.get(obj);
				if (set == null)
				{
					set = EnumSet.noneOf(ForgeDirection.class);
				}
				this.getConnectors().add((IMechanical) obj);
				set.add(side);
				this.handlerDirectionMap.put(obj, set);
			}
		}
	}

	@Override
	public float getRotation()
	{
		long deltaTime = System.currentTimeMillis() - lastRotateTime;

		if (deltaTime > 1)
		{
			rotation = (float) (((angularVelocity) * (deltaTime / 1000f) + rotation) % (2 * Math.PI));
			lastRotateTime = System.currentTimeMillis();
		}

		return rotation;
	}

	@Override
	public IMechanicalNetwork newInstance()
	{
		return new MechanicalNetwork();
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "[" + this.hashCode() + ", Handlers: " + getConnectors().size() + ", Connectors: " + getConnectors().size() + ", Power:" + getPower() + "]";
	}
}
