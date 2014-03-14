package resonantinduction.mechanical.energy.grid;

import java.util.Iterator;
import java.util.Map.Entry;

import calclavia.api.resonantinduction.IMechanicalNode;
import calclavia.lib.grid.INodeProvider;
import calclavia.lib.grid.Node;
import calclavia.lib.grid.TickingGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.TMultiPart;

/**
 * A mechanical node for mechanical energy.
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
public class MechanicalNode extends Node<INodeProvider, TickingGrid, MechanicalNode> implements IMechanicalNode
{
	public double torque = 0;
	public double prevAngularVelocity, angularVelocity = 0;
	public float acceleration = 2f;

	/**
	 * The current rotation of the mechanical node.
	 */
	public double angle = 0;

	protected double load = 2;
	protected byte connectionMap = Byte.parseByte("111111", 2);

	private double power = 0;

	public MechanicalNode(INodeProvider parent)
	{
		super(parent);
	}

	@Override
	public MechanicalNode setLoad(double load)
	{
		this.load = load;
		return this;
	}

	public MechanicalNode setConnection(byte connectionMap)
	{
		this.connectionMap = connectionMap;
		return this;
	}

	@Override
	public void update(float deltaTime)
	{
		prevAngularVelocity = angularVelocity;

		if (!ResonantInduction.proxy.isPaused())
			angle += angularVelocity * deltaTime;

		if (angle % (Math.PI * 2) != angle)
		{
			revolve();
			angle = angle % (Math.PI * 2);
		}

		if (world() != null && !world().isRemote)
		{
			double acceleration = this.acceleration * deltaTime;

			/**
			 * Energy loss
			 */
			double torqueLoss = Math.min(Math.abs(getTorque()), (Math.abs(getTorque() * getTorqueLoad()) + getTorqueLoad() / 10) * deltaTime);

			if (torque > 0)
				torque -= torqueLoss;
			else
				torque += torqueLoss;

			double velocityLoss = Math.min(Math.abs(getAngularVelocity()), (Math.abs(getAngularVelocity() * getAngularVelocityLoad()) + getAngularVelocityLoad() / 10) * deltaTime);

			if (angularVelocity > 0)
				angularVelocity -= velocityLoss;
			else
				angularVelocity += velocityLoss;

			power = getEnergy() / deltaTime;

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
					// adjacentMech.angle = Math.abs(angle) * (adjacentMech.angle >= 0 ? 1 : -1);
				}
			}
		}

		onUpdate();
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

	@Override
	public void apply(double torque, double angularVelocity)
	{
		this.torque += torque;
		this.angularVelocity += angularVelocity;
	}

	@Override
	public double getTorque()
	{
		return angularVelocity != 0 ? torque : 0;
	}

	@Override
	public double getAngularVelocity()
	{
		return torque != 0 ? angularVelocity : 0;
	}

	@Override
	public float getRatio(ForgeDirection dir, IMechanicalNode with)
	{
		return 0.5f;
	}

	@Override
	public boolean inverseRotation(ForgeDirection dir, IMechanicalNode with)
	{
		return true;
	}

	/**
	 * The energy percentage loss due to resistance in seconds.
	 */
	public double getTorqueLoad()
	{
		return load;
	}

	public double getAngularVelocityLoad()
	{
		return load;
	}

	/**
	 * Recache the connections. This is the default connection implementation.
	 */
	@Override
	public void recache()
	{
		synchronized (connections)
		{
			connections.clear();

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = position().translate(dir).getTileEntity(world());

				if (tile instanceof INodeProvider)
				{
					MechanicalNode check = ((INodeProvider) tile).getNode(MechanicalNode.class, dir.getOpposite());

					if (check != null && canConnect(dir, check) && check.canConnect(dir.getOpposite(), this))
					{
						connections.put(check, dir);
					}
				}
			}
		}
	}

	public World world()
	{
		return parent instanceof TMultiPart ? ((TMultiPart) parent).world() : parent instanceof TileEntity ? ((TileEntity) parent).getWorldObj() : null;
	}

	public Vector3 position()
	{
		return parent instanceof TMultiPart ? new Vector3(((TMultiPart) parent).x(), ((TMultiPart) parent).y(), ((TMultiPart) parent).z()) : parent instanceof TileEntity ? new Vector3((TileEntity) parent) : null;
	}

	@Override
	public boolean canConnect(ForgeDirection from, Object source)
	{
		return (source instanceof MechanicalNode) && (connectionMap & (1 << from.ordinal())) != 0;
	}

	@Override
	public double getEnergy()
	{
		return getTorque() * getAngularVelocity();
	}

	@Override
	public double getPower()
	{
		return power;
	}

	@Override
	public TickingGrid newGrid()
	{
		return new TickingGrid<MechanicalNode>(this, MechanicalNode.class);
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
