package com.builtbroken.minecraft.fluid;

import net.minecraftforge.fluids.Fluid;

/** Some common Fluid that other mods use
 * 
 * @author DarkGuardsman */
public enum EnumFluid
{
    FUEL(new Fluid("fuel").setUnlocalizedName("fluid.fuel.name")),
    OIL(new Fluid("oil").setUnlocalizedName("fluid.oil.name").setDensity(1500).setViscosity(4700)),
    BIOFUEL(new Fluid("biofuel").setUnlocalizedName("fluid.biofuel.name")),
    WASTE(new Fluid("waste").setUnlocalizedName("fluid.waste.name").setDensity(1300).setViscosity(1800));

    public final Fluid fluid;

    private EnumFluid(Fluid fluid)
    {
        this.fluid = fluid;
    }
}
