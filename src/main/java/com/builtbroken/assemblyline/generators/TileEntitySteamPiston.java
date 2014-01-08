package com.builtbroken.assemblyline.generators;

import java.util.EnumSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.builtbroken.minecraft.prefab.TileEntityEnergyMachine;

public class TileEntitySteamPiston extends TileEntityEnergyMachine
{
    protected float wattPerSteam = 32.0f;
    protected float maxWattOutput = 500f;
    protected float maxSteamInput = 50f;
    protected float wattsOut = 0;
    protected int heatUpTime = 100;
    protected int heatTicks = 0;

    public TileEntitySteamPiston()
    {
        super(0, 0);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (this.isFunctioning())
        {
            if (heatTicks < heatUpTime)
            {
                heatTicks++;
            }
            this.wattsOut = this.maxWattOutput * (heatTicks / heatUpTime);
        }
        else
        {
            if (heatTicks > 0)
            {
                heatTicks--;
            }
        }

    }

    @Override
    public boolean canFunction()
    {
        TileEntity ent = this.worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);
        return super.canFunction() && ent instanceof TileEntitySteamGen && ((TileEntitySteamGen) ent).isCreatingSteam();
    }

    protected void updateAnimation()
    {

    }

    @Override
    public EnumSet<ForgeDirection> getInputDirections()
    {
        return EnumSet.noneOf(ForgeDirection.class);
    }

    @Override
    public EnumSet<ForgeDirection> getOutputDirections()
    {
        return EnumSet.allOf(ForgeDirection.class);
    }

}
