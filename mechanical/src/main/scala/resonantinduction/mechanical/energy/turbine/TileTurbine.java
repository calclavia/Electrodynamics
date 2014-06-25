package resonantinduction.mechanical.energy.turbine;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INodeProvider;
import resonant.lib.multiblock.IMultiBlockStructure;
import resonant.lib.network.IPacketReceiverWithID;
import resonantinduction.mechanical.energy.grid.TileMechanical;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Reduced version of the main turbine class */
public class TileTurbine extends TileMechanical implements IMultiBlockStructure<TileTurbine>, INodeProvider, IPacketReceiverWithID
{
    /** Tier of the tile */
    public int tier = 0;

    /** Radius of large turbine? */
    public int multiBlockRadius = 1;

    /** Max power in watts. */
    protected long maxPower;

    /** The power of the turbine this tick. In joules/tick */
    public long power = 0;

    protected final long defaultTorque = 5000;  

    /** MutliBlock methods. */
    private TurbineMBlockHandler multiBlock;

    public TileTurbine()
    {
        super(Material.wood);
        mechanicalNode = new TurbineNode(this);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        getMultiBlock().update();

        if (getMultiBlock().isPrimary())
        {
            if (!worldObj.isRemote)
            {
                /** Set angular velocity based on power and torque. */
                mechanicalNode.angularVelocity = (float) ((double) power / mechanicalNode.torque);
            }

            if (mechanicalNode.angularVelocity != 0)
            {
                playSound();
            }
        }

        if (!worldObj.isRemote)
            power = 0;
    }

    protected long getMaxPower()
    {
        if (this.getMultiBlock().isConstructed())
        {
            return (long) (maxPower * getArea());
        }

        return maxPower;
    }

    public int getArea()
    {
        return (int) (((multiBlockRadius + 0.5) * 2) * ((multiBlockRadius + 0.5) * 2));
    }

    /** Called to play sound effects */
    public void playSound()
    {

    }   

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        multiBlockRadius = nbt.getInteger("multiBlockRadius");
        tier = nbt.getInteger("tier");
        getMultiBlock().load(nbt);
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("multiBlockRadius", multiBlockRadius);
        nbt.setInteger("tier", tier);
        getMultiBlock().save(nbt);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getAABBPool().getAABB(this.xCoord - multiBlockRadius, this.yCoord - multiBlockRadius, this.zCoord - multiBlockRadius, this.xCoord + 1 + multiBlockRadius, this.yCoord + 1 + multiBlockRadius, this.zCoord + 1 + multiBlockRadius);
    }

    @Override
    public Vector3[] getMultiBlockVectors()
    {
        Set<Vector3> vectors = new HashSet<Vector3>();

        ForgeDirection dir = getDirection();
        int xMulti = dir.offsetX != 0 ? 0 : 1;
        int yMulti = dir.offsetY != 0 ? 0 : 1;
        int zMulti = dir.offsetZ != 0 ? 0 : 1;

        for (int x = -multiBlockRadius; x <= multiBlockRadius; x++)
        {
            for (int y = -multiBlockRadius; y <= multiBlockRadius; y++)
            {
                for (int z = -multiBlockRadius; z <= multiBlockRadius; z++)
                {
                    vectors.add(new Vector3(x * xMulti, y * yMulti, z * zMulti));
                }
            }
        }

        return vectors.toArray(new Vector3[0]);
    }

    @Override
    public Vector3 getPosition()
    {
        return new Vector3(this);
    }

    @Override
    public TurbineMBlockHandler getMultiBlock()
    {
        if (multiBlock == null)
            multiBlock = new TurbineMBlockHandler(this);

        return multiBlock;
    }

    @Override
    public void onMultiBlockChanged()
    {
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType() != null ? getBlockType().blockID : 0);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public World getWorld()
    {
        return worldObj;
    }
}
