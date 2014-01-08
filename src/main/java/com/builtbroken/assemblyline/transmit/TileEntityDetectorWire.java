package com.builtbroken.assemblyline.transmit;

public class TileEntityDetectorWire extends TileEntityWire
{

    @Override
    public void updateEntity()
    {
        if (!this.worldObj.isRemote && this.getNetwork() != null && this.ticks % 5 == 0)
        {
            //TODO if produced watts > minimal set value by the detector gui activate redstone on all sides
        }
    }

    @Override
    public boolean canUpdate()
    {
        return true;
    }
}
