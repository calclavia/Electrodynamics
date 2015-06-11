package com.resonant.wrapper.lib.utility.science;

/**
 * Used to store values related to temperature and heat of a material. Temperatures are in kelvin,
 * and heat in joules
 * @author DarkGuardsman
 */
public class HeatingData {
	/**
	 * (K) temperature kelvin that this material goes from solid to liquid, or back
	 */
	public float meltingPoint;
	/**
	 * (K) temperature kelvin that this material goes from liquid to gas, or back
	 */
	public float boilingPoint;
	/**
	 * (kJ/mol) Energy per gram needed to make the final leap from solid to liquid
	 */
	public float latentFusionHeat;
	/**
	 * (kJ/mol) Energy per gram needed to make the final leap from liquid to gas
	 */
	public float latentVaporizationHeat;
	/**
	 * (j/kg K) Heating rate, at constant volume if gas
	 */
	public float specificHeat;
	/**
	 * How much the material expands
	 */
	public float thermalExpasion;
	/**
	 * (W/(m K)) How well does the material conduct heat
	 */
	public float thermalConductivity;

	public HeatingData(float meltingPoint, float boilingPoint, float fisionHeat, float vaporHeat, float specificHeat) {
		this.meltingPoint = meltingPoint;
		this.boilingPoint = boilingPoint;
		this.latentFusionHeat = fisionHeat;
		this.latentVaporizationHeat = vaporHeat;
		this.specificHeat = specificHeat;
	}

	public HeatingData(float meltingPoint, float boilingPoint, float fisionHeat, float vaporHeat, float specificHeat, float thermalExpansion, float thermalConductivity) {
		this(meltingPoint, boilingPoint, fisionHeat, vaporHeat, specificHeat);
		this.thermalConductivity = thermalConductivity;
		this.thermalExpasion = thermalExpansion;
	}
}
