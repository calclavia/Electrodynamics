package com.builtbroken.minecraft.tilenetwork;

import universalelectricity.core.block.IElectricalStorage;

/** Tiles that use NetworkSharedPower class should implements this. All methods in IElectricalStorage
 * should point to the network instead of the tile. This is why more energy methods are added to
 * this interface
 * 
 * @author DarkGuardsman */
public interface INetworkEnergyPart extends INetworkPart, IElectricalStorage
{
    /** Gets the energy stored in the part */
    public float getPartEnergy();

    /** Gets the max energy storage limit of the part */
    public float getPartMaxEnergy();

    /** Sets the energy stored in the part */
    public void setPartEnergy(float energy);
}
