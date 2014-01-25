package resonantinduction.mechanical.network;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.gear.PartGear;
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
	public static final float ACCELERATION = 0.1f;
	/** The current rotation of the network */
	private float rotation = 0;
	private long lastRotateTime;

	/** The direction in which a conductor is placed relative to a specific conductor. */
	protected final HashMap<Object, EnumSet<ForgeDirection>> handlerDirectionMap = new LinkedHashMap<Object, EnumSet<ForgeDirection>>();

	private boolean markUpdateRotation = true;

	/**
	 * Only add the exact instance of the connector into the network. Multipart tiles allowed!
	 */
	@Override
	public void addConnector(IMechanical connector)
	{
		super.addConnector(connector);
		NetworkTickHandler.addNetwork(this);
		markUpdateRotation = true;
	}

	/**
	 * An network update called only server side.
	 */
	@Override
	public void update()
	{
		/**
		 * Update all mechanical nodes.
		 */
		Iterator<IMechanical> it = new LinkedHashSet<IMechanical>(getConnectors()).iterator();

		while (it.hasNext())
		{
			IMechanical mechanical = it.next();
			Object[] connections = mechanical.getConnections();

			for (int i = 0; i < connections.length; i++)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				Object adacent = connections[i];

				if (adacent instanceof IMechanical)
				{
					IMechanical adjacentMech = ((IMechanical) adacent).getInstance(dir);

					if (adjacentMech != null)
					{
						float ratio = adjacentMech.getRatio() / mechanical.getRatio();
						long torque = mechanical.getTorque();

						if (Math.abs(torque - adjacentMech.getTorque() / ratio * ACCELERATION) < Math.abs(adjacentMech.getTorque() / ratio))
						{
							mechanical.setTorque((long) (torque - (adjacentMech.getTorque() / ratio * ACCELERATION)));
						}

						float velocity = mechanical.getAngularVelocity();

						if (Math.abs(velocity - adjacentMech.getAngularVelocity() * ratio * ACCELERATION) < Math.abs(adjacentMech.getAngularVelocity() * ratio))
						{
							mechanical.setAngularVelocity(velocity - (adjacentMech.getAngularVelocity() * ratio * ACCELERATION));
						}
					}
				}
			}
		}
	}

	@Override
	public boolean canUpdate()
	{
		return getConnectors().size() > 0;
	}

	@Override
	public boolean continueUpdate()
	{
		return canUpdate();
	}

	@Override
	public void reconstruct()
	{
		super.reconstruct();
		NetworkTickHandler.addNetwork(this);
	}

	@Override
	protected void reconstructConnector(IMechanical connector)
	{
		connector.setNetwork(this);
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
	public float getRotation(float velocity)
	{
		long deltaTime = System.currentTimeMillis() - lastRotateTime;

		if (deltaTime > 1)
		{
			rotation = (float) (((velocity) * (deltaTime / 1000f) + rotation) % (2 * Math.PI));
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
	public Class getConnectorClass()
	{
		return IMechanical.class;
	}
}
