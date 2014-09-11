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
import universalelectricity.api.core.grid.IUpdate;
import universalelectricity.core.transform.vector.IVectorWorld;
import universalelectricity.core.transform.vector.Vector3;
import codechicken.multipart.TMultiPart;
import universalelectricity.core.transform.vector.VectorWorld;

/**
 * Prefab node for the mechanical system used by almost ever mechanical object in Resonant Induction. Handles connections to other tiles, and shares power with them
 *
 * @author Calclavia, Darkguardsman
 */
public class MechanicalNode extends MultipartNode implements IMechanicalNode, ISaveObj, IVectorWorld, IUpdate
{
    /** Marks that the rotation has changed and should be updated client side */
	public boolean markRotationUpdate = false;

    /** Makrs that the torque value has changed and should be updated client side */
	public boolean markTorqueUpdate = false;

    /** Allows the node to share its power with other nodes*/
    public boolean sharePower = true;

	public double torque = 0, prevTorque;
	public double prevAngularVelocity, angularVelocity = 0;

    /** Current angle of rotation, mainly used for rendering */
    public double renderAngle = 0;

    /** Angle of rotation of last update */
    public double prev_angle = 0;

	public float acceleration = 2f;


	protected double maxDeltaAngle = Math.toRadians(120);
	protected double load = 2;

	private double power = 0;

    /** Current update tick # */
	private long ticks = 0;

	public MechanicalNode(INodeProvider parent)
	{
		super(parent);
	}

	@Override
	public double getRadius(ForgeDirection dir, IMechanicalNode with)
	{
		return 0.5;
	}

    @Override
    public double getAngularSpeed(ForgeDirection side)
    {
        return angularVelocity;
    }

    @Override
    public double getForce(ForgeDirection side)
    {
        return torque;
    }

    @Override
    public boolean inverseRotation(ForgeDirection side)
    {
        return false;
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
			ticks = 0;
		}

		//-----------------------------------
		// Render Update
		//-----------------------------------

        // Updates rotation angle and prevents it from rotating too fast
		if (angularVelocity >= 0)
		{
			renderAngle += Math.min(angularVelocity, this.maxDeltaAngle) * deltaTime;
		}
		else
		{
			renderAngle += Math.max(angularVelocity, -this.maxDeltaAngle) * deltaTime;
		}

        // Cap rotation angle to prevent render issues
		if (renderAngle >= Math.PI * 2)
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

            if(sharePower)
            {
                // Power sharing calculations
                Iterator<Entry<Object, ForgeDirection>> it = connections.entrySet().iterator();

                while (it.hasNext())
                {
                    MechanicalNode adjacentMech = null;
                    Entry<Object, ForgeDirection> entry = it.next();
                    ForgeDirection dir = entry.getValue();

                    // Get mech node
                    if (entry.getKey() instanceof MechanicalNode)
                    {
                        adjacentMech = (MechanicalNode) entry.getKey();
                    }
                    else if (entry.getKey() instanceof INodeProvider)
                    {
                        INode node = ((INodeProvider) entry.getKey()).getNode(MechanicalNode.class, dir.getOpposite());
                        if (node instanceof MechanicalNode)
                            adjacentMech = (MechanicalNode) node;
                    }
                    else
                    {
                        it.remove();
                    }

                    // If node is not null apply power
                    if (adjacentMech != null)
                    {
                        /** Calculate angular velocity and torque. */
                        double ratio = adjacentMech.getRadius(dir.getOpposite(), this) / getRadius(dir, adjacentMech);
                        boolean inverseRotation = inverseRotation(dir) && adjacentMech.inverseRotation(dir.getOpposite());

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

	private double getTorque()
	{
		return angularVelocity != 0 ? torque : 0;
	}

	private double getAngularSpeed()
	{
		return torque != 0 ? angularVelocity : 0;
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

	public double getEnergy()
	{
		return getTorque() * getAngularSpeed();
	}

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

    @Override
    public boolean isValidConnection(Object object)
    {
        return true;
    }
}
