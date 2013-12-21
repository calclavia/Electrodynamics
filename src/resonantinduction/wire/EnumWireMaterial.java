package resonantinduction.wire;

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
	COPPER("Copper", 12.5F, 3, 20, 184, 115, 51), TIN("Tin", 13, 2, 5, 132, 132, 130),
	IRON("Iron", 0.1F, 20, 40, 97, 102, 105), ALUMINUM("Aluminum", 0.025F, 6, 150, 215, 205, 181),
	SILVER("Silver", 5F, 1, 20, 192, 192, 192),
	SUPERCONDUCTOR("Superconductor", 0, 1, 100, 192, 192, 192);

	public final float resistance;
	public final int damage;
	public final long maxAmps;
	public final ColourRGBA color;
	private ItemStack wire;
	private final String name;

	private EnumWireMaterial(String s, float resist, int electrocution, long max, int r, int g, int b)
	{
		name = s;
		resistance = resist;
		damage = electrocution;
		maxAmps = max;
		color = new ColourRGBA(r, g, b, 1);
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