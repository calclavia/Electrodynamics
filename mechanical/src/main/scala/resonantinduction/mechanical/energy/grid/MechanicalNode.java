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
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.TMultiPart;

/** A mechanical node for mechanical energy.
 * 
 * From Darkguardsman,
 * 
 * Comes built in with extra debug but must be manually triggered by the part using this node. The
 * suggest approach is threw right click of the part with a tool or simple item. In which you will
 * have to switch debug to true. As well provide a way to increase or decrease the cue value. The
 * cue as well is used to divide up the debug based on area inside the node. This allows you to
 * debug one part at a time rather than be spammed with console chat.
 * 
 * An external gui is also support for this node but must also be manually created by the part. This
 * gui will show basic info about velocity, angle, torque, and connections. Additional information
 * can be added by extending the gui.
 * 
 * @author Calclavia, Darkguardsman */
public class MechanicalNode implements IMechanicalNode, ISaveObj
{
    /** Is debug enabled for the node */
    public boolean doDebug = false;
    /** Which section of debug is enabled */
    public int debugCue = 0, maxDebugCue = 1, minDebugCue = 0;
    public static final int UPDATE_DEBUG = 0, CONNECTION_DEBUG = 1;
    /** Rotational Force */
    public double torque = 0;
    /** Rotational speed */
    public double prevAngularVelocity, angularVelocity = 0;
    /** Rotational acceleration */
    public float acceleration = 2f;

    /** The current rotation of the mechanical node. */
    public double renderAngle = 0, prev_angle = 0;
    /** Limits the max distance an object can rotate in a single update */
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

    public void debug(String txt)
    {
        debug(txt, UPDATE_DEBUG);
    }

    public void debug(String txt, int cue)
    {
        debug(txt, cue, false);
    }

    public void debug(String txt, int cue, boolean nextLine)
    {
        if (doDebug && world() != null && !world().isRemote && cue == debugCue)
            System.out.println((nextLine ? "\n" : "") + "[MechMode]" + txt);
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
        debug("Node->Update");
        prevAngularVelocity = angularVelocity;
        debug("\tNode :" + toString());

        if (angularVelocity >= 0)
        {
            renderAngle += Math.min(angularVelocity, this.maxDeltaAngle) * deltaTime;
        }
        else
        {
            renderAngle += Math.max(angularVelocity, -this.maxDeltaAngle) * deltaTime;
        }
        debug("\tAngle: " + renderAngle + "  Vel: " + angularVelocity);

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

            //----------------------------------- 
            // Loss calculations
            //-----------------------------------
            double torqueLoss = Math.min(Math.abs(getTorque()), (Math.abs(getTorque() * getTorqueLoad()) + getTorqueLoad() / 10) * deltaTime);

            if (torque > 0)
            {
                torque -= torqueLoss;
            }
            else
            {
                torque += torqueLoss;
            }

            double velocityLoss = Math.min(Math.abs(getAngularSpeed()), (Math.abs(getAngularSpeed() * getAngularVelocityLoad()) + getAngularVelocityLoad() / 10) * deltaTime);

            if (angularVelocity > 0)
            {
                angularVelocity -= velocityLoss;
            }
            else
            {
                angularVelocity += velocityLoss;
            }

            if (getEnergy() <= 0)
            {
                angularVelocity = torque = 0;
            }

            power = getEnergy() / deltaTime;

            //----------------------------------- 
            // Connection application of force and speed
            //-----------------------------------
            debug("Node->Connections");
            synchronized (getConnections())
            {
                Iterator<Entry<MechanicalNode, ForgeDirection>> it = getConnections().entrySet().iterator();

                while (it.hasNext())
                {
                    Entry<MechanicalNode, ForgeDirection> entry = it.next();

                    ForgeDirection dir = entry.getValue();
                    MechanicalNode adjacentMech = entry.getKey();
                    debug("\tConnection: " + adjacentMech + "  Side: " + dir);
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

    /** Called when one revolution is made. */
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

    /** The energy percentage loss due to resistance in seconds. */
    public double getTorqueLoad()
    {
        return load;
    }

    public double getAngularVelocityLoad()
    {
        return load;
    }

    public World world()
    {
        return getParent() instanceof TMultiPart ? ((TMultiPart) getParent()).world() : getParent() instanceof TileEntity ? ((TileEntity) getParent()).getWorldObj() : null;
    }

    public Vector3 position()
    {
        return getParent() instanceof TMultiPart ? new Vector3(((TMultiPart) getParent()).x(), ((TMultiPart) getParent()).y(), ((TMultiPart) getParent()).z()) : getParent() instanceof TileEntity ? new Vector3((TileEntity) getParent()) : null;
    }

    /** Checks to see if a connection is allowed from side and from a source */
    public boolean canConnect(ForgeDirection from, Object source)
    {
        debug("Node -> Canconnect", CONNECTION_DEBUG);
        if (source instanceof MechanicalNode)
        {
            boolean flag = (connectionMap & (1 << from.ordinal())) != 0;
            debug("\t" + flag, CONNECTION_DEBUG);
            return flag;
        }
        debug("\tFalse", CONNECTION_DEBUG);
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
        debug("reconstruct", CONNECTION_DEBUG);
        debug("reconstruct");
        recache();
    }

    @Override
    public void deconstruct()
    {
        debug("deconstruct");
        for (Entry<MechanicalNode, ForgeDirection> entry : getConnections().entrySet())
        {
            entry.getKey().recache();
        }
        getConnections().clear();
    }

    @Override
    public void recache()
    {
        debug("Node->Recahce", CONNECTION_DEBUG);
        getConnections().clear();

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            debug("\tDir: " + dir, CONNECTION_DEBUG);
            TileEntity tile = position().translate(dir).getTileEntity(world());
            debug("\tTile: " + tile, CONNECTION_DEBUG);
            if (tile instanceof INodeProvider)
            {
                debug("\tTile instanceof INodeProvider", CONNECTION_DEBUG);
                INode node = ((INodeProvider) tile).getNode(MechanicalNode.class, dir.getOpposite());
                if (node instanceof MechanicalNode)
                {
                    debug("\tNode instanceof MechanicalNode", CONNECTION_DEBUG);
                    MechanicalNode check = (MechanicalNode) node;

                    if (check != null && canConnect(dir, check) && check.canConnect(dir.getOpposite(), this))
                    {
                        debug("\tCanConnect and added to connections", CONNECTION_DEBUG);
                        getConnections().put(check, dir);
                    }
                }
            }
        }
    }

    /** Gets the node provider for this node */
    public INodeProvider getParent()
    {
        return parent;
    }

    /** Sets the node provider for the node */
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
}
