package resonantinduction.mechanical.fluid.pipe;

import java.awt.Color;

/**
 * Enumerator to hold info about each pipe material.
 * 
 * @author Calclavia
 */
public enum EnumPipeMaterial
{
	CERAMIC(5, 5, new Color(0xB3866F)), BRONZE(25, 25, new Color(0xD49568)),
	PLASTIC(50, 30, new Color(0xDAF4F7)), IRON(100, 50, new Color(0x5C6362)),
	STEEL(100, 100, new Color(0x888888)), FIBERGLASS(1000, 200, new Color(0x9F9691));

	public final int maxPressure;

	/**
	 * The max flow rate in liters of tick.
	 */
	public final int maxFlowRate;
	public final Color color;

	private EnumPipeMaterial(int maxFlowRate, int maxPressure, Color color)
	{
		this.maxFlowRate = maxFlowRate;
		this.maxPressure = maxPressure;
		this.color = color;
	}
}
