package resonantinduction.electrical.wire;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import codechicken.lib.colour.ColourRGBA;

/**
 * An enumerator for different wire materials. The metadata of the wire determines the type of the
 * wire.
 * 
 * @author Calclavia
 * 
 */

public enum EnumWireMaterial
{
	/** Copper: General. */
	COPPER("Copper", 1.68f, 5, 200, 184, 115, 51),
	/** Tin: Low shock, cheap */
	TIN("Tin", 3.1f, 1, 100, 132, 132, 130),
	/** Iron: High Capacity */
	IRON("Iron", 3f, 3, 800, 97, 102, 105),
	/** Aluminum: High Shock */
	ALUMINUM("Aluminum", 2.6f, 10, 600, 215, 205, 181),
	/** Aluminum: Low Resistance */
	SILVER("Silver", 1.59f, 5, 700, 192, 192, 192),
	/** Superconductor: Over-powered */
	SUPERCONDUCTOR("Superconductor", 0, 10, 1000000, 255, 255, 1);

	public final float resistance;
	public final int damage;
	public final long maxAmps;
	public final ColourRGBA color;
	private ItemStack wire;
	private final String name;

	private EnumWireMaterial(String name, float resistance, int electrocutionDamage, long maxAmps, int r, int g, int b)
	{
		this.name = name;
		/** Multiply the realistic resistance by a factor for game balance. */
		this.resistance = resistance / 100;
		this.damage = electrocutionDamage;
		this.maxAmps = maxAmps;
		this.color = new ColourRGBA(r, g, b, 255);
	}

	public String getName()
	{
		return name;
	}

	public ItemStack getWire()
	{
		return getWire(1);
	}

	public ItemStack getWire(int amount)
	{
		ItemStack returnStack = wire.copy();
		returnStack.stackSize = amount;

		return returnStack;
	}

	public void setWire(ItemStack item)
	{
		if (wire == null)
		{
			wire = item;
			OreDictionary.registerOre(getName().toLowerCase() + "Wire", wire);
		}
	}

	public void setWire(Item item)
	{
		setWire(new ItemStack(item, 1, ordinal()));
	}

	public void setWire(Block block)
	{
		setWire(new ItemStack(block, 1, ordinal()));
	}
}