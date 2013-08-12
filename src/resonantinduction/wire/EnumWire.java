package resonantinduction.wire;

/**
 * An enumerator for different wire materials. The metadata of the wire determines the type of the
 * wire.
 * 
 * @author Calclavia
 * 
 */

public enum EnumWire
{
	COPPER(12.5f, 3, 2), TIN(10, 2, 0.5f), IRON(0.1f, 2, 4), ALUMINUM(0.025f, 6, 0.15f),
	SILVER(0.005f, 1, 2), SUPERCONDUCTOR(0, 8, Float.MAX_VALUE);

	public final float resistance;
	public final float damage;
	public final float maxAmps;

	EnumWire(float resistance, float electrocutionDamage, float maxAmps)
	{
		this.resistance = resistance;
		this.damage = electrocutionDamage;
		this.maxAmps = maxAmps;
	}
}