package resonantinduction.assemblyline.fluid;

import com.builtbroken.common.science.ChemElement;
import com.builtbroken.common.science.ChemicalCompound;

/** Enum of gases used to create all the gas fluids
 * 
 * @author DarkGuardsman */
public enum EnumGas
{
    CARBONDIOXIDE("Carbon DiOxide", false),
    OXYGEN(ChemElement.Oxygen, 2f, true),
    BUTANE(ChemicalCompound.BUTANE, true),
    METHANE(ChemicalCompound.METHANE, true),
    NATURAL_GAS("Natural Gas", false),
    PROPANE("Propane", false);

    /** Name used when creating this as a fluid */
    public final String fluidName;
    /** Name used to display to the players */
    public final String name;
    /** Object data reference that was used to create this gas, can be a ChemicalCompound, Element,
     * or Fluid */
    public final Object data;
    public boolean enabled = false;
    /** Only used for elements since when used as a gas they sometimes bind together */
    private float molePerGasMolecule = 1.0f;
    /** Local instance of the gas used when the getGas method is called */
    private Gas gas;

    private EnumGas(String name, boolean enabled)
    {
        this.fluidName = name.replace(" ", "").toLowerCase();
        this.name = name;
        data = null;
        this.enabled = enabled;
    }

    private EnumGas(ChemicalCompound compound, boolean enabled)
    {
        this.fluidName = "gas:" + compound.compoundName.replace(" ", "").toLowerCase();
        this.name = compound.compoundName;
        data = compound;
        this.enabled = enabled;
    }

    private EnumGas(ChemElement element, float molesPerGasMolecule, boolean enabled)
    {
        this.fluidName = "gas:" + element.elementName.replace(" ", "").toLowerCase();
        this.name = element.elementName;
        data = element;
        this.enabled = enabled;
        this.molePerGasMolecule = molesPerGasMolecule;
    }

    public Gas getGas()
    {
        if (gas == null)
        {
            gas = new Gas(fluidName);
            if (data instanceof ChemElement)
            {
                gas.setDensity((int) ((ChemElement) data).density * 1000);
            }
            else if (data instanceof ChemicalCompound)
            {
                gas.setDensity((int) ((ChemicalCompound) data).density * 1000);
            }
            else
            {
                gas.setDensity(-1000);
            }
        }
        return gas;
    }
}
