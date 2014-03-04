package resonantinduction.mechanical.energy.network;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import resonantinduction.core.grid.IGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.TMultiPart;

public class MechanicalNode extends EnergyNode
{
	protected final AbstractMap<MechanicalNode, ForgeDirection> connections = new WeakHashMap<MechanicalNode, ForgeDirection>();

	protected final IMechanicalNodeProvider parent;

	public double torque = 0;
	public double prevAngularVelocity, angularVelocity = 0;
	public float acceleration = 2f;

	/**
	 * The current rotation of the mechanical node.
	 */
	public double angle = 0;

	protected double load = 1;

	public MechanicalNode(IMechanicalNodeProvider parent)
	{
		this.parent = parent;
	}

	public MechanicalNode setLoad(double load)
	{
		this.load = load;
		return this;
	}

	@Override
	public void update(float deltaTime)
	{
		float acceleration = this.acceleration * deltaTime;
		double load = getLoad() * deltaTime;

		prevAngularVelocity = angularVelocity;

		onUpdate();

		/**
		 * Loss energy
		 */
		torque -= torque * torque * load;
		angularVelocity -= angularVelocity * angularVelocity * load;

		angle += angularVelocity / 20;

		if (angle % (Math.PI * 2) != angle)
		{
			revolve();
			angle = angle % (Math.PI * 2);
		}

		synchronized (connections)
		{
			Iterator<Entry<MechanicalNode, ForgeDirection>> it = connections.entrySet().iterator();

			while (it.hasNext())
			{
				Entry<MechanicalNode, ForgeDirection> entry = it.next();

				ForgeDirection dir = entry.getValue();
				MechanicalNode adjacentMech = entry.getKey();

				/**
				 * Calculate angular velocity and torque.
				 */
				float ratio = adjacentMech.getRatio(dir.getOpposite(), this) / getRatio(dir, adjacentMech);
				boolean inverseRotation = inverseRotation(dir, adjacentMech) && adjacentMech.inverseRotation(dir.getOpposite(), this);

				int inversion = inverseRotation ? -1 : 1;

				if (Math.abs(torque + inversion * (adjacentMech.getTorque() / ratio * acceleration)) < Math.abs(adjacentMech.getTorque() / ratio))
					torque = torque + inversion * (adjacentMech.getTorque() / ratio * acceleration);

				if (Math.abs(angularVelocity + inversion * (adjacentMech.getAngularVelocity() * ratio * acceleration)) < Math.abs(adjacentMech.getAngularVelocity() * ratio))
					angularVelocity = angularVelocity + (inversion * adjacentMech.getAngularVelocity() * ratio * acceleration);

				/**
				 * Set all current rotations
				 */
				adjacentMech.angle = Math.abs(angle) * (adjacentMech.angle >= 0 ? 1 : -1);
			}
		}
	}

	protected void onUpdate()
	{

	}

	/**
	 * Called when one revolution is made.
	 */
	protected void revolve()
	{

	}

	public void apply(double torque, double angularVelocity)
	{
		this.torque += torque;
		this.angularVelocity += angularVelocity;
	}

	public double getTorque()
	{
		return torque;
	}

	public double getAngularVelocity()
	{
		return angularVelocity;
	}

	public float getRatio(ForgeDirection dir, MechanicalNode with)
	{
		return 0.5f;
	}

	public boolean inverseRotation(ForgeDirection dir, MechanicalNode with)
	{
		return true;
	}

	/**
	 * The energy percentage loss due to resistance in seconds.
	 */
	public double getLoad()
	{
		return load;
	}

	/**
	 * Recache the connections. This is the default connection implementation.
	 */
	public void recache()
	{
		synchronized (connections)
		{
			connections.clear();

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = position().translate(dir).getTileEntity(world());

				if (tile instanceof IMechanicalNodeProvider)
				{
					MechanicalNode check = ((IMechanicalNodeProvider) tile).getNode(dir.getOpposite());

					if (check != null && canConnect(dir, check) && check.canConnect(dir.getOpposite(), this))
					{
						connections.put(check, dir);
					}
				}
			}
		}
	}

	@Override
	public AbstractMap<MechanicalNode, ForgeDirection> getConnections()
	{
		return connections;
	}

	public World world()
	{
		return parent instanceof TMultiPart ? ((TMultiPart) parent).world() : parent instanceof TileEntity ? ((TileEntity) parent).getWorldObj() : null;
	}

	public Vector3 position()
	{
		return parent instanceof TMultiPart ? new Vector3(((TMultiPart) parent).x(), ((TMultiPart) parent).y(), ((TMultiPart) parent).z()) : parent instanceof TileEntity ? new Vector3((TileEntity) parent) : null;
	}

	public boolean canConnect(ForgeDirection from, Object source)
	{
		return true;
	}

	@Override
	public double getEnergy()
	{
		return torque * angularVelocity;
	}

	@Override
	public IGrid newGrid()
	{
		return new MechanicalNetwork(this);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		torque = nbt.getDouble("torque");
		angularVelocity = nbt.getDouble("angularVelocity");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setDouble("torque", torque);
		nbt.setDouble("angularVelocity", angularVelocity);
	}
}
