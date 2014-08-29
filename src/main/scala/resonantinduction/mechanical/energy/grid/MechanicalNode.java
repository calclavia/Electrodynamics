package resonantinduction.mechanical.energy.grid;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.lib.utility.nbt.ISaveObj;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.core.prefab.node.MultipartNode;
import resonantinduction.mechanical.Mechanical;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.core.transform.vector.IVectorWorld;
import universalelectricity.core.transform.vector.Vector3;
import codechicken.multipart.TMultiPart;
import universalelectricity.core.transform.vector.VectorWorld;

/**
 * A resonantinduction.mechanical node for resonantinduction.mechanical energy.
 *
 * @author Calclavia, Darkguardsman
 */
public class MechanicalNode extends MultipartNode implements IMechanicalNode, ISaveObj, IVectorWorld
{
	public boolean markRotationUpdate = false;
	public boolean markTorqueUpdate = false;

	public double torque = 0, prevTorque;
	public double prevAngularVelocity, angularVelocity = 0;
    public double renderAngle = 0, prev_angle = 0;

	public float acceleration = 2f;


	protected double maxDeltaAngle = Math.toRadians(120);
	protected double load = 2;

	private double power = 0;

	private long ticks = 0;

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
	public double getRadius()
	{
		return 0.5;
	}

	final public void update()
	{
		update(0.05f);
	}

	@Override
	final public void update(double deltaTime)
	{
		ticks++;
		if (ticks >= Long.MAX_VALUE)
		{
			ticks = 1;
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
            Iterator<Entry<Object, ForgeDirection>> it = connections.entrySet().iterator();

            while (it.hasNext())
            {
                MechanicalNode adjacentMech = null;
                Entry<Object, ForgeDirection> entry = it.next();
                ForgeDirection dir = entry.getValue();

                if (entry.getKey() instanceof MechanicalNode) adjacentMech = (MechanicalNode) entry.getKey();
                if (entry.getKey() instanceof INodeProvider)
                {
                    INode node = ((INodeProvider) entry.getKey()).getNode(MechanicalNode.class, dir.getOpposite());
                    if(node instanceof MechanicalNode)
                        adjacentMech = (MechanicalNode) node;
                }
                if (adjacentMech != null)
                {
                    /** Calculate angular velocity and torque. */
                    float ratio = adjacentMech.getRatio(dir.getOpposite(), this) / getRatio(dir, adjacentMech);
                    boolean inverseRotation = inverseRotation(dir, adjacentMech) && adjacentMech.inverseRotation(dir.getOpposite(), this);

                    int inversion = inverseRotation ? -1 : 1;

                    double targetTorque = inversion * adjacentMech.getTorque() / ratio;
                    double applyTorque = targetTorque * acceleration;

                    if (Math.abs(torque + applyTorque) < Math.abs(targetTorque)) {
                        torque += applyTorque;
                    } else if (Math.abs(torque - applyTorque) > Math.abs(targetTorque)) {
                        torque -= applyTorque;
                    }

                    double targetVelocity = inversion * adjacentMech.getAngularSpeed() * ratio;
                    double applyVelocity = targetVelocity * acceleration;

                    if (Math.abs(angularVelocity + applyVelocity) < Math.abs(targetVelocity)) {
                        angularVelocity += applyVelocity;
                    } else if (Math.abs(angularVelocity - applyVelocity) > Math.abs(targetVelocity)) {
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

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public boolean continueUpdate() {
        return true;
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
    public boolean canConnect(ForgeDirection direction, Object object)
    {
        if(canConnect(direction)) {
            if (object instanceof INodeProvider)
            {
                return ((INodeProvider) object).getNode(MechanicalNode.class, direction.getOpposite()) instanceof MechanicalNode;
            }
            return object instanceof MechanicalNode;
        }
        return false;
    }
}
