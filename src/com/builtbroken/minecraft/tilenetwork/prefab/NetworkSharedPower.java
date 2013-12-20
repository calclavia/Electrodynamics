package com.builtbroken.minecraft.tilenetwork.prefab;

import net.minecraft.tileentity.TileEntity;
import universalelectricity.core.block.IElectricalStorage;
import universalelectricity.core.electricity.ElectricityPack;

import com.builtbroken.minecraft.interfaces.IPowerLess;
import com.builtbroken.minecraft.tilenetwork.INetworkEnergyPart;
import com.builtbroken.minecraft.tilenetwork.INetworkPart;

/** Used for tile networks that only need to share power or act like a group battery that doesn't
 * store power on world save
 * 
 * @author DarkGuardsman */
public class NetworkSharedPower extends NetworkTileEntities implements IElectricalStorage, IPowerLess
{
    private float energy, energyMax;
    private boolean runPowerLess;

    public NetworkSharedPower(INetworkPart... parts)
    {
        super(parts);
    }

    @Override
    public boolean isValidMember(INetworkPart part)
    {
        return super.isValidMember(part) && part instanceof INetworkEnergyPart;
    }

    public float receiveElectricity(TileEntity source, float power, boolean doFill)
    {
        if (!this.runPowerLess && this.networkMembers.contains(source))
        {
            return this.receiveElectricity(power, doFill);
        }
        return 0;
    }

    public float receiveElectricity(ElectricityPack receive, boolean doReceive)
    {
        if (receive != null)
        {
            float prevEnergyStored = this.getEnergyStored();
            float newStoredEnergy = Math.min(this.getEnergyStored() + receive.getWatts(), this.getMaxEnergyStored());

            if (doReceive)
            {
                this.setEnergyStored(newStoredEnergy);
            }

            return Math.max(newStoredEnergy - prevEnergyStored, 0);
        }
        return 0;
    }

    public float receiveElectricity(float energy, boolean doReceive)
    {
        return this.receiveElectricity(ElectricityPack.getFromWatts(energy, .120f), doReceive);
    }

    public boolean drainPower(TileEntity source, float power, boolean doDrain)
    {
        if (this.networkMembers.contains(source) && (this.getEnergyStored() >= power || this.runPowerLess))
        {
            if (doDrain && !this.runPowerLess)
            {
                this.setEnergyStored(this.getEnergyStored() - power);
            }
            return true;
        }
        return false;
    }

    @Override
    public void cleanUpMembers()
    {
        super.cleanUpMembers();
        boolean set = false;
        this.energyMax = 0;
        for (INetworkPart part : this.networkMembers)
        {
            if (!set && part instanceof IPowerLess && ((IPowerLess) part).runPowerLess())
            {
                this.setPowerLess(((IPowerLess) part).runPowerLess());
                set = true;
            }
            if (part instanceof INetworkEnergyPart)
            {
                this.energyMax += ((INetworkEnergyPart) part).getPartMaxEnergy();
            }
        }

    }

    @Override
    public boolean runPowerLess()
    {
        return this.runPowerLess;
    }

    @Override
    public void setPowerLess(boolean bool)
    {
        this.runPowerLess = bool;
        for (INetworkPart part : this.networkMembers)
        {
            if (part instanceof IPowerLess)
            {
                ((IPowerLess) part).setPowerLess(bool);
            }

        }
    }

    @Override
    public void setEnergyStored(float energy)
    {
        this.energy = energy;
        if (this.energy > this.getMaxEnergyStored())
        {
            this.energy = this.getMaxEnergyStored();
        }
    }

    @Override
    public float getEnergyStored()
    {
        if (this.energy < 0)
        {
            this.energy = 0;
        }
        return this.energy;
    }

    @Override
    public float getMaxEnergyStored()
    {
        if (this.energyMax < 0)
        {
            this.energyMax = Math.abs(this.energyMax);
        }
        return this.energyMax;
    }

    /** Space left to store more energy */
    public float getEnergySpace()
    {
        return Math.max(this.getMaxEnergyStored() - this.getEnergyStored(), 0);
    }

    @Override
    public void save()
    {
        this.cleanUpMembers();
        float energyRemaining = this.getEnergyStored();
        for (INetworkPart part : this.getMembers())
        {
            float watts = energyRemaining / this.getMembers().size();
            if (part instanceof INetworkEnergyPart)
            {
                ((INetworkEnergyPart) part).setEnergyStored(Math.min(watts, ((INetworkEnergyPart) part).getMaxEnergyStored()));
                energyRemaining -= Math.min(watts, ((INetworkEnergyPart) part).getMaxEnergyStored());
            }
        }
    }

    @Override
    public void load()
    {
        this.setEnergyStored(0);
        this.cleanUpMembers();
        for (INetworkPart part : this.getMembers())
        {
            if (part instanceof INetworkEnergyPart)
            {
                this.setEnergyStored(this.getEnergyStored() + ((INetworkEnergyPart) part).getPartEnergy());
            }
        }
    }

}
