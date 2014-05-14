package resonantinduction.electrical.battery;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.prefab.tile.TileElectrical;
import universalelectricity.api.net.IConnector;
import universalelectricity.api.vector.Vector3;

public class TileEnergyDistribution extends TileElectrical implements IConnector<EnergyDistributionNetwork>
{
    public boolean markClientUpdate = false;
    public boolean markDistributionUpdate = false;
    public long renderEnergyAmount = 0;
    private EnergyDistributionNetwork network;

    public TileEnergyDistribution()
    {
        super(null);
    }

    public TileEnergyDistribution(Material material)
    {
        super(material);
    }

    @Override
    public void initiate()
    {
        super.initiate();
        this.updateStructure();
    }

    @Override
    public void onAdded()
    {
        if (!world().isRemote)
        {
            updateStructure();
        }
    }

    @Override
    public void onNeighborChanged()
    {
        if (!world().isRemote)
        {
            updateStructure();
        }
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (!this.worldObj.isRemote)
        {
            if (markDistributionUpdate && ticks % 5 == 0)
            {
                getNetwork().redistribute();
                markDistributionUpdate = false;
            }

            if (markClientUpdate && ticks % 5 == 0)
            {
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    @Override
    public long getEnergy(ForgeDirection from)
    {
        return getNetwork().totalEnergy;
    }

    @Override
    public long getEnergyCapacity(ForgeDirection from)
    {
        return getNetwork().totalCapacity;

    }

    @Override
    public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        long returnValue = super.onReceiveEnergy(from, receive, doReceive);
        markDistributionUpdate = true;
        markClientUpdate = true;
        return returnValue;
    }

    @Override
    public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
    {
        long returnValue = super.onExtractEnergy(from, extract, doExtract);
        markDistributionUpdate = true;
        markClientUpdate = true;
        return returnValue;
    }

    @Override
    public EnergyDistributionNetwork getNetwork()
    {
        if (this.network == null)
        {
            this.network = new EnergyDistributionNetwork();
            this.network.addConnector(this);
        }

        return this.network;
    }

    @Override
    public void setNetwork(EnergyDistributionNetwork structure)
    {
        this.network = structure;
    }

    public void updateStructure()
    {
        if (!this.worldObj.isRemote)
        {
            for (Object obj : getConnections())
            {
                if (obj != null)
                {
                    this.getNetwork().merge(((TileEnergyDistribution) obj).getNetwork());
                }
            }

            markDistributionUpdate = true;
            markClientUpdate = true;
        }
    }

    @Override
    public Object[] getConnections()
    {
        Object[] connections = new Object[6];

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            TileEntity tile = new Vector3(this).translate(dir).getTileEntity(this.worldObj);

            if (tile != null && tile.getClass() == this.getClass())
            {
                connections[dir.ordinal()] = tile;
            }
        }

        return connections;
    }

    @Override
    public void invalidate()
    {
        this.getNetwork().redistribute(this);
        this.getNetwork().split(this);
        super.invalidate();
    }

    @Override
    public IConnector<EnergyDistributionNetwork> getInstance(ForgeDirection from)
    {
        return this;
    }

}
