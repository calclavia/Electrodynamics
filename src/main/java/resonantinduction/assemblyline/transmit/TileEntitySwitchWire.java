package resonantinduction.assemblyline.transmit;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntitySwitchWire extends TileEntityWire
{
    protected boolean activated = true;

    @Override
    public TileEntity[] getConnections()
    {
        if (activated)
        {
            return super.getConnections();
        }
        return null;
    }

    public void setActivate(boolean yes)
    {
        boolean p = this.activated;
        this.activated = yes;
        if (p != this.activated)
        {
            this.refresh();
        }
    }

    public boolean getActivated()
    {
        return this.activated;
    }

    @Override
    public boolean canConnect(ForgeDirection direction)
    {
        return this.getActivated();
    }
}
