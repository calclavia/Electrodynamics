package resonantinduction.mechanical.energy.grid;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.lib.utility.nbt.ISaveObj;
import resonantinduction.core.interfaces.IMechanicalNode;
import universalelectricity.api.vector.IVectorWorld;
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.TMultiPart;

/**
 * A mechanical node for mechanical energy.
 *
 * @author Calclavia, Darkguardsman
 */
public class MechanicalNode implements IMechanicalNode, ISaveObj, IVectorWorld
{
	/**
	 * Is debug enabled for the node
	 */
	public boolean doDebug = false;
	/**
	 * Used to note that you should trigger a packet update for rotation
	 */
	public boolean markRotationUpdate = false;
	public boolean markTorqueUpdate = false;
	/**
	 * Which section of debug is enabled
	 */
	public int debugCue = 0, maxDebugCue = 1, minDebugCue = 0;
	public static final int UPDATE_DEBUG = 0, CONNECTION_DEBUG = 1;
	/**
	 * Rotational Force
	 */
	public double torque = 0, prevTorque;
	/**
	 * Rotational speed
	 */
	public double prevAngularVelocity, angularVelocity = 0;
	/**
	 * Rotational acceleration
	 */
	public float acceleration = 2f;

	/**
	 * The current rotation of the mechanical node.
	 */
	public double renderAngle = 0, prev_angle = 0;
	/**
	 * Limits the max distance an object can rotate in a single update
	 */
	protected double maxDeltaAngle = Math.toRadians(180);

	protected double load = 2;
	protected byte connectionMap = Byte.parseByte("111111", 2);

	private double power = 0;
	private INodeProvider parent;
	private long ticks = 0;

	private final AbstractMap<MechanicalNode, ForgeDirection> connections = new WeakHashMap<MechanicalNode, ForgeDirection>();

	public MechanicalNode(INodeProvider parent)
	{
		this.setParent(parent);
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
	public double getRadius()
	{
		return 0.5;
	}

	public void update()
	{
		update(0.05f);
	}

	@Override
	public void update(float deltaTime)
	{
		ticks++;
		if (ticks >= Long.MAX_VALUE)
		{
			ticks = 1;
		}
		//temp, TODO find a better way to trigger this
		if (ticks % 100 == 0)
		{
			this.recache();
		}
		//-----------------------------------
		// Render Update
		//-----------------------------------

		if (angularVelocity >= 0)
		{
			renderAngle += Math.min(angularVelocity, this.maxDeltaAngle) * deltaTime;
		}
		else
		{
			renderAngle += Math.max(angularVelocity, -this.maxDeltaAngle) * deltaTime;
		}

		if (renderAngle % (Math.PI * 2) != renderAngle)
		{
			revolve();
			renderAngle = renderAngle % (Math.PI * 2);
		}

		//-----------------------------------
		// Server side Update
		//-----------------------------------
		if (world() != null && !world().isRemote)
		{
			final double acceleration = this.acceleration * deltaTime;

			if (Math.abs(prevAngularVelocity - angularVelocity) > 0.01f)
			{
				prevAngularVelocity = angularVelocity;
				markRotationUpdate = true;
			}

			if (Math.abs(prevTorque - torque) > 0.01f)
			{
				prevTorque = torque;
				markTorqueUpdate = true;
			}

			//-----------------------------------
			// Loss calculations
			//-----------------------------------
			double torqueLoss = Math.min(Math.abs(getTorque()), (Math.abs(getTorque() * getTorqueLoad()) + getTorqueLoad() / 10) * deltaTime);
			torque += torque > 0 ? -torqueLoss : torqueLoss;

			double velocityLoss = Math.min(Math.abs(getAngularSpeed()), (Math.abs(getAngularSpeed() * getAngularVelocityLoad()) + getAngularVelocityLoad() / 10) * deltaTime);
			angularVelocity += angularVelocity > 0 ? -velocityLoss : velocityLoss;

			if (getEnergy() <= 0)
			{
				angularVelocity = torque = 0;
			}

			power = getEnergy() / deltaTime;

			//-----------------------------------
			// Connection application of force and speed
			//-----------------------------------
			synchronized (getConnections())
			{
				Iterator<Entry<MechanicalNode, ForgeDirection>> it = getConnections().entrySet().iterator();

				while (it.hasNext())
				{
					Entry<MechanicalNode, ForgeDirection> entry = it.next();

					ForgeDirection dir = entry.getValue();
					MechanicalNode adjacentMech = entry.getKey();
					/** Calculate angular velocity and torque. */
					float ratio = adjacentMech.getRatio(dir.getOpposite(), this) / getRatio(dir, adjacentMech);
					boolean inverseRotation = inverseRotation(dir, adjacentMech) && adjacentMech.inverseRotation(dir.getOpposite(), this);

					int inversion = inverseRotation ? -1 : 1;

					double targetTorque = inversion * adjacentMech.getTorque() / ratio;
					double applyTorque = targetTorque * acceleration;

					if (Math.abs(torque + applyTorque) < Math.abs(targetTorque))
					{
						torque += applyTorque;
					}
					else if (Math.abs(torque - applyTorque) > Math.abs(targetTorque))
					{
						torque -= applyTorque;
					}

					double targetVelocity = inversion * adjacentMech.getAngularSpeed() * ratio;
					double applyVelocity = targetVelocity * acceleration;

					if (Math.abs(angularVelocity + applyVelocity) < Math.abs(targetVelocity))
					{
						angularVelocity += applyVelocity;
					}
					else if (Math.abs(angularVelocity - applyVelocity) > Math.abs(targetVelocity))
					{
						angularVelocity -= applyVelocity;
					}

					/** Set all current rotations */
					// adjacentMech.angle = Math.abs(angle) * (adjacentMech.angle >= 0 ? 1 : -1);
				}
			}
		}

		onUpdate();
		prev_angle = renderAngle;
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
	public void apply(Object source, double torque, double angularVelocity)
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
	public double getAngularSpeed()
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
	 * Checks to see if a connection is allowed from side and from a source
	 */
	public boolean canConnect(ForgeDirection from, Object source)
	{
		if (source instanceof MechanicalNode)
		{
			boolean flag = (connectionMap & (1 << from.ordinal())) != 0;
			return flag;
		}
		return false;
	}

	@Override
	public double getEnergy()
	{
		return getTorque() * getAngularSpeed();
	}

	@Override
	public double getPower()
	{
		return power;
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		torque = nbt.getDouble("torque");
		angularVelocity = nbt.getDouble("angularVelocity");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setDouble("torque", torque);
		nbt.setDouble("angularVelocity", angularVelocity);
	}

	@Override
	public void reconstruct()
	{
		recache();
	}

	@Override
	public void deconstruct()
	{
		for (Entry<MechanicalNode, ForgeDirection> entry : getConnections().entrySet())
		{
			entry.getKey().getConnections().remove(this);
			entry.getKey().recache();
		}
		getConnections().clear();
	}

	@Override
	public void recache()
	{
		synchronized (this)
		{
			getConnections().clear();

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = position().translate(dir).getTileEntity(world());
				if (tile instanceof INodeProvider)
				{
					INode node = ((INodeProvider) tile).getNode(MechanicalNode.class, dir.getOpposite());
					if (node instanceof MechanicalNode)
					{
						MechanicalNode check = (MechanicalNode) node;
						boolean canConnect = canConnect(dir, check);
						boolean canOtherConnect = check.canConnect(dir.getOpposite(), this);
						if (canConnect && canOtherConnect)
						{
							getConnections().put(check, dir);
						}
					}
				}
			}
		}
	}

	/**
	 * Gets the node provider for this node
	 */
	public INodeProvider getParent()
	{
		return parent;
	}

	/**
	 * Sets the node provider for the node
	 */
	public void setParent(INodeProvider parent)
	{
		this.parent = parent;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + this.hashCode();
	}

	public AbstractMap<MechanicalNode, ForgeDirection> getConnections()
	{
		return connections;
	}

	@Override
	public World world()
	{
		return getParent() instanceof TMultiPart ? ((TMultiPart) getParent()).world() : getParent() instanceof TileEntity ? ((TileEntity) getParent()).getWorldObj() : null;
	}

	public Vector3 position()
	{
		return new Vector3(x(), y(), z());
	}

	@Override
	public double z()
	{
		if (this.getParent() instanceof TileEntity)
		{
			return ((TileEntity) this.getParent()).zCoord;
		}
		return this.getParent() instanceof TMultiPart && ((TMultiPart) this.getParent()).tile() != null ? ((TMultiPart) this.getParent()).z() : 0;
	}

	@Override
	public double x()
	{
		if (this.getParent() instanceof TileEntity)
		{
			return ((TileEntity) this.getParent()).xCoord;
		}
		return this.getParent() instanceof TMultiPart && ((TMultiPart) this.getParent()).tile() != null ? ((TMultiPart) this.getParent()).x() : 0;
	}

	@Override
	public double y()
	{
		if (this.getParent() instanceof TileEntity)
		{
			return ((TileEntity) this.getParent()).yCoord;
		}
		return this.getParent() instanceof TMultiPart && ((TMultiPart) this.getParent()).tile() != null ? ((TMultiPart) this.getParent()).y() : 0;
	}
}
