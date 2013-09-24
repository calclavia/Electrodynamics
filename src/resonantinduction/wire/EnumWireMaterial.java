package resonantinduction.wire;

import universalelectricity.core.vector.Vector3;

/**
 * An enumerator for different wire materials. The metadata of the wire determines the type of the
 * wire.
 * 
 * @author Calclavia
 * 
 */

public enum EnumWireMaterial
{
	COPPER(12.5f, 3, 2, new Vector3(184, 115, 51)),
	TIN(13, 2, 0.5f, new Vector3(132, 132, 130)),
	IRON(0.1f, 2, 4, new Vector3(97, 102, 105)),
	ALUMINUM(0.025f, 6, 0.15f, new Vector3(215, 205, 181)),
	SILVER(0.005f, 1, 2, new Vector3(192, 192, 192)),
	SUPERCONDUCTOR(0, 8, Float.MAX_VALUE, new Vector3(212, 175, 55));

	public final float resistance;
	public final float damage;
	public final float maxAmps;
	public final Vector3 color;

	EnumWireMaterial(float resistance, float electrocutionDamage, float maxAmps, Vector3 color)
	{
		this.resistance = resistance;
		this.damage = electrocutionDamage;
		this.maxAmps = maxAmps;
		this.color = color.scale(1D/255D);
	}
}