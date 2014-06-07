package resonantinduction.mechanical.energy.turbine;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.lib.network.Synced;
import resonant.lib.network.Synced.SyncedInput;
import resonant.lib.network.Synced.SyncedOutput;
import resonantinduction.mechanical.energy.grid.MechanicalNode;

//TODO: MC 1.7, merge turbines in.
public class TileMechanicalTurbine extends TileTurbineBase implements INodeProvider
{
    protected MechanicalNode mechanicalNode;
    @Synced(1)
    protected double renderAngularVelocity;
    protected double renderAngle;

    protected double prevAngularVelocity;

    public TileMechanicalTurbine()
    {
        super();
        mechanicalNode = new TurbineNode(this);
    }

    @Override
    public void initiate()
    {
        mechanicalNode.reconstruct();
        super.initiate();
    }

    @Override
    public void invalidate()
    {
        mechanicalNode.deconstruct();
        super.invalidate();
    }

    @Override
    public void updateEntity()
    {
        mechanicalNode.update(0.05f);
        if (!worldObj.isRemote)
        {
            renderAngularVelocity = (double) mechanicalNode.angularVelocity;

            if (renderAngularVelocity != prevAngularVelocity)
            {
                prevAngularVelocity = renderAngularVelocity;
                sendPowerUpdate();
            }
        }
        else
        {
            renderAngle = (renderAngle + renderAngularVelocity / 20) % (Math.PI * 2);

            // TODO: Make this neater
            onProduce();
        }

        super.updateEntity();
    }

    @Override
    public void onProduce()
    {
        if (!worldObj.isRemote)
        {
            if (mechanicalNode.torque < 0)
                torque = -Math.abs(torque);

            if (mechanicalNode.angularVelocity < 0)
                angularVelocity = -Math.abs(angularVelocity);

            mechanicalNode.apply(this, (torque - mechanicalNode.getTorque()) / 10, (angularVelocity - mechanicalNode.getAngularSpeed()) / 10);
        }
    }

    @Override
    public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
    {
        if (nodeType.isAssignableFrom(mechanicalNode.getClass()))
            return ((TileMechanicalTurbine) getMultiBlock().get()).mechanicalNode;
        return null;
    }

    @Override
    @SyncedInput
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        tier = nbt.getInteger("tier");
        mechanicalNode.load(nbt);
    }

    /** Writes a tile entity to NBT. */
    @Override
    @SyncedOutput
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("tier", tier);
        mechanicalNode.save(nbt);
    }
}
