package resonantinduction.wire;

/**
 * An enumerator for different wire materials.
 * 
 * @author Calclavia
 * 
 */

public enum EnumWire
{
	COPPER(0.0125f, 3, 200), TIN(0.01f, 2, 30), IRON(0.005f, 1, 300), ALUMINUM(0.025f, 8, 15),
	SILVER(0.005f, 1, 300), SUPERCONDUCTOR(0, 5, Integer.MAX_VALUE);

	public final float resistance;
	public final int damage;
	public final int maxAmps;

	EnumWire(float resistance, int electrocutionDamage, int maxAmps)
	{
		this.resistance = resistance;
		this.damage = electrocutionDamage;
		this.maxAmps = maxAmps;
	}
}