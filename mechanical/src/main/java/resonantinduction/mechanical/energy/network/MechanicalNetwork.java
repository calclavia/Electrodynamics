package resonantinduction.mechanical.energy.network;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.mechanical.IMechanical;
import resonantinduction.api.mechanical.IMechanicalNetwork;
import universalelectricity.api.net.IUpdate;
import universalelectricity.api.vector.Vector3;
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
	public MechanicalNetwork()
	{
		super(IMechanical.class);
	}

	public static final float ACCELERATION = 0.2f;

	/** The current rotation of the network. Used by covneyor belts. */
	private float rotation = 0;
	private long lastRotateTime;

	/**
	 * The cached connections of the mechanical network.
	 */
	private final WeakHashMap<IMechanical, WeakReference[]> connectionCache = new WeakHashMap<IMechanical, WeakReference[]>();

	/**
	 * Only add the exact instance of the connector into the network. Multipart tiles allowed!
	 */
	@Override
	public void addConnector(IMechanical connector)
	{
		super.addConnector(connector);
		NetworkTickHandler.addNetwork(this);
	}

	/**
	 * An network update called only server side.
	 */
	@Override
	public void update()
	{
		synchronized (getConnectors())
		{
			/**
			 * Update all mechanical nodes.
			 */
			Iterator<IMechanical> it = getConnectors().iterator();

			while (it.hasNext())
			{
				IMechanical mechanical = it.next();
				WeakReference[] connections = connectionCache.get(mechanical);

				if (connections != null)
				{
					for (int i = 0; i < connections.length; i++)
					{
						if (connections[i] != null)
						{
							ForgeDirection dir = ForgeDirection.getOrientation(i);
							Object adjacent = connections[i].get();

							if (adjacent instanceof IMechanical)
							{
								IMechanical adjacentMech = ((IMechanical) adjacent).getInstance(dir.getOpposite());

								if (adjacentMech != null && adjacent != mechanical)
								{
									float ratio = adjacentMech.getRatio(dir.getOpposite(), mechanical) / mechanical.getRatio(dir, adjacentMech);
									long torque = mechanical.getTorque();

									boolean inverseRotation = mechanical.inverseRotation(dir, adjacentMech) && adjacentMech.inverseRotation(dir.getOpposite(), mechanical);

									int inversion = inverseRotation ? -1 : 1;

									if (Math.abs(torque + inversion * (adjacentMech.getTorque() / ratio * ACCELERATION)) < Math.abs(adjacentMech.getTorque() / ratio))
										mechanical.setTorque((long) (torque + inversion * ((adjacentMech.getTorque() / ratio * ACCELERATION))));

									float velocity = mechanical.getAngularVelocity();

									if (Math.abs(velocity + inversion * (adjacentMech.getAngularVelocity() * ratio * ACCELERATION)) < Math.abs(adjacentMech.getAngularVelocity() * ratio))
										mechanical.setAngularVelocity(velocity + (inversion * adjacentMech.getAngularVelocity() * ratio * ACCELERATION));
								}
							}
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
		connectionCache.clear();
		super.reconstruct();
	}

	@Override
	protected void reconstructConnector(IMechanical node)
	{
		node.setNetwork(this);

		/**
		 * Cache connections.
		 */
		Object[] conn = node.getConnections();

		if (conn == null && node instanceof TileEntity)
		{
			/**
			 * Default connection implementation
			 */
			conn = new Object[6];

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = new Vector3((TileEntity) node).translate(dir).getTileEntity(((TileEntity) node).worldObj);

				if (tile instanceof IMechanical)
				{
					IMechanical mech = ((IMechanical) tile).getInstance(dir.getOpposite());

					if (mech != null && node.canConnect(dir, mech) && mech.canConnect(dir.getOpposite(), node))
					{
						conn[dir.ordinal()] = mech;
					}
				}
			}
		}

		WeakReference[] connections = new WeakReference[conn.length];

		for (int i = 0; i < connections.length; i++)
		{
			if (conn[i] != null)
			{
				if (conn[i] instanceof IMechanical)
				{
					IMechanical connected = ((IMechanical) conn[i]);

					if (connected.getNetwork() != this)
					{
						connected.getNetwork().getConnectors().clear();
						connected.setNetwork(this);
						addConnector(connected);
						reconstructConnector(connected);
					}
				}

				connections[i] = new WeakReference(conn[i]);
			}
		}

		connectionCache.put(node, connections);
	}

	@Override
	public Object[] getConnectionsFor(IMechanical connector)
	{
		Object[] conn = new Object[6];
		WeakReference[] connections = connectionCache.get(connector);

		if (connections != null)
		{
			for (int i = 0; i < connections.length; i++)
				if (connections[i] != null)
					conn[i] = connections[i].get();
		}

		return conn;
	}

	@Override
	public float getRotation(float velocity)
	{
		long deltaTime = System.currentTimeMillis() - lastRotateTime;

		if (deltaTime > 1)
		{
			rotation = (float) (((velocity) * (deltaTime / 1000d) + rotation) % (2 * Math.PI));
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
