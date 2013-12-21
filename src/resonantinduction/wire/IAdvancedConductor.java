package resonantinduction.wire;

import universalelectricity.api.energy.IConductor;

/**
 * A connector for {EnergyNetwork}.
 * 
 * @author Calclavia
 * 
 */
public interface IAdvancedConductor extends IConductor
{
	/**
	 * The insulatation methods.
	 * 
	 * @return
	 */
	public boolean isInsulated();

	public void setInsulated(boolean insulated);

	public int getInsulationColor();

	public void setInsulationColor(int dye);

	public EnumWireMaterial getMaterial();
}
