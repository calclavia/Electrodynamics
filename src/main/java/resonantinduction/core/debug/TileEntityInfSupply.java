package resonantinduction.core.debug;

import java.util.EnumSet;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.tile.TileMachine;
import universalelectricity.api.energy.EnergyStorageHandler;

public class TileEntityInfSupply extends TileMachine
{
    public TileEntityInfSupply()
    {
        this.energy = new EnergyStorageHandler(Long.MAX_VALUE);
        this.energy.setMaxExtract(Long.MAX_VALUE);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        this.energy.setEnergy(Long.MAX_VALUE);
        this.produce();
    }

    @Override
    public EnumSet<ForgeDirection> getOutputDirections()
    {
        return EnumSet.allOf(ForgeDirection.class);
    }

    @Override
    public EnumSet<ForgeDirection> getInputDirections()
    {
        return EnumSet.noneOf(ForgeDirection.class);
    }

    @Override
    public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        return 0;
    }

    @Override
    public long onExtractEnergy(ForgeDirection from, long request, boolean doExtract)
    {
        return request;
    }

    @Override
    public boolean canConnect(ForgeDirection direction)
    {
        return true;
    }
}
