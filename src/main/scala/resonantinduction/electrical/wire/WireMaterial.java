package resonantinduction.electrical.wire;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * An enumerator for different wire materials. The metadata of the wire determines the type of the
 * wire.
 *
 * @author Calclavia
 */
public enum WireMaterial
{
	/**
	 * Copper: General.
	 */
	COPPER("Copper", 1.68f, 5, 200, 0xB87333),
	/**
	 * Tin: Low shock, cheap
	 */
	TIN("Tin", 3.1f, 1, 100, 0x848482),
	/**
	 * Iron: High Capacity
	 */
	IRON("Iron", 3f, 3, 800, 0x616669),
	/**
	 * Aluminum: High Shock
	 */
	ALUMINUM("Aluminum", 2.6f, 10, 600, 0xD7CDB5),
	/**
	 * Aluminum: Low Resistance
	 */
	SILVER("Silver", 1.59f, 5, 700, 0xC0C0C0),
	/**
	 * Superconductor: Over-powered
	 */
	SUPERCONDUCTOR("Superconductor", 0, 10, 1000000, 0xFFFF01);

	public final String name;
	public final float resistance;
	public final int damage;
	public final long maxCurrent;
	public final int color;

	private WireMaterial(String name, float resistance, int damage, long maxCurrent, int color)
	{
		this.name = name;
		this.resistance = resistance;
		this.damage = damage;
		this.maxCurrent = maxCurrent;
		this.color = color;
	}

}